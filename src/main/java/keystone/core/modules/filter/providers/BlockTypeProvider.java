package keystone.core.modules.filter.providers;

import keystone.api.wrappers.blocks.BlockType;
import keystone.core.registries.BlockTypeRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;

public class BlockTypeProvider implements IBlockProvider
{
    private BlockType blockType;
    
    private BlockTypeProvider()
    {
        this.blockType = null;
    }
    public BlockTypeProvider(BlockType blockType)
    {
        this.blockType = blockType;
    }

    @Override
    public BlockType get()
    {
        return blockType;
    }
    @Override
    public BlockType getFirst()
    {
        return blockType;
    }
    @Override
    public IBlockProvider clone()
    {
        return new BlockTypeProvider(blockType);
    }
    
    @Override public NbtCompound write() { return NbtHelper.fromBlockState(blockType.getMinecraftBlock()); }
    @Override public void read(NbtCompound nbt) { blockType = BlockTypeRegistry.fromMinecraftBlock(NbtHelper.toBlockState(nbt)); }
    
    @Override
    public int hashCode()
    {
        return blockType.hashCode();
    }
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockTypeProvider blockTypeProvider = (BlockTypeProvider) o;
        return blockType.equals(blockTypeProvider.get());
    }
}