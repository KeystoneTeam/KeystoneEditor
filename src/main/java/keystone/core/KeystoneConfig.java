package keystone.core;

import keystone.api.variables.Header;
import keystone.api.variables.Variable;

public class KeystoneConfig
{

    public static boolean disableInGameMenu = true;
    public static boolean debugHistoryLog = false;

    public static int clickThreshold = 200;
    public static int dragThresholdSqr = 8 * 8;
    @Header("Performance")
    @Variable public static int maxBrushSize = 512;
    @Variable public static int maxChunkUpdatesPerTick = 64;
    @Variable public static int chunkUpdateCooldownTicks = 0;

    @Header("Controls")
    @Variable public static boolean startActive = false;
    @Variable public static float flySpeed = 0.1f;
    @Variable public static float flySmoothing = 0.0f;
    @Variable public static float flySpeedChangeAmount = 0.05f;

    public static float viewportTopBorder = 0.05f;
    public static float viewportBottomBorder = 0.075f;
    public static float viewportLeftBorder = 0.25f;
    public static float viewportRightBorder = 0.25f;
    @Header("GUI Settings")
    @Variable public static int viewportPadding = 5;
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
}
