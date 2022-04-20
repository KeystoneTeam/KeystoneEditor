package keystone.core.keybinds;

import keystone.core.keybinds.conflicts.IKeyCondition;
import keystone.core.keybinds.conflicts.IKeyConditionContainer;
import net.minecraft.client.option.KeyBinding;

public final class KeyBindingUtils
{
    public static void addConditions(KeyBinding keyBinding, IKeyCondition... conditions)
    {
        IKeyConditionContainer container = (IKeyConditionContainer)keyBinding;
        for (IKeyCondition condition : conditions) container.addCondition(condition);
    }
    public static void removeCondition(KeyBinding keyBinding, IKeyCondition... conditions)
    {
        IKeyConditionContainer container = (IKeyConditionContainer)keyBinding;
        for (IKeyCondition condition : conditions) container.addCondition(condition);
    }
    public static void clearConditions(KeyBinding keyBinding)
    {
        ((IKeyConditionContainer)keyBinding).clearConditions();
    }
    public static boolean testConditions(KeyBinding keyBinding)
    {
        return ((IKeyConditionContainer)keyBinding).testConditions();
    }
}
