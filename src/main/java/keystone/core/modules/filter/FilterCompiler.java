package keystone.core.modules.filter;

import keystone.api.Keystone;
import keystone.api.filters.KeystoneFilter;
import keystone.api.utils.StringUtils;
import keystone.core.KeystoneConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.commons.compiler.InternalCompilerException;
import org.codehaus.janino.Scanner;
import org.codehaus.janino.SimpleCompiler;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FilterCompiler
{
    private static List<File> stockFilters;
    private static void loadStockFilters()
    {
        stockFilters = new ArrayList<>();
        File stockFilterCache = getStockFilterCache();

        try
        {
            IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            Collection<ResourceLocation> filterResources = resourceManager.getAllResourceLocations("stock_filters", path -> path.endsWith(".java") || path.endsWith(".filter"));

            for (ResourceLocation filterResourceLocation : filterResources)
            {
                Matcher matcher = Pattern.compile("[0-9a-z_\\.]+$").matcher(filterResourceLocation.getPath());
                matcher.find();
                String fileName = matcher.group();
                fileName = StringUtils.titleCase(fileName.replace('_', ' '));

                File cacheFile = stockFilterCache.toPath().resolve(fileName).toFile();
                if (!cacheFile.exists()) cacheFile.createNewFile();

                try (IResource filterResource = resourceManager.getResource(filterResourceLocation);
                     InputStream filterStream = filterResource.getInputStream();
                     FileOutputStream fileOutputStream = new FileOutputStream(cacheFile))
                {
                    int read;
                    byte[] bytes = new byte[8192];
                    while ((read = filterStream.read(bytes)) != -1) fileOutputStream.write(bytes, 0, read);
                }

                stockFilters.add(cacheFile);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    private static File getStockFilterCache()
    {
        File stockFilterCache = Minecraft.getInstance().gameDir.toPath().resolve(KeystoneConfig.stockFilterCache).toFile();
        if (!stockFilterCache.exists()) stockFilterCache.mkdirs();
        return stockFilterCache;
    }
    private static File getFilterDirectory()
    {
        File filtersDirectory = Minecraft.getInstance().gameDir.toPath().resolve(KeystoneConfig.filtersDirectory).toFile();
        if (!filtersDirectory.exists()) filtersDirectory.mkdirs();
        return filtersDirectory;
    }

    public static File[] getInstalledFilters()
    {
        if (stockFilters == null || stockFilters.size() == 0) loadStockFilters();

        List<File> filters = new ArrayList<>();
        File[] customFilters = getFilterDirectory().listFiles((dir, name) -> name.endsWith(".java") || name.endsWith(".filter"));
        for (File customFilter : customFilters) filters.add(customFilter);
        filters.addAll(stockFilters);
        Collections.sort(filters, Comparator.comparing(a -> getFilterName(a, true)));

        File[] filtersArray = new File[filters.size()];
        for (int i = 0; i < filtersArray.length; i++) filtersArray[i] = filters.get(i);
        return filtersArray;
    }
    public static String getFilterName(File filterFile, boolean removeSpaces)
    {
        if (removeSpaces) return filterFile.getName().replaceAll(" ", "").replaceFirst("[.][^.]+$", "");
        else return filterFile.getName().replaceFirst("[.][^.]+$", "");
    }

    public static KeystoneFilter compileFilter(String filterPath)
    {
        File filterFile = new File(filterPath);
        String oldClassName = getFilterName(filterFile, true);
        String newClassName = createRandomClassName();

        try
        {
            String filterCode = Files.lines(Paths.get(filterPath)).collect(Collectors.joining(System.lineSeparator()));

            filterCode = filterCode.replaceAll(oldClassName, newClassName);
            FilterImports.Result imports = FilterImports.getImports(filterCode);
            filterCode = imports.newCode;

            try
            {
                Scanner scanner = new Scanner(filterFile.getName(), new StringReader(filterCode));
                SimpleCompiler compiler = new SimpleCompiler();

                compiler.setParentClassLoader(KeystoneFilter.class.getClassLoader());
                compiler.cook(scanner);

                ClassLoader classLoader = compiler.getClassLoader();
                try
                {
                    Class loadedClass = Class.forName(newClassName, true, classLoader);
                    try
                    {
                        Class<? extends KeystoneFilter> filterClass = loadedClass.asSubclass(KeystoneFilter.class);
                        return filterClass.newInstance().setName(getFilterName(filterFile, false)).compiledSuccessfully();
                    }
                    catch (ClassCastException e)
                    {
                        String error = "Class '" + oldClassName + "' does not extend KeystoneFilter!";
                        Keystone.LOGGER.error(error);
                        Minecraft.getInstance().player.sendMessage(new StringTextComponent(error).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
                        return new KeystoneFilter().setName(getFilterName(filterFile, false));
                    }
                    catch (IllegalAccessException e)
                    {
                        String error = "Cannot access filter constructor! Ensure the filter has a public zero-argument constructor.";
                        Keystone.LOGGER.error(error);
                        Minecraft.getInstance().player.sendMessage(new StringTextComponent(error).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
                        return new KeystoneFilter().setName(getFilterName(filterFile, false));
                    }
                    catch (InstantiationException e)
                    {
                        String error = "Error instantiating filter '" + oldClassName + "'!";
                        Keystone.LOGGER.error(error);
                        e.printStackTrace();
                        Minecraft.getInstance().player.sendMessage(new StringTextComponent(error).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
                        return new KeystoneFilter().setName(getFilterName(filterFile, false));
                    }
                }
                catch (ClassNotFoundException e)
                {
                    String error = "Unable to find class '" + oldClassName + "'! Make sure your filter class and file share the same name.";
                    Keystone.LOGGER.error(error);
                    Minecraft.getInstance().player.sendMessage(new StringTextComponent(error).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
                    return new KeystoneFilter().setName(getFilterName(filterFile, false));
                }
            }
            catch (CompileException | InternalCompilerException e)
            {
                String error = "Unable to compile filter '" + oldClassName + "': " + e.getLocalizedMessage();
                Matcher matcher = Pattern.compile("Line ([0-9]+)").matcher(error);
                String fixedError = error;
                while (matcher.find())
                {
                    String group = matcher.group();
                    int line = Integer.parseInt(group.split(" ")[1]) - imports.lineOffset;
                    fixedError = fixedError.replace(matcher.group(), "Line " + line);
                }

                Keystone.LOGGER.error(fixedError);
                Minecraft.getInstance().player.sendMessage(new StringTextComponent(fixedError).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
                return new KeystoneFilter().setName(getFilterName(filterFile, false));
            }
        }
        catch (FileNotFoundException e)
        {
            String error = "Filter file '" + filterPath + "' not found!";
            Keystone.LOGGER.error(error);
            Minecraft.getInstance().player.sendMessage(new StringTextComponent(error).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
            return new KeystoneFilter().setName("File Not Found");
        }
        catch (IOException e)
        {
            String error = "Could not read filter file '" + filterPath + "'!";
            Keystone.LOGGER.error(error);
            e.printStackTrace();
            Minecraft.getInstance().player.sendMessage(new StringTextComponent(error).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
            return new KeystoneFilter().setName(getFilterName(filterFile, false));
        }
    }

    private static String createRandomClassName()
    {
        StringBuilder sb = new StringBuilder();
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        Random rand = new Random();

        sb.append("Filter_");
        for (int i = 0; i < 32; i++) sb.append(chars[rand.nextInt(chars.length)]);

        return sb.toString();
    }
}
