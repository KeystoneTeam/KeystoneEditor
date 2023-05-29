package keystone.api;

import keystone.core.KeystoneConfig;
import keystone.core.modules.world_cache.WorldCacheModule;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class KeystoneDirectories
{
    private static WorldCacheModule worldCache;
    private static Path keystoneDirectory;
    private static Path currentSaveDirectory;

    private static Path analysesDirectory;
    private static Path schematicsDirectory;
    private static Path palettesDirectory;
    private static Path masksDirectory;
    private static Path filterDirectory;
    private static Path stockFilterCache;

    public static void init() throws IOException
    {
        keystoneDirectory = MinecraftClient.getInstance().runDirectory.toPath().resolve(KeystoneConfig.keystoneDirectory);
        if (!keystoneDirectory.toFile().exists()) keystoneDirectory.toFile().mkdirs();

        analysesDirectory = getKeystoneSubdirectory(KeystoneConfig.analysesDirectory);
        schematicsDirectory = getKeystoneSubdirectory(KeystoneConfig.schematicsDirectory);
        palettesDirectory = getKeystoneSubdirectory(KeystoneConfig.palettesDirectory);
        masksDirectory = getKeystoneSubdirectory(KeystoneConfig.masksDirectory);
        filterDirectory = getKeystoneSubdirectory(KeystoneConfig.filtersDirectory);
        stockFilterCache = getKeystoneSubdirectory(KeystoneConfig.stockFilterCache);
    }
    public static void setCurrentSaveDirectory(Path currentSaveDirectory)
    {
        KeystoneDirectories.currentSaveDirectory = currentSaveDirectory;
        if (!currentSaveDirectory.toFile().exists()) currentSaveDirectory.toFile().mkdirs();
    }

    public static Path getKeystoneDirectory() { return keystoneDirectory; }
    public static Path getKeystoneSubdirectory(String subdirectory)
    {
        File file = keystoneDirectory.resolve(subdirectory).toFile();
        if (!file.exists()) file.mkdirs();
        return file.toPath();
    }

    public static Path getAnalysesDirectory() { return analysesDirectory; }
    public static Path getSchematicsDirectory() { return schematicsDirectory; }
    public static Path getPalettesDirectory() { return palettesDirectory; }
    public static Path getMasksDirectory() { return masksDirectory; }
    public static Path getFilterDirectory() { return filterDirectory; }
    public static Path getStockFilterCache() { return stockFilterCache; }

    public static Path getWorldDirectory()
    {
        return currentSaveDirectory;
    }
    public static Path getWorldCacheDirectory()
    {
        if (worldCache == null) worldCache = Keystone.getModule(WorldCacheModule.class);
        File file = currentSaveDirectory.resolve("##KEYSTONE.TEMP##").toFile();
        if (!file.exists()) file.mkdirs();
        return file.toPath();
    }
    public static Path getWorldCacheSubdirectory(String subdirectory)
    {
        File file = getWorldCacheDirectory().resolve(subdirectory).toFile();
        if (!file.exists()) file.mkdirs();
        return file.toPath();
    }

    public static Path getHistoryDirectory() { return getWorldCacheSubdirectory(KeystoneConfig.historyDirectory); }
    public static Path getWorldSessionDirectory() { return getWorldCacheSubdirectory(KeystoneConfig.sessionDirectory); }
    public static Path getWorldBackupDirectory() { return getWorldCacheSubdirectory(KeystoneConfig.backupDirectory); }
}
