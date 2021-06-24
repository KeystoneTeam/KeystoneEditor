package keystone.api.wrappers;

import keystone.api.filters.KeystoneFilter;
import net.minecraft.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.function.Consumer;

/**
 * A block mask for a filter. Used to restrict operations to either only a set of blocks or
 * every block except a set of blocks
 */
public class BlockMask
{
    private static final Map<Block, Block[]> forcedBlockAdditions = new HashMap<>();
    static
    {
        forcedBlockAdditions.put(new Block(Blocks.AIR.defaultBlockState()), new Block[]
        {
                new Block(Blocks.CAVE_AIR.defaultBlockState()),
                new Block(Blocks.VOID_AIR.defaultBlockState())
        });
    }

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
            ITagCollection<net.minecraft.block.Block> tags = BlockTags.getAllTags();
            ITag<net.minecraft.block.Block> tag = tags.getTag(new ResourceLocation(block.substring(1)));
            if (tag != null)
            {
                List<net.minecraft.block.Block> blocks = tag.getValues();
                for (net.minecraft.block.Block add : blocks) with(new Block(add.defaultBlockState()));
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
        if (forcedBlockAdditions.containsKey(block))
        {
            for (Block add : forcedBlockAdditions.get(block)) if (!mask.contains(add)) mask.add(add);
        }

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
            ITagCollection<net.minecraft.block.Block> tags = BlockTags.getAllTags();
            ITag<net.minecraft.block.Block> tag = tags.getTag(new ResourceLocation(block.substring(1)));
            if (tag != null)
            {
                List<net.minecraft.block.Block> blocks = tag.getValues();
                for (net.minecraft.block.Block add : blocks) without(new Block(add.defaultBlockState()));
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
        if (forcedBlockAdditions.containsKey(block))
        {
            for (Block remove : forcedBlockAdditions.get(block)) if (mask.contains(remove)) mask.remove(remove);
        }

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
     * @return Whether this {@link keystone.api.wrappers.BlockMask} is a blacklist. If true, this will match all blocks except
     * the mask contents
     */
    public boolean isBlacklist() { return blacklist; }

    /**
     * @return Whether this {@link keystone.api.wrappers.BlockMask} is a whitelist. If true, this will match all blocks that are
     * in the mask contents
     */
    public boolean isWhitelist() { return !blacklist; }

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
