package keystone.core.modules.filter.providers;

import keystone.api.Keystone;
import keystone.api.wrappers.blocks.BlockType;
import keystone.core.registries.BlockTypeRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntryList;

import java.util.*;

public class TagBlockProvider implements IBlockProvider
{
    private final RegistryEntryList<Block> blockTag;
    private final Map<String, String> vagueProperties;
    private final List<BlockState> states;

    public TagBlockProvider(RegistryEntryList<Block> blockTag, Map<String, String> vagueProperties)
    {
        this.blockTag = blockTag;
        List<BlockState> states = new ArrayList<>();
        // TODO: Implement vague properties
        this.blockTag.forEach(entry -> states.add(entry.value().getDefaultState()));
        this.vagueProperties = vagueProperties;
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
        return new TagBlockProvider(blockTag, vagueProperties);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagBlockProvider that = (TagBlockProvider) o;
        return states.equals(that.states);
    }

    @Override
    public int hashCode()
    {
        return states.hashCode();
    }
}
