package keystone.api;

import keystone.core.KeystoneConfig;
import net.minecraft.client.Minecraft;

import java.io.File;

public class KeystoneDirectories
{
    private static String currentLevelID;
    private static File keystoneDirectory;

    private static File analysesDirectory;
    private static File filterDirectory;
    private static File schematicsDirectory;
    private static File stockFilterCache;

    public static void init()
    {
        keystoneDirectory = Minecraft.getInstance().gameDirectory.toPath().resolve(KeystoneConfig.keystoneDirectory).toFile();
        if (!keystoneDirectory.exists()) keystoneDirectory.mkdirs();

        analysesDirectory = getKeystoneSubdirectory(KeystoneConfig.analysesDirectory);
        filterDirectory = getKeystoneSubdirectory(KeystoneConfig.filtersDirectory);
        schematicsDirectory = getKeystoneSubdirectory(KeystoneConfig.schematicsDirectory);
        stockFilterCache = getKeystoneSubdirectory(KeystoneConfig.stockFilterCache);
    }

    public static void setCurrentLevelID(String levelID)
    {
        currentLevelID = levelID;
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
        File file = Minecraft.getInstance().getLevelSource().getBaseDir().resolve(currentLevelID).resolve("##KEYSTONE.TEMP##").toFile();
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
