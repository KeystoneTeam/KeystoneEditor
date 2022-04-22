package keystone.api.wrappers.blocks;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import keystone.api.Keystone;
import keystone.api.filters.KeystoneFilter;
import keystone.core.registries.BlockTypeRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A block mask for a filter. Used to restrict operations to either only a set of blocks or
 * every block except a set of blocks
 */
public class BlockMask
{
    private static final Map<BlockType, BlockType[]> forcedBlockAdditions = new HashMap<>();
    public static void buildForcedAdditionsList()
    {
        forcedBlockAdditions.put(BlockTypeRegistry.AIR, new BlockType[]
        {
                BlockTypeRegistry.fromMinecraftBlock(Blocks.CAVE_AIR.getDefaultState()),
                BlockTypeRegistry.fromMinecraftBlock(Blocks.VOID_AIR.getDefaultState())
        });
    }

    private List<BlockType> mask = new ArrayList<>();
    private boolean blacklist;

    /**
     * Create a new {@link BlockMask} with the same contents as this one
     * @return The cloned {@link BlockMask}
     */
    public BlockMask clone()
    {
        BlockMask clone = new BlockMask();
        clone.mask.addAll(mask);
        clone.blacklist = blacklist;
        return clone;
    }

    /**
     * Add a block ID to the mask. Any ID that is a valid ID for the /setblock command will work. [e.g. "minecraft:stone_slab[type=top]"]
     * @param block The block ID to add
     * @return The modified {@link BlockMask}
     */
    public BlockMask with(String block)
    {
        if (block.startsWith("#"))
        {
            try
            {
                BlockArgumentParser parser = new BlockArgumentParser(new StringReader(block), true).parse(false);
                Registry.BLOCK.iterateEntries(parser.getTagId()).forEach(tagEntry -> with(BlockTypeRegistry.fromMinecraftBlock(tagEntry.value().getDefaultState())));
            }
            catch (CommandSyntaxException e)
            {
                Keystone.abortFilter(e.getLocalizedMessage());
            }

            return this;
        }
        else return with(KeystoneFilter.block(block).blockType());
    }
    /**
     * Add a {@link BlockType} to the mask
     * @param blockType The {@link BlockType} to add
     * @return The modified {@link BlockMask}
     */
    public BlockMask with(BlockType blockType)
    {
        if (forcedBlockAdditions.containsKey(blockType))
        {
            for (BlockType add : forcedBlockAdditions.get(blockType)) if (!mask.contains(add)) mask.add(add);
        }

        if (!mask.contains(blockType)) mask.add(blockType);
        return this;
    }

    /**
     * Remove a block ID from the mask. Any ID that is a valid ID for the /setblock command will work. [e.g. "minecraft:stone_slab[type=top]"]
     * @param block The block ID to remove
     * @return the modified {@link BlockMask}
     */
    public BlockMask without(String block)
    {
        if (block.startsWith("#"))
        {
            try
            {
                BlockArgumentParser parser = new BlockArgumentParser(new StringReader(block), true).parse(false);
                Registry.BLOCK.iterateEntries(parser.getTagId()).forEach(tagEntry -> without(BlockTypeRegistry.fromMinecraftBlock(tagEntry.value().getDefaultState())));
            }
            catch (CommandSyntaxException e)
            {
                Keystone.abortFilter(e.getLocalizedMessage());
            }

            return this;
        }
        else return without(KeystoneFilter.block(block).blockType());
    }
    /**
     * Remove a {@link BlockType} from the mask
     * @param blockType The {@link BlockType} to remove
     * @return The modified {@link BlockMask}
     */
    public BlockMask without(BlockType blockType)
    {
        if (forcedBlockAdditions.containsKey(blockType))
        {
            for (BlockType remove : forcedBlockAdditions.get(blockType)) if (mask.contains(remove)) mask.remove(remove);
        }

        if (mask.contains(blockType)) mask.remove(blockType);
        return this;
    }

    /**
     * Mark this {@link BlockMask} as a blacklist. This will match all blocks except the mask contents
     * @return The modified {@link BlockMask}
     */
    public BlockMask blacklist()
    {
        this.blacklist = true;
        return this;
    }

    /**
     * Mark this {@link BlockMask} as a whitelist. This will match all blocks that are in the mask contents
     * @return The modified {@link BlockMask}
     */
    public BlockMask whitelist()
    {
        this.blacklist = false;
        return this;
    }

    /**
     * @return Whether this {@link BlockMask} is a blacklist. If true, this will match all blocks except
     * the mask contents
     */
    public boolean isBlacklist() { return blacklist; }

    /**
     * @return Whether this {@link BlockMask} is a whitelist. If true, this will match all blocks that are
     * in the mask contents
     */
    public boolean isWhitelist() { return !blacklist; }

    /**
     * Check if a {@link BlockType} is matched by this mask
     * @param blockType The {@link BlockType} to check
     * @return Whether the {@link BlockType} is matched by this mask
     */
    public boolean valid(@Nonnull BlockType blockType) { return mask.isEmpty() || mask.contains(blockType) != blacklist; }

    /**
     * Check if a {@link Block} is matched by this mask
     * @param block The {@link Block} to check
     * @return Whether the {@link Block} is matched by this mask
     */
    public boolean valid(@Nonnull Block block) { return valid(block.blockType()); }

    /**
     * Run a function on every {@link BlockType} in the mask contents
     * @param consumer The function to run
     */
    public void forEach(Consumer<BlockType> consumer) { mask.forEach(block -> consumer.accept(block)); }
}
