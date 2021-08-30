package keystone.core.modules.filter.providers;

import keystone.api.Keystone;
import keystone.api.wrappers.blocks.BlockType;
import keystone.core.registries.BlockTypeRegistry;
import net.minecraft.tags.ITag;

public class TagBlockProvider implements IBlockProvider
{
    private final ITag.INamedTag<net.minecraft.block.Block> blockTag;

    public TagBlockProvider(ITag.INamedTag<net.minecraft.block.Block> blockTag)
    {
        this.blockTag = blockTag;
    }

    @Override
    public BlockType get()
    {
        return BlockTypeRegistry.fromMinecraftBlock(this.blockTag.getRandomElement(Keystone.RANDOM).defaultBlockState());
    }
    @Override
    public BlockType getFirst()
    {
        return BlockTypeRegistry.fromMinecraftBlock(this.blockTag.getValues().get(0).defaultBlockState());
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
