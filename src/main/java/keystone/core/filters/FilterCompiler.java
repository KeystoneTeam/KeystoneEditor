package keystone.core.filters;

import javafx.stage.FileChooser;
import keystone.api.Keystone;
import keystone.api.filters.KeystoneFilter;
import keystone.core.KeystoneConfig;
import net.minecraft.client.Minecraft;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.Scanner;
import org.codehaus.janino.SimpleCompiler;

import java.io.*;
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
    public static KeystoneFilter compileFilter(String filterPath)
    {
        File filterFile = new File(filterPath);
        String filterName = filterFile.getName().replaceAll(" ", "").replaceFirst("[.][^.]+$", "");
        String className = createRandomClassName();

        try
        {
            String filterCode = Files.lines(Paths.get(filterPath)).collect(Collectors.joining(System.lineSeparator()));
            filterCode = filterCode.replaceAll(filterName, className);
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
                    Class loadedClass = Class.forName(className, true, classLoader);
                    try
                    {
                        Class<? extends KeystoneFilter> filterClass = loadedClass.asSubclass(KeystoneFilter.class);
                        return filterClass.newInstance();
                    }
                    catch (ClassCastException e)
                    {
                        Keystone.LOGGER.error("Class '" + filterName + "' does not extend KeystoneFilter!");
                        return null;
                    }
                    catch (IllegalAccessException e)
                    {
                        Keystone.LOGGER.error("Cannot access filter constructor! Ensure the filter has a public zero-argument constructor.");
                        return null;
                    }
                    catch (InstantiationException e)
                    {
                        Keystone.LOGGER.error("Error instantiating filter '" + filterName + "'!");
                        e.printStackTrace();
                        return null;
                    }
                }
                catch (ClassNotFoundException e)
                {
                    Keystone.LOGGER.error("Unable to find class '" + filterName + "'! Make sure your filter class and file share the same name.");
                    return null;
                }
            }
            catch (CompileException e)
            {
                Keystone.LOGGER.error("Unable to compile filter '" + filterName + "'!");
                e.printStackTrace();
                return null;
            }

        }
        catch (FileNotFoundException e)
        {
            Keystone.LOGGER.error("Filter file '" + filterPath + "' not found!");
            return null;
        }
        catch (IOException e)
        {
            Keystone.LOGGER.error("Could not read filter file '" + filterPath + "'!");
            e.printStackTrace();
            return null;
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
