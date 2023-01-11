package keystone.core.modules.hotkeys;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Hotkey
{
    private final List<Integer> keys;
    private final List<Runnable> listeners;
    private final List<Supplier<Boolean>> conditions;
    private final int priority;
    
    public Hotkey(int priority, Integer... keys)
    {
        this.keys = List.of(keys);
        this.listeners = new ArrayList<>();
        this.conditions = new ArrayList<>();
        this.priority = priority;
    }
    public Hotkey copy()
    {
        Hotkey copy = new Hotkey(priority, keys.toArray(Integer[]::new));
        copy.listeners.addAll(listeners);
        copy.conditions.addAll(conditions);
        return copy;
    }
    
    public int getPriority() { return priority; }
    
    public Hotkey addListener(Runnable listener) { listeners.add(listener); return this; }
    public Hotkey addCondition(Supplier<Boolean> condition) { conditions.add(condition); return this; }
    
    public Hotkey clearListeners() { listeners.clear(); return this; }
    public Hotkey clearConditions() { conditions.clear(); return this; }
    public Hotkey clear()
    {
        listeners.clear();
        conditions.clear();
        return this;
    }
    
    public boolean test(List<Integer> currentKeys)
    {
        // Check Keys
        if (!keys.equals(currentKeys)) return false;
        
        // Check Conditions
        for (Supplier<Boolean> condition : conditions) if (!condition.get()) return false;
        
        // Trigger Listeners
        for (Runnable listener : listeners) listener.run();
        return true;
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Hotkey{keys=(");
        for (int i = 0; i < keys.size() - 1; i++) builder.append(keys.get(i)).append(" ");
        builder.append(keys.get(keys.size() - 1));
        builder.append("), conditions=").append(conditions.size());
        builder.append(", listeners=").append(listeners.size()).append("}");
        return builder.toString();
    }
}
