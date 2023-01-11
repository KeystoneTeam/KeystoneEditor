package keystone.core.modules.hotkeys;

import keystone.api.Keystone;
import keystone.core.events.minecraft.InputEvents;
import keystone.core.modules.IKeystoneModule;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class HotkeysModule implements IKeystoneModule
{
    private final Map<String, HotkeySet> hotkeySets = new HashMap<>();
    private final ConcurrentMap<List<Integer>, Hotkey> currentHotkeys = new ConcurrentHashMap<>();
    private final List<Integer> currentlyPressedKeys = new ArrayList<>();
    
    @Override public boolean isEnabled() { return true; }
    @Override public void postInit()
    {
        InputEvents.KEY_EVENT.register(this::onKey);
        addHotkeySet(HotkeySets.DEFAULT);
    }
    
    @Override
    public void resetModule()
    {
        hotkeySets.clear();
        currentHotkeys.clear();
        
        addHotkeySet(HotkeySets.DEFAULT);
    }
    
    public void addHotkeySet(HotkeySet hotkeySet)
    {
        if (hotkeySet != null)
        {
            hotkeySets.put(hotkeySet.getName(), hotkeySet);
            rebuildCurrentHotkeys();
        }
    }
    public void removeHotkeySet(HotkeySet hotkeySet)
    {
        if (hotkeySet != null && hotkeySets.containsKey(hotkeySet.getName()))
        {
            hotkeySets.remove(hotkeySet.getName());
            rebuildCurrentHotkeys();
        }
    }
    
    private void rebuildCurrentHotkeys()
    {
        currentHotkeys.clear();
        for (HotkeySet hotkeySet : hotkeySets.values())
        {
            hotkeySet.forEach((keys, hotkey) ->
            {
                if (!currentHotkeys.containsKey(keys) || hotkey.getPriority() < currentHotkeys.get(keys).getPriority()) currentHotkeys.put(keys, hotkey);
            });
        }
    }
    private boolean onKey(int key, int action, int scancode, int modifiers)
    {
        if (action == GLFW.GLFW_PRESS)
        {
            currentlyPressedKeys.add(key);
            if (Keystone.isActive())
            {
                boolean hotkeyPassed = false;
                for (Hotkey hotkey : currentHotkeys.values()) if (hotkey.test(currentlyPressedKeys)) hotkeyPassed = true;
                return hotkeyPassed;
            }
        }
        else if (action == GLFW.GLFW_RELEASE) currentlyPressedKeys.remove((Integer) key);
        
        return false;
    }
}
