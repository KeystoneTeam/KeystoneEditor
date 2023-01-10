package keystone.core.keybinds.hotkeys;

import keystone.api.Keystone;
import keystone.core.events.minecraft.InputEvents;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public final class HotkeyManager
{
    private static final List<Integer> currentKeys = new ArrayList<>();
    private static final Stack<Map<List<Integer>, Hotkey>> hotkeyStack = new Stack<>();
    
    public static void init()
    {
        InputEvents.KEY_EVENT.register(HotkeyManager::onKey);
        hotkeyStack.add(new HashMap<>());
        
        HotkeySet.DEFAULT.addHotkeys();
    }
    
    public static void push() { push(true); }
    public static void push(boolean copyPreviousHotkeys)
    {
        Map<List<Integer>, Hotkey> hotkeys = new HashMap<>();
        if (copyPreviousHotkeys)
        {
            for (Map.Entry<List<Integer>, Hotkey> hotkey : hotkeyStack.peek().entrySet()) hotkeys.put(hotkey.getKey(), hotkey.getValue().copy());
            hotkeys.putAll(hotkeyStack.peek());
        }
        hotkeyStack.push(hotkeys);
    }
    public static void pop()
    {
        if (hotkeyStack.size() <= 1) Keystone.LOGGER.error("Trying to pop the hotkey stack when there are no frames to pop! Are you sure you called HotkeyManager.push first?");
        else hotkeyStack.pop();
    }
    public static Hotkey getHotkey(Integer... keys)
    {
        List<Integer> keysList = new ArrayList<>(keys.length);
        Collections.addAll(keysList, keys);
        return hotkeyStack.peek().computeIfAbsent(keysList, list -> new Hotkey(keys));
    }
    public static void removeHotkey(Integer... keys)
    {
        List<Integer> keysList = new ArrayList<>(keys.length);
        Collections.addAll(keysList, keys);
        hotkeyStack.peek().remove(keysList);
    }
    public static void clearHotkeys()
    {
        hotkeyStack.peek().clear();
    }
    
    private static boolean onKey(int key, int action, int scancode, int modifiers)
    {
        if (action == GLFW.GLFW_PRESS)
        {
            currentKeys.add(key);
            if (Keystone.isActive())
            {
                boolean hotkeyPassed = false;
                for (Hotkey hotkey : hotkeyStack.peek().values()) if (hotkey.test(currentKeys)) hotkeyPassed = true;
                return hotkeyPassed;
            }
        }
        else if (action == GLFW.GLFW_RELEASE) currentKeys.clear();
        
        return false;
    }
}
