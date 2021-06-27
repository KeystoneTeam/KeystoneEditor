package keystone.api;

import keystone.core.KeystoneConfig;
import net.minecraft.client.Minecraft;

import java.io.File;

public class KeystoneDirectories
{
    private static File filterDirectory;
    private static File stockFilterCache;
    private static File schematicDirectory;

    public static void init()
    {
        filterDirectory = Minecraft.getInstance().gameDirectory.toPath().resolve(KeystoneConfig.filtersDirectory).toFile();
        if (!filterDirectory.exists()) filterDirectory.mkdirs();

        stockFilterCache = Minecraft.getInstance().gameDirectory.toPath().resolve(KeystoneConfig.stockFilterCache).toFile();
        if (!stockFilterCache.exists()) stockFilterCache.mkdirs();

        schematicDirectory = Minecraft.getInstance().gameDirectory.toPath().resolve(KeystoneConfig.schematicsDirectory).toFile();
        if (!schematicDirectory.exists()) schematicDirectory.mkdirs();
    }

    public static File getFilterDirectory() { return filterDirectory; }
    public static File getStockFilterCache() { return stockFilterCache; }
    public static File getSchematicDirectory() { return schematicDirectory; }
}
