package keystone.core.modules.history;

public interface IHistoryEntry
{
    void undo();
    void redo();

    default boolean addToUnsavedChanges() { return true; }
    default void onPushToHistory(HistoryModule history, boolean beforePush) {  }
}
