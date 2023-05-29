package keystone.core.modules.filter;

import keystone.api.Keystone;
import keystone.api.KeystoneCache;
import keystone.api.filters.KeystoneFilter;
import keystone.core.modules.filter.remapper.FilterRemapper;
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
import org.codehaus.janino.Compiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class FilterCompiler
{
    public static KeystoneFilter loadFilter(File filterSource)
    {
        // Load Valid Filter Sources
        if (filterSource.isFile())
        {
            if (filterSource.getName().toLowerCase().endsWith(".java")) return compileSimpleFilter(filterSource);
            else if (filterSource.getName().toLowerCase().endsWith(".jar")) return loadJarFilter(filterSource);
        }
        else if (filterSource.isDirectory()) return compileSourceFilter(filterSource);
        
        // Invalid Filter Source
        String error = "Unknown Filter Source '" + filterSource.toPath() + "'!";
        Keystone.LOGGER.error(error);
        sendErrorMessage(error);
        return new KeystoneFilter().setName(KeystoneFilter.getFilterName(filterSource, false)).setCompilerException(new IllegalArgumentException(error));
    }
    
    //region Compilers
    private static KeystoneFilter compileSimpleFilter(File filterFile)
    {
        // Randomize Class Name
        String oldClassName = KeystoneFilter.getFilterName(filterFile, true);
        String newClassName = createRandomClassName();
        
        // Create a Janino compiler
        Compiler compiler = new Compiler();
        compiler.setTargetVersion(8);
        compiler.setIClassLoader(FilterRemapper.REMAPPED_CLASS_LOADER);
        compiler.setClassFileFinder(new DirectoryResourceFinder(KeystoneCache.getCompiledDirectory().toFile()));
        compiler.setClassFileCreator(new FileResourceCreator() { @Override protected File getFile(String resourceName) { return KeystoneCache.getCompiledDirectory().resolve(resourceName).toFile(); } });
        
        // Compile Filter File
        try
        {
            compiler.compile(new File[] { filterFile });
        }
        catch (CompileException | InternalCompilerException e)
        {
            // Error Logging
            String filterName = KeystoneFilter.getFilterName(filterFile, false);
            String error = "Unable to compile filter '" + filterName + "': " + e.getLocalizedMessage();
            Keystone.LOGGER.error(error);
            sendErrorMessage(error);
    
            e.printStackTrace();
            return new KeystoneFilter().setName(filterName).setCompilerException(e);
        }
        catch (IOException e)
        {
            // Error Logging
            String error = "Unable to open filter file '" + filterFile.toPath() + "': " + e.getLocalizedMessage();
            Keystone.LOGGER.error(error);
            sendErrorMessage(error);
    
            e.printStackTrace();
            return new KeystoneFilter().setName(KeystoneFilter.getFilterName(filterFile, false)).setCompilerException(e);
        }
    
        // TODO: Finish Filter Loading
        return new KeystoneFilter().setName(KeystoneFilter.getFilterName(filterFile, false)).setCompilerException(new NotImplementedException("Filters are currently not loaded from the compiled classes."));
    }
    private static KeystoneFilter compileSourceFilter(File filterDirectory)
    {
        throw new NotImplementedException("Keystone currently does not support complex filters!");
    }
    private static KeystoneFilter loadJarFilter(File filterJar)
    {
        throw new NotImplementedException("Keystone currently does not support pre-compiled filters!");
    }
    //endregion
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
