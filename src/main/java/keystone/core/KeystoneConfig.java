package keystone.core;

import keystone.api.Keystone;
import keystone.api.KeystoneDirectories;
import keystone.api.variables.DisplayModifiers;
import keystone.api.variables.EditorDirtyFlag;
import keystone.api.variables.FloatRange;
import keystone.api.variables.Header;
import keystone.api.variables.Hook;
import keystone.api.variables.IntRange;
import keystone.api.variables.Tooltip;
import keystone.api.variables.Variable;
import keystone.core.modules.filter.cache.FilterCache;
import keystone.core.serialization.VariablesSerializer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;

import java.io.File;
import java.io.IOException;

public class KeystoneConfig
{
    @EditorDirtyFlag private static boolean editorDirty;

    //region Exposed Config Options
    @Header("Installation")
    @Variable public static String keystoneDirectory = "keystone";
    @Variable public static boolean startActive = false;
    
    @Header("Performance")
    @Variable @IntRange(min = 1, scrollStep = 16) public static int maxBrushSize = 512;
    @Variable @IntRange(min = 1) public static int maxChunkUpdatesPerTick = 64;
    @Variable @IntRange(min = 0) public static int chunkUpdateCooldownTicks = 0;

    @Header("Controls")
    @Variable @FloatRange(min = 0.05f) @DisplayModifiers(numberScale = 200.0f) @Hook("flySpeedHook") public static float flySpeed = 0.1f;
    @Variable @FloatRange(min = 0.0f, max = 1.0f, scrollStep = 0.1f) public static float flySmoothing = 0.0f;
    @Variable @FloatRange(min = 0.05f) @DisplayModifiers(numberScale = 200.0f) public static float flySpeedChangeAmount = 0.05f;
    
    @Header("GUI Settings")
    @Variable @IntRange(min = 0) public static int viewportPadding = 10;
    @Variable @IntRange(min = 1) public static int minGuiScale = 2;
    @Variable @IntRange(min = 1) public static int guiScrollSpeed = 10;
    @Variable @FloatRange(min = 0, scrollStep = 0.25f) @DisplayModifiers(numberScale = 0.05f) public static float tooltipDelay = 20;
    
    @Header("Highlighting")
    @Variable public static boolean highlightTileEntities = true;
    @Variable public static boolean highlightEntities = true;

    @Header("Global Directories")
    @Variable public static String analysesDirectory = "analyses";
    @Variable public static String schematicsDirectory = "schematics";
    @Variable public static String palettesDirectory = "palettes";
    @Variable public static String masksDirectory = "masks";
    @Variable public static String filtersDirectory = "filters";
    @Variable public static String stockFilterCache = "stock_filters";
    
    @Header("Local Directories")
    @Variable public static String sessionDirectory = "session";
    @Variable public static String backupDirectory = "backup";
    @Variable public static String historyDirectory = "history";
    //endregion
    //region Code-Only Config Options
    public static boolean debugHistoryLog = false;
    
    public static int clickThreshold = 200;
    public static int dragThresholdSqr = 8 * 8;
    
    public static float viewportTopBorder = 0.05f;
    public static float viewportBottomBorder = 0.075f;
    public static float viewportLeftBorder = 0.25f;
    public static float viewportRightBorder = 0.25f;
    //endregion
    //region Debugging Options
    @Header("Debug Flags")
    @Hook("trimCache")
    @Tooltip("Clicking this box will trim the current version's filter cache.")
    @Variable static boolean trimFilterCache = false;

    @Hook("trimCacheAll")
    @Tooltip("Clicking this box will trim all version's filter caches.")
    @Variable static boolean trimAllFilterCaches = false;
    //endregion

    //region Serialization
    public static void save()
    {
        try
        {
            NbtCompound nbt = VariablesSerializer.write(KeystoneConfig.class, null);
            NbtIo.write(nbt, KeystoneDirectories.getKeystoneDirectory().resolve("config.nbt").toFile());
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
            File configFile = KeystoneDirectories.getKeystoneDirectory().resolve("config.nbt").toFile();
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
    //endregion
    //region Hooks
    private static void flySpeedHook(float speed) { Keystone.setFlySpeed(speed); }
    private static void trimCache()
    {
        if (trimFilterCache)
        {
            FilterCache.trim();
            trimFilterCache = false;
            editorDirty = true;
        }
    }
    private static void trimCacheAll()
    {
        if (trimAllFilterCaches)
        {
            FilterCache.trimAllVersions();
            trimAllFilterCaches = false;
            editorDirty = true;
        }
    }
    //endregion
}
