package keystone.core.modules.filter.providers;

import keystone.api.Keystone;
import keystone.api.wrappers.blocks.BlockType;
import keystone.core.registries.BlockTypeRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TagBlockProvider implements IBlockProvider
{
    private final TagKey<Block> blockTag;
    private final List<BlockState> states;

    public TagBlockProvider(TagKey<Block> blockTag)
    {
        this.blockTag = blockTag;
        List<BlockState> states = new ArrayList<>();
        Registry.BLOCK.iterateEntries(this.blockTag).forEach(entry -> states.add(entry.value().getDefaultState()));
        this.states = Collections.unmodifiableList(states);
    }

    @Override
    public BlockType get()
    {
        return BlockTypeRegistry.fromMinecraftBlock(states.get(Keystone.RANDOM.nextInt(states.size())));
    }
    @Override
    public BlockType getFirst()
    {
        return BlockTypeRegistry.fromMinecraftBlock(this.states.get(0));
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
        return blockTag.id().equals(blockProvider.blockTag.id());
    }
    @Override
    public int hashCode()
    {
        return blockTag.id().hashCode();
    }
}
