package keystone.core;

import java.io.File;

public class KeystoneConfig
{
    public static String filtersDirectory = "keystone" + File.separator + "filters";

    public static boolean startActive = true;
    public static boolean disableInGameMenu = true;
    public static boolean debugHistoryLog = false;

    public static double closeSelectDistance = 4.0;

    public static boolean renderSphereAsDots = false;
}
