package keystone.core;

import keystone.api.KeystoneDirectories;
import keystone.api.variables.*;
import keystone.core.serialization.VariablesSerializer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;

import java.io.File;
import java.io.IOException;

public class KeystoneConfig
{
    @Header("Performance")
    @Variable @IntRange(min = 1, scrollStep = 16) public static int maxBrushSize = 512;
    @Variable @IntRange(min = 1) public static int maxChunkUpdatesPerTick = 64;
    @Variable @IntRange(min = 0) public static int chunkUpdateCooldownTicks = 0;

    @Header("Controls")
    @Variable public static boolean startActive = false;
    @Variable @FloatRange(min = 0.05f) @DisplayScale(20) public static float flySpeed = 0.1f;
    @Variable @FloatRange(min = 0.0f, max = 1.0f, scrollStep = 0.1f) public static float flySmoothing = 0.0f;
    @Variable @FloatRange(min = 0.05f) @DisplayScale(20) public static float flySpeedChangeAmount = 0.05f;
    
    @Header("GUI Settings")
    @Variable @IntRange(min = 0) public static int viewportPadding = 10;
    @Variable @IntRange(min = 1) public static int minGuiScale = 2;
    @Variable @IntRange(min = 1) public static int guiScrollSpeed = 10;
    @Variable @FloatRange(min = 0, scrollStep = 0.25f) @DisplayScale(0.05f) public static float tooltipDelay = 20;

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
