package keystone.api;

import keystone.core.modules.filter.cache.FilterCache;
import keystone.core.utils.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

public class KeystoneCache
{
    private static Path cacheDirectory;
    private static Path tempDirectory;
    private static Path compiledDirectory;
    private static Path remappedDirectory;
    
    public static void init()
    {
        cacheDirectory = KeystoneDirectories.getKeystoneSubdirectory(".cache");
        compiledDirectory = getCacheSubdirectory("compiled");
        remappedDirectory = getCacheSubdirectory("remapped");
    
        tempDirectory = cacheDirectory.resolve("temp");
        if (tempDirectory.toFile().exists()) cleanTempFiles();
    }
    public static void cleanTempFiles()
    {
        FileUtils.deleteRecursively(tempDirectory.toFile(), false);
    }
    
    public static Path getCacheSubdirectory(String subdirectory)
    {
        File file = cacheDirectory.resolve(subdirectory).toFile();
        if (!file.exists()) file.mkdirs();
        return file.toPath();
    }
    
    public static Path getCacheDirectory() { return cacheDirectory; }
    public static Path getCompiledDirectory() { return compiledDirectory; }
    public static Path getRemappedDirectory() { return remappedDirectory; }
    public static Path newTempDirectory()
    {
        File file = tempDirectory.resolve(UUID.randomUUID().toString()).toFile();
        file.mkdirs();
        return file.toPath();
    }
}
