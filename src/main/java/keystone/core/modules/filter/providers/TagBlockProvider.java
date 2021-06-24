package keystone.core.modules.filter.providers;

import keystone.api.Keystone;
import keystone.api.wrappers.Block;
import net.minecraft.tags.ITag;

public class TagBlockProvider implements IBlockProvider
{
    private final ITag.INamedTag<net.minecraft.block.Block> blockTag;

    public TagBlockProvider(ITag.INamedTag<net.minecraft.block.Block> blockTag)
    {
        this.blockTag = blockTag;
    }

    @Override
    public Block get()
    {
        return new Block(this.blockTag.getRandomElement(Keystone.RANDOM).defaultBlockState());
    }
    @Override
    public Block getFirst()
    {
        return new Block(this.blockTag.getValues().get(0).defaultBlockState());
    }
    @Override
    public IBlockProvider clone()
    {
        return new TagBlockProvider(blockTag);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagBlockProvider blockProvider = (TagBlockProvider) o;
        return blockTag.getName().equals(blockProvider.blockTag.getName());
    }
    @Override
    public int hashCode()
    {
        return blockTag.getName().hashCode();
    }
}
