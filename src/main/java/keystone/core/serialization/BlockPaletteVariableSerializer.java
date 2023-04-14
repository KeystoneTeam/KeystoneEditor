package keystone.core.serialization;

import keystone.api.wrappers.blocks.BlockPalette;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class BlockPaletteVariableSerializer implements VariableSerializer<BlockPalette>
{
    @Override
    public void write(String name, BlockPalette value, NbtCompound nbt)
    {
        nbt.put(name, value.write());
    }
    
    @Override
    public BlockPalette read(String name, NbtCompound nbt)
    {
        if (nbt.contains(name, NbtElement.COMPOUND_TYPE)) return BlockPalette.load(nbt);
        else return null;
    }
}
