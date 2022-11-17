package keystone.core;

import keystone.api.KeystoneDirectories;
import keystone.api.variables.Header;
import keystone.api.variables.Variable;
import keystone.core.serialization.VariablesSerializer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;

import java.io.File;
import java.io.IOException;

public class KeystoneConfig
{
    @Header("Performance")
    @Variable public static int maxBrushSize = 512;
    @Variable public static int maxChunkUpdatesPerTick = 64;
    @Variable public static int chunkUpdateCooldownTicks = 0;

    @Header("Controls")
    @Variable public static boolean startActive = false;
    @Variable public static float flySpeed = 0.1f;
    @Variable public static float flySmoothing = 0.0f;
    @Variable public static float flySpeedChangeAmount = 0.05f;
    
    @Header("GUI Settings")
    @Variable public static int viewportPadding = 10;
    @Variable public static int minGuiScale = 2;
    @Variable public static int guiScrollSpeed = 10;
    @Variable public static float tooltipDelay = 20;

    @Header("Directories")
    @Variable public static String keystoneDirectory = "keystone";
    @Variable public static String analysesDirectory = "analyses";
    @Variable public static String filtersDirectory = "filters";
    @Variable public static String schematicsDirectory = "schematics";
    @Variable public static String stockFilterCache = "stock_filters";
    @Variable public static String sessionDirectory = "session";
    @Variable public static String backupDirectory = "backup";
    @Variable public static String historyDirectory = "history";
    
    public static boolean disableInGameMenu = true;
    public static boolean debugHistoryLog = false;
    
    public static int clickThreshold = 200;
    public static int dragThresholdSqr = 8 * 8;
    
    public static float viewportTopBorder = 0.05f;
    public static float viewportBottomBorder = 0.075f;
    public static float viewportLeftBorder = 0.25f;
    public static float viewportRightBorder = 0.25f;

    public static void save()
    {
        try
        {
            NbtCompound nbt = VariablesSerializer.write(KeystoneConfig.class, null);
            NbtIo.write(nbt, KeystoneDirectories.getKeystoneDirectory().toPath().resolve("config.nbt").toFile());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public static void load()
    {
        try
        {
            File configFile = KeystoneDirectories.getKeystoneDirectory().toPath().resolve("config.nbt").toFile();
            if (configFile.exists() && configFile.isFile())
            {
                NbtCompound nbt = NbtIo.read(configFile);
                if (nbt != null) VariablesSerializer.read(KeystoneConfig.class, nbt, () -> null);
            }
            else save();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
