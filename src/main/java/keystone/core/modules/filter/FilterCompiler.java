package keystone.core.modules.filter;

import keystone.api.Keystone;
import keystone.api.KeystoneCache;
import keystone.api.filters.KeystoneFilter;
import keystone.core.KeystoneMod;
import keystone.core.modules.filter.providers.IFilterProvider;
import keystone.core.modules.filter.providers.impl.SimpleFilterProvider;
import keystone.core.modules.filter.remapper.FilterRemapper;
import keystone.core.utils.FileUtils;
import keystone.core.utils.Result;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.codehaus.commons.compiler.CompileException;
import org.codehaus.commons.compiler.InternalCompilerException;
import org.codehaus.commons.compiler.util.resource.DirectoryResourceFinder;
import org.codehaus.commons.compiler.util.resource.FileResourceCreator;
import org.codehaus.janino.ClassLoaderIClassLoader;
import org.codehaus.janino.Compiler;
import oshi.util.FileUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class FilterCompiler
{
    private static final List<IFilterProvider> FILTER_PROVIDERS = List.of(
            SimpleFilterProvider.INSTANCE
    );

    public static KeystoneFilter loadFilter(File filterSource)
    {
        // Check Each Provider
        for (IFilterProvider provider : FILTER_PROVIDERS)
        {
            // If the provider supports the source file type
            if (provider.isSourceSupported(filterSource))
            {
                // Run the provider on the filter source
                Result<Path> providerResult = provider.getFilter(filterSource);

                // If the provider wasn't successful
                if (providerResult.isFailed())
                {
                    providerResult.logFailure();
                    return new KeystoneFilter().setName(KeystoneFilter.getFilterName(filterSource, false)).setCompilerException(providerResult.exception());
                }

                // If the provider was successful
                else
                {
                    String error = "Filter loading is currently being rewritten. If you see this, the currently implemented parts of the filter pipeline were successful.";
                    return new KeystoneFilter().setName(KeystoneFilter.getFilterName(filterSource, false)).setCompilerException(new NotImplementedException(error));
                }
            }
        }
        
        // Invalid Filter Source
        String error = "Unknown Filter Source '" + filterSource.toPath() + "'!";
        Keystone.LOGGER.error(error);
        sendErrorMessage(error);
        return new KeystoneFilter().setName(KeystoneFilter.getFilterName(filterSource, false)).setCompilerException(new IllegalArgumentException(error));
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
