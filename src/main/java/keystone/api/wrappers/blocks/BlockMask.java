package keystone.api.wrappers.blocks;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import keystone.core.registries.BlockTypeRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryEntryList;

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
    private List<net.minecraft.block.Block> anyVariantMask = new ArrayList<>();
    private boolean blacklist;

    /**
     * Create a new {@link BlockMask} with the same contents as this one
     *
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
     *
     * @param block The block ID to add
     * @return The modified {@link BlockMask}
     */
    public BlockMask with(String block)
    {
        try
        {
            Either<BlockArgumentParser.BlockResult, BlockArgumentParser.TagResult> parser = BlockArgumentParser.blockOrTag(Registry.BLOCK, block, false);
            if (parser.left().isPresent()) return with(BlockTypeRegistry.fromMinecraftBlock(parser.left().get().blockState()));
            if (parser.right().isPresent())
            {
                RegistryEntryList<net.minecraft.block.Block> tag = parser.right().get().tag();
                for (RegistryEntry<net.minecraft.block.Block> tagElement : tag) with(BlockTypeRegistry.fromMinecraftBlock(tagElement.value().getDefaultState()));
                return this;
            }
        } catch (CommandSyntaxException e)
        {
            e.printStackTrace();
        }
        return this;
    }
    /**
     * Add a {@link BlockType} to the mask
     *
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
     * Add a property agnostic block ID to the mask. Any valid block ID or block tag ID will work. Blocks added in this way will
     * match any variant of the block. For example, adding "minecraft:stone_slab" will match "minecraft:stone_slab[type=top]" and
     * "minecraft:stone_slab[type=bottom]"
     *
     * @param block The block ID to add
     * @return The modified {@link BlockMask}
     */
    public BlockMask withAllVariants(String block)
    {
        try
        {
            Either<BlockArgumentParser.BlockResult, BlockArgumentParser.TagResult> parser = BlockArgumentParser.blockOrTag(Registry.BLOCK, block, false);
            if (parser.left().isPresent()) return withAllVariants(BlockTypeRegistry.fromMinecraftBlock(parser.left().get().blockState()));
            if (parser.right().isPresent())
            {
                RegistryEntryList<net.minecraft.block.Block> tag = parser.right().get().tag();
                for (RegistryEntry<net.minecraft.block.Block> tagElement : tag) withAllVariants(BlockTypeRegistry.fromMinecraftBlock(tagElement.value().getDefaultState()));
                return this;
            }
        } catch (CommandSyntaxException e)
        {
            e.printStackTrace();
        }
        return this;
    }
    /**
     * Add a property agnostic {@link BlockType} to the mask.  Blocks added in this way will match any variant of the block. For example,
     * adding "minecraft:stone_slab" will match "minecraft:stone_slab[type=top]" and "minecraft:stone_slab[type=bottom]"
     * @param blockType The {@link BlockType} to add
     * @return The modified {@link BlockMask}
     */
    public BlockMask withAllVariants(BlockType blockType)
    {
        if (forcedBlockAdditions.containsKey(blockType))
        {
            for (BlockType add : forcedBlockAdditions.get(blockType)) if (!mask.contains(add)) mask.add(add);
        }

        if (!anyVariantMask.contains(blockType.getMinecraftBlock().getBlock())) anyVariantMask.add(blockType.getMinecraftBlock().getBlock());
        return this;
    }

    /**
     * Remove a block ID from the mask. Any ID that is a valid ID for the /setblock command will work. [e.g. "minecraft:stone_slab[type=top]"]
     * @param block The block ID to remove
     * @return the modified {@link BlockMask}
     */
    public BlockMask without(String block)
    {
        try
        {
            Either<BlockArgumentParser.BlockResult, BlockArgumentParser.TagResult> parser = BlockArgumentParser.blockOrTag(Registry.BLOCK, block, false);
            if (parser.left().isPresent()) return without(BlockTypeRegistry.fromMinecraftBlock(parser.left().get().blockState()));
            if (parser.right().isPresent())
            {
                RegistryEntryList<net.minecraft.block.Block> tag = parser.right().get().tag();
                for (RegistryEntry<net.minecraft.block.Block> tagElement : tag) without(BlockTypeRegistry.fromMinecraftBlock(tagElement.value().getDefaultState()));
                return this;
            }
        }
        catch (CommandSyntaxException e)
        {
            e.printStackTrace();
        }
        return this;
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
            for (BlockType remove : forcedBlockAdditions.get(blockType)) mask.remove(remove);
        }

        mask.remove(blockType);
        return this;
    }

    /**
     * Add a property agnostic block ID to the mask. Any valid block ID or block tag ID will work. Blocks added in this way will
     * match any variant of the block. For example, adding "minecraft:stone_slab" will match "minecraft:stone_slab[type=top]" and
     * "minecraft:stone_slab[type=bottom]"
     *
     * @param block The block ID to add
     * @return The modified {@link BlockMask}
     */
    public BlockMask withoutAllVariants(String block)
    {
        try
        {
            Either<BlockArgumentParser.BlockResult, BlockArgumentParser.TagResult> parser = BlockArgumentParser.blockOrTag(Registry.BLOCK, block, false);
            if (parser.left().isPresent()) return withoutAllVariants(BlockTypeRegistry.fromMinecraftBlock(parser.left().get().blockState()));
            if (parser.right().isPresent())
            {
                RegistryEntryList<net.minecraft.block.Block> tag = parser.right().get().tag();
                for (RegistryEntry<net.minecraft.block.Block> tagElement : tag) withoutAllVariants(BlockTypeRegistry.fromMinecraftBlock(tagElement.value().getDefaultState()));
                return this;
            }
        } catch (CommandSyntaxException e)
        {
            e.printStackTrace();
        }
        return this;
    }
    /**
     * Add a property agnostic {@link BlockType} to the mask.  Blocks added in this way will match any variant of the block. For example,
     * adding "minecraft:stone_slab" will match "minecraft:stone_slab[type=top]" and "minecraft:stone_slab[type=bottom]"
     * @param blockType The {@link BlockType} to add
     * @return The modified {@link BlockMask}
     */
    public BlockMask withoutAllVariants(BlockType blockType)
    {
        if (forcedBlockAdditions.containsKey(blockType))
        {
            for (BlockType add : forcedBlockAdditions.get(blockType)) mask.remove(add);
        }

        anyVariantMask.remove(blockType.getMinecraftBlock().getBlock());
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
     * Check if a {@link Block} is matched by this mask
     * @param block The {@link Block} to check
     * @return Whether the {@link Block} is matched by this mask
     */
    public boolean valid(@Nonnull Block block) { return valid(block.blockType()); }
    /**
     * Check if a {@link BlockType} is matched by this mask
     * @param blockType The {@link BlockType} to check
     * @return Whether the {@link BlockType} is matched by this mask
     */
    public boolean valid(@Nonnull BlockType blockType)
    {
        boolean matches = mask.contains(blockType) || anyVariantMask.contains(blockType.getMinecraftBlock().getBlock());
        return matches != blacklist;
    }

    /**
     * Run a function on every {@link BlockType} in the mask contents
     * @param variantConsumer The function to run on property-specific blocks
     * @param anyVariantConsumer The function to run on property agnostic blocks
     */
    public void forEach(Consumer<BlockType> variantConsumer, Consumer<net.minecraft.block.Block> anyVariantConsumer)
    {
        mask.forEach(variantConsumer);
        anyVariantMask.forEach(anyVariantConsumer);
    }
}
