package keystone.core.filters;

import keystone.api.Keystone;
import keystone.api.filters.KeystoneFilter;
import net.openhft.compiler.CachedCompiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.stream.Collectors;

public class FilterCompiler
{
    private static String createRandomClassName()
    {
        StringBuilder sb = new StringBuilder();
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        Random rand = new Random();

        sb.append("Filter_");
        for (int i = 0; i < 32; i++) sb.append(chars[rand.nextInt(chars.length)]);

        return sb.toString();
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

            try
            {
                CachedCompiler compiler = new CachedCompiler(null, null);
                Class loadedClass = compiler.loadFromJava(KeystoneFilter.class.getClassLoader(), className, filterCode);

                try
                {
                    Class<? extends KeystoneFilter> filterClass = loadedClass.asSubclass(KeystoneFilter.class);
                    try
                    {
                        KeystoneFilter filterInstance = filterClass.newInstance();
                        return filterInstance;
                    }
                    catch (InstantiationException | IllegalAccessException e)
                    {
                        Keystone.LOGGER.error("Could not instantiate filter class '" + filterName + "'!");
                        e.printStackTrace();
                        return null;
                    }
                }
                catch (ClassCastException e)
                {
                    Keystone.LOGGER.error("Class '" + filterName + "' does not extend KeystoneFilter!");
                    return null;
                }
            }
            catch (ClassNotFoundException e)
            {
                Keystone.LOGGER.error("Filter class '" + filterName + "' not found! Make sure the filter class and file name are the same.");
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
}
