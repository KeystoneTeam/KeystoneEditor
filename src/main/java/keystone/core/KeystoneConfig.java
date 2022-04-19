package keystone.core;

public class KeystoneConfig
{
    public static String keystoneDirectory = "keystone";
    public static String analysesDirectory = "analyses";
    public static String filtersDirectory = "filters";
    public static String schematicsDirectory = "schematics";
    public static String stockFilterCache = "stock_filters";

    public static String historyDirectory = "history";

    public static boolean startActive = true;
    public static boolean disableInGameMenu = true;
    public static boolean debugHistoryLog = false;

    public static int clickThreshold = 200;
    public static int dragThresholdSqr = 8 * 8;
    public static int maxBrushSize = 512;

    public static float flySpeed = 0.1f;

    public static boolean translucentGhostBlocks = false;
    public static float translucentGhostBlocksAlpha = 0.5f;
}
