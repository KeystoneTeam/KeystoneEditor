package keystone.api;

import keystone.core.KeystoneConfig;
import keystone.core.mixins.PersistentStateManagerAccessor;
import keystone.core.modules.world_cache.WorldCacheModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.World;

import java.io.File;

public class KeystoneDirectories
{
    private static WorldCacheModule worldCache;
    private static File keystoneDirectory;

    private static File analysesDirectory;
    private static File filterDirectory;
    private static File schematicsDirectory;
    private static File stockFilterCache;

    public static void init()
    {
        keystoneDirectory = MinecraftClient.getInstance().runDirectory.toPath().resolve(KeystoneConfig.keystoneDirectory).toFile();
        if (!keystoneDirectory.exists()) keystoneDirectory.mkdirs();

        analysesDirectory = getKeystoneSubdirectory(KeystoneConfig.analysesDirectory);
        filterDirectory = getKeystoneSubdirectory(KeystoneConfig.filtersDirectory);
        schematicsDirectory = getKeystoneSubdirectory(KeystoneConfig.schematicsDirectory);
        stockFilterCache = getKeystoneSubdirectory(KeystoneConfig.stockFilterCache);
    }

    public static File getKeystoneDirectory() { return keystoneDirectory; }
    public static File getKeystoneSubdirectory(String subdirectory)
    {
        File file = keystoneDirectory.toPath().resolve(subdirectory).toFile();
        if (!file.exists()) file.mkdirs();
        return file;
    }

    public static File getAnalysesDirectory() { return analysesDirectory; }
    public static File getFilterDirectory() { return filterDirectory; }
    public static File getSchematicsDirectory() { return schematicsDirectory; }
    public static File getStockFilterCache() { return stockFilterCache; }

    public static File getWorldCacheDirectory()
    {
        if (worldCache == null) worldCache = Keystone.getModule(WorldCacheModule.class);
        File file = ((PersistentStateManagerAccessor)worldCache.getDimensionWorld(World.OVERWORLD).getPersistentStateManager()).getDirectory().getParentFile().toPath().resolve("##KEYSTONE.TEMP##").toFile();
        if (!file.exists()) file.mkdirs();
        return file;
    }
    public static File getWorldCacheSubdirectory(String subdirectory)
    {
        File file = getWorldCacheDirectory().toPath().resolve(subdirectory).toFile();
        if (!file.exists()) file.mkdirs();
        return file;
    }

    public static File getHistoryDirectory() { return getWorldCacheSubdirectory(KeystoneConfig.historyDirectory); }
}
