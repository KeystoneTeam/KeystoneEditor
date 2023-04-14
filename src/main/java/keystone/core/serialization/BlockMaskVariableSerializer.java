package keystone.core.serialization;

import keystone.api.wrappers.blocks.BlockMask;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class BlockMaskVariableSerializer implements VariableSerializer<BlockMask>
{
    @Override
    public void write(String name, BlockMask value, NbtCompound nbt)
    {
        nbt.put(name, value.write());
    }
    
    @Override
    public BlockMask read(String name, NbtCompound nbt)
    {
        if (nbt.contains(name, NbtElement.COMPOUND_TYPE)) return BlockMask.load(nbt);
        else return null;
    }
}
