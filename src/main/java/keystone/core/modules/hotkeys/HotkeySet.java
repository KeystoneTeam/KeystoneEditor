package keystone.core.modules.hotkeys;

import java.util.*;
import java.util.function.BiConsumer;

public class HotkeySet
{
    private final String name;
    private final int priority;
    private final Map<List<Integer>, Hotkey> hotkeys;
    
    public HotkeySet(String name) { this(name, 0); }
    public HotkeySet(String name, int priority)
    {
        this.name = name;
        this.priority = priority;
        this.hotkeys = new HashMap<>();
    }
    
    public Hotkey getHotkey(Integer... keys)
    {
        List<Integer> keyList = new ArrayList<>();
        Collections.addAll(keyList, keys);
        return hotkeys.computeIfAbsent(keyList, (list) -> new Hotkey(priority, keys));
    }
    public void removeHotkey(Integer... keys)
    {
        List<Integer> keyList = new ArrayList<>();
        Collections.addAll(keyList, keys);
        hotkeys.remove(keyList);
    }
    public void clear()
    {
        hotkeys.clear();
    }
    
    public String getName() { return name; }
    public int getPriority() { return priority; }
    public void forEach(BiConsumer<List<Integer>, Hotkey> consumer) { hotkeys.forEach(consumer); }
    
    @Override
    public String toString()
    {
        return "HotkeySet{name=" + name + ", priority=" + priority + ", hotkeys=" + hotkeys.size() + "}";
    }
}
