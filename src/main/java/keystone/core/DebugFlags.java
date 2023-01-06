package keystone.core;

import keystone.api.KeystoneDirectories;
import keystone.core.events.minecraft.InputEvents;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public final class DebugFlags
{
    private static final String fileName = "debugFlags.txt";
    private static final Set<String> flags = new HashSet<>();
    
    public static void init()
    {
        InputEvents.KEY_PRESSED.register(DebugFlags::reloadHotkeyListener);
        reload();
    }
    
    private static void reloadHotkeyListener(int key, int action, int scancode, int modifiers)
    {
        // Reload debug flags with Ctrl+R
        if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_R && modifiers == GLFW.GLFW_MOD_CONTROL) reload();
    }
    
    public static void reload()
    {
        flags.clear();
    
        File flagsFile = KeystoneDirectories.getKeystoneDirectory().toPath().resolve(fileName).toFile();
        if (flagsFile.exists())
        {
            try (Scanner scanner = new Scanner(flagsFile))
            {
                while (scanner.hasNextLine())
                {
                    String flag = scanner.nextLine().trim();
                    if (flag.length() != 0) flags.add(flag);
                }
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }
    }
    public static boolean isFlagSet(String flag)
    {
        return flags.contains(flag);
    }
}
