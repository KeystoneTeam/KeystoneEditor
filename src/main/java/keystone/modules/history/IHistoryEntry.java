package keystone.modules.history;

public interface IHistoryEntry
{
    void undo();
    void redo();
    default boolean addToUnsavedChanges() { return true; }
}
