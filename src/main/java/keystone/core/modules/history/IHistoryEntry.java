package keystone.core.modules.history;

import net.minecraft.nbt.NbtCompound;

public interface IHistoryEntry
{
    void apply();
    String id();
    void serialize(NbtCompound nbt);
    void deserialize(NbtCompound nbt);

    default boolean addToUnsavedChanges() { return true; }
    default void onPushToHistory(HistoryModule history, boolean beforePush) {  }
}
