package keystone.core.keybinds.hotkeys;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Hotkey
{
    private final Integer[] keys;
    private final List<Runnable> listeners;
    private final List<Supplier<Boolean>> conditions;
    
    public Hotkey(Integer... keys)
    {
        this.keys = keys;
        this.listeners = new ArrayList<>();
        this.conditions = new ArrayList<>();
    }
    public Hotkey copy()
    {
        Hotkey copy = new Hotkey(keys);
        copy.listeners.addAll(listeners);
        copy.conditions.addAll(conditions);
        return copy;
    }
    
    public Hotkey addListener(Runnable listener) { listeners.add(listener); return this; }
    public Hotkey clearListeners() { listeners.clear(); return this; }
    
    public Hotkey addCondition(Supplier<Boolean> condition) { conditions.add(condition); return this; }
    public Hotkey clearConditions() { conditions.clear(); return this; }
    
    public boolean test(List<Integer> currentKeys)
    {
        // Check Keys
        if (currentKeys.size() != keys.length) return false;
        for (int i = 0; i < keys.length; i++) if (!currentKeys.get(i).equals(keys[i])) return false;
        
        // Check Conditions
        for (Supplier<Boolean> condition : conditions) if (!condition.get()) return false;
        
        // Trigger Listeners
        for (Runnable listener : listeners) listener.run();
        return true;
    }
}
