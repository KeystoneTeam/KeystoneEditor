package keystone.core;

import java.io.File;

public class KeystoneConfig
{
    public static String filtersDirectory = "keystone" + File.separator + "filters";
    public static String stockFilterCache = "keystone" + File.separator + "stock_filters";
    public static String schematicsDirectory = "keystone" + File.separator + "schematics";

    public static boolean startActive = true;
    public static boolean disableInGameMenu = true;
    public static boolean debugHistoryLog = false;

    public static int clickThreshold = 200;
    public static int dragThresholdSqr = 8 * 8;
    public static int maxBrushSize = 512;
}
