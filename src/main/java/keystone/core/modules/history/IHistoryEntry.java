package keystone.core.modules.history;

import net.minecraft.nbt.CompoundNBT;

public interface IHistoryEntry
{
    void undo();
    void redo();
    String id();
    void serialize(CompoundNBT nbt);
    void deserialize(CompoundNBT nbt);

    default boolean addToUnsavedChanges() { return true; }
    default void onPushToHistory(HistoryModule history, boolean beforePush) {  }
}
