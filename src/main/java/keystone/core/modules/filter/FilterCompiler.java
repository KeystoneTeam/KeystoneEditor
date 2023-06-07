package keystone.core.modules.filter;

import keystone.api.Keystone;
import keystone.api.filters.KeystoneFilter;
import keystone.core.modules.filter.cache.FilterCache;
import keystone.core.modules.filter.providers.IFilterProvider;
import keystone.core.modules.filter.providers.impl.SimpleFilterProvider;
import keystone.core.utils.Result;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;

public class FilterCompiler
{
    private static final List<IFilterProvider> FILTER_PROVIDERS = List.of(
            SimpleFilterProvider.INSTANCE
    );

    public static KeystoneFilter loadFilter(File filterSource)
    {
        // Get the Filter JAR File
        FilterCache.Entry cacheEntry = FilterCache.getEntry(filterSource);
        Result<Path> filterJar = getFilterJarPath(filterSource, cacheEntry);

        // If the JAR could not be found or created
        if (filterJar.isFailed())
        {
            filterJar.logFailure();
            return new KeystoneFilter().setName(KeystoneFilter.getFilterName(filterSource, false)).setCompilerException(filterJar.exception());
        }

        // Create the Filter Instance
        try
        {
            // Load the Filter Class
            URLClassLoader filterLoader = new URLClassLoader(new URL[] { filterJar.get().toUri().toURL() }, KeystoneFilter.class.getClassLoader());
            Class<?> filterClass = filterLoader.loadClass(KeystoneFilter.getFilterName(filterSource, true));

            // Check that the filter class extends KeystoneFilter
            if (!KeystoneFilter.class.isAssignableFrom(filterClass))
            {
                String error = "Filter class '" + KeystoneFilter.getFilterName(filterSource, true) + "' does not extend KeystoneFilter!";
                Keystone.LOGGER.error(error);
                sendErrorMessage(error);
                return new KeystoneFilter().setName(KeystoneFilter.getFilterName(filterSource, false)).setCompilerException(new IllegalArgumentException(error));
            }

            // Instantiate the Filter Class
            Constructor<?> filterConstructor = filterClass.getDeclaredConstructor();
            KeystoneFilter filter = (KeystoneFilter) filterConstructor.newInstance();
            filter.setName(KeystoneFilter.getFilterName(filterSource, false)).compiledSuccessfully();
            return filter;
        }
        catch (MalformedURLException e)
        {
            String error = "Could not create URLClassLoader for filter jar '" + filterJar.get() + "': " + e.getLocalizedMessage();
            Keystone.LOGGER.error(error);
            e.printStackTrace();
            sendErrorMessage(error);
            return new KeystoneFilter().setName(KeystoneFilter.getFilterName(filterSource, false)).setCompilerException(e);
        }
        catch (ClassNotFoundException e)
        {
            String error = "Could not load filter class '" + KeystoneFilter.getFilterName(filterSource, true) + "': " + e.getLocalizedMessage();
            Keystone.LOGGER.error(error);
            e.printStackTrace();
            sendErrorMessage(error);
            return new KeystoneFilter().setName(KeystoneFilter.getFilterName(filterSource, false)).setCompilerException(e);
        }
        catch (NoSuchMethodException e)
        {
            String error = "Could not find zero-parameter constructor for filter class '" + KeystoneFilter.getFilterName(filterSource, true) + "': " + e.getLocalizedMessage();
            Keystone.LOGGER.error(error);
            e.printStackTrace();
            sendErrorMessage(error);
            return new KeystoneFilter().setName(KeystoneFilter.getFilterName(filterSource, false)).setCompilerException(e);
        }
        catch (InvocationTargetException | InstantiationException | IllegalAccessException e)
        {
            String error = "Could not instantiate filter class '" + KeystoneFilter.getFilterName(filterSource, true) + "': " + e.getLocalizedMessage();
            Keystone.LOGGER.error(error);
            e.printStackTrace();
            sendErrorMessage(error);
            return new KeystoneFilter().setName(KeystoneFilter.getFilterName(filterSource, false)).setCompilerException(e);
        }

//        String error = "Filter loading is currently being rewritten. If you see this, the currently implemented parts of the filter pipeline were successful.";
//        return new KeystoneFilter().setName(KeystoneFilter.getFilterName(filterSource, false)).setCompilerException(new NotImplementedException(error));
    }
    private static Result<Path> getFilterJarPath(File filterSource, FilterCache.Entry cacheEntry)
    {
        if (!cacheEntry.remapped().toFile().isFile())
        {
            // Check Each Provider
            for (IFilterProvider provider : FILTER_PROVIDERS)
            {
                // If the provider supports the source file type
                if (provider.isSourceSupported(filterSource))
                {
                    // Run the provider on the filter source
                    return provider.getFilter(filterSource, cacheEntry);
                }
            }

            // Invalid Filter Source
            return Result.failed("Unknown Filter Source '" + filterSource.toPath() + "'");
        }
        else return Result.success(cacheEntry.remapped());
    }

    //region Helpers
    private static void sendErrorMessage(String message)
    {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) player.sendMessage(Text.literal(message).styled(style -> style.withColor(Formatting.RED)), false);
    }
    private static String createRandomClassName()
    {
        StringBuilder sb = new StringBuilder();
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

        sb.append("Filter_");
        for (int i = 0; i < 32; i++) sb.append(chars[Keystone.RANDOM.nextInt(chars.length)]);

        return sb.toString();
    }
    //endregion
}
