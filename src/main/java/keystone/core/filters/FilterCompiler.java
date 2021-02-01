package keystone.core.filters;

import keystone.api.Keystone;
import keystone.api.filters.KeystoneFilter;
import keystone.core.KeystoneConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.CompilerFactory;
import org.codehaus.janino.JavaSourceClassLoader;
import org.codehaus.janino.Scanner;
import org.codehaus.janino.SimpleCompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.stream.Collectors;

public class FilterCompiler
{
    private static File getFilterDirectory()
    {
        File filtersDirectory = Minecraft.getInstance().gameDir.toPath().resolve(KeystoneConfig.filtersDirectory).toFile();
        if (!filtersDirectory.exists()) filtersDirectory.mkdirs();
        return filtersDirectory;
    }

    public static File[] getInstalledFilters()
    {
        return getFilterDirectory().listFiles((dir, name) -> name.endsWith(".java") || name.endsWith(".filter"));
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
            filterCode = FilterImports.addImportsToCode(filterCode);

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
            catch (CompileException e)
            {
                String error = "Unable to compile filter '" + oldClassName + "': " + e.getMessage();
                Keystone.LOGGER.error(error);
                e.printStackTrace();
                Minecraft.getInstance().player.sendMessage(new StringTextComponent(error).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
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
