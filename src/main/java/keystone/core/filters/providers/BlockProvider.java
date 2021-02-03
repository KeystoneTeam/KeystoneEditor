package keystone.core.filters.providers;

import keystone.api.wrappers.Block;

public class BlockProvider implements IBlockProvider
{
    private final Block block;

    public BlockProvider(Block block)
    {
        this.block = block;
    }

    @Override
    public Block get()
    {
        return block;
    }
    @Override
    public Block getFirst()
    {
        return block;
    }
    @Override
    public IBlockProvider clone()
    {
        return new BlockProvider(new Block(block.getMinecraftBlock(), block.getTileEntityData()));
    }

    @Override
    public int hashCode()
    {
        return block.hashCode();
    }
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockProvider blockProvider = (BlockProvider) o;
        return block.equals(blockProvider.get());
    }
}