package keystone.core.keybinds.conflicts;

public interface IKeyConditionContainer
{
    void addCondition(IKeyCondition condition);
    void removeCondition(IKeyCondition condition);
    void clearConditions();
    boolean testConditions();
}
