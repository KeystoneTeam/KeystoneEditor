package keystone.core.modules.filter.providers;

import keystone.api.wrappers.blocks.BlockType;

public class BlockTypeProvider implements IBlockProvider
{
    private final BlockType blockType;

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