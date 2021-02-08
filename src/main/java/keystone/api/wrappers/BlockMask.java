package keystone.api.wrappers;

import keystone.api.filters.KeystoneFilter;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A block mask for a filter. Used to restrict operations to either only a set of blocks or
 * every block except a set of blocks
 */
public class BlockMask
{
    private List<Block> mask = new ArrayList<>();
    private boolean blacklist;

    /**
     * Create a new {@link keystone.api.wrappers.BlockMask} with the same contents as this one
     * @return The cloned {@link keystone.api.wrappers.BlockMask}
     */
    public BlockMask clone()
    {
        BlockMask clone = new BlockMask();
        for (Block block : mask) clone.mask.add(new Block(block.getMinecraftBlock(), block.getTileEntityData()));
        clone.blacklist = blacklist;
        return clone;
    }

    /**
     * Add a block ID to the mask. Any ID that is a valid ID for the /setblock command will work. [e.g. "minecraft:stone_slab[type=top]"]
     * @param block The block ID to add
     * @return The modified {@link keystone.api.wrappers.BlockMask}
     */
    public BlockMask with(String block)
    {
        if (block.startsWith("#"))
        {
            ITagCollection<net.minecraft.block.Block> tags = BlockTags.getCollection();
            ITag<net.minecraft.block.Block> tag = tags.get(new ResourceLocation(block.substring(1)));
            if (tag != null)
            {
                List<net.minecraft.block.Block> blocks = tag.getAllElements();
                for (net.minecraft.block.Block add : blocks) with(new Block(add.getDefaultState()));
            }
            return this;
        }
        else return with(KeystoneFilter.block(block));
    }
    /**
     * Add a {@link keystone.api.wrappers.Block} to the mask
     * @param block The {@link keystone.api.wrappers.Block} to add
     * @return The modified {@link keystone.api.wrappers.BlockMask}
     */
    public BlockMask with(Block block)
    {
        if (!mask.contains(block)) mask.add(block);
        return this;
    }

    /**
     * Remove a block ID from the mask. Any ID that is a valid ID for the /setblock command will work. [e.g. "minecraft:stone_slab[type=top]"]
     * @param block The block ID to remove
     * @return the modified {@link keystone.api.wrappers.BlockMask}
     */
    public BlockMask without(String block)
    {
        if (block.startsWith("#"))
        {
            ITagCollection<net.minecraft.block.Block> tags = BlockTags.getCollection();
            ITag<net.minecraft.block.Block> tag = tags.get(new ResourceLocation(block.substring(1)));
            if (tag != null)
            {
                List<net.minecraft.block.Block> blocks = tag.getAllElements();
                for (net.minecraft.block.Block add : blocks) without(new Block(add.getDefaultState()));
            }
            return this;
        }
        else return without(KeystoneFilter.block(block));
    }
    /**
     * Remove a {@link keystone.api.wrappers.Block} from the mask
     * @param block The {@link keystone.api.wrappers.Block} to remove
     * @return The modified {@link keystone.api.wrappers.BlockMask}
     */
    public BlockMask without(Block block)
    {
        if (mask.contains(block)) mask.remove(block);
        return this;
    }

    /**
     * Mark this {@link keystone.api.wrappers.BlockMask} as a blacklist. This will match all blocks except the mask contents
     * @return The modified {@link keystone.api.wrappers.BlockMask}
     */
    public BlockMask blacklist()
    {
        this.blacklist = true;
        return this;
    }

    /**
     * Mark this {@link keystone.api.wrappers.BlockMask} as a whitelist. This will match all blocks that are in the mask contents
     * @return The modified {@link keystone.api.wrappers.BlockMask}
     */
    public BlockMask whitelist()
    {
        this.blacklist = false;
        return this;
    }

    /**
     * Check if a {@link keystone.api.wrappers.Block} is matched by this mask
     * @param block The {@link keystone.api.wrappers.Block} to check
     * @return Whether the {@link keystone.api.wrappers.Block} is matched by this mask
     */
    public boolean valid(Block block) { return mask.contains(block) != blacklist; }

    /**
     * Run a function on every {@link keystone.api.wrappers.Block} in the mask contents
     * @param consumer The function to run
     */
    public void forEach(Consumer<Block> consumer) { mask.forEach(block -> consumer.accept(block)); }
}
