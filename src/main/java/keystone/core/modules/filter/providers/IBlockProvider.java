package keystone.core.modules.filter.providers;

import keystone.api.wrappers.blocks.BlockType;
import net.minecraft.nbt.NbtCompound;

public interface IBlockProvider
{
    BlockType get();
    BlockType getFirst();
    IBlockProvider clone();
    
    NbtCompound write();
    void read(NbtCompound nbt);
}