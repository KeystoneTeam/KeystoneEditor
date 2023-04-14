package keystone.api.wrappers.blocks;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import keystone.api.Keystone;
import keystone.core.registries.BlockTypeRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.nbt.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryEntryList;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
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

    private final List<BlockType> mask = new ArrayList<>();
    private final List<net.minecraft.block.Block> anyVariantMask = new ArrayList<>();
    private boolean blacklist;
    
    // region INTERNAL USE ONLY, DO NOT USE IN FILTERS
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Create the forced block additions list. Currently, this is used to bind all variants
     * of air together.
     */
    public static void buildForcedAdditionsList()
    {
        forcedBlockAdditions.put(BlockTypeRegistry.AIR, new BlockType[]
                {
                        BlockTypeRegistry.fromMinecraftBlock(Blocks.CAVE_AIR.getDefaultState()),
                        BlockTypeRegistry.fromMinecraftBlock(Blocks.VOID_AIR.getDefaultState())
                });
    }
    
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Write the contents of this mask to an NBT compound
     */
    public NbtCompound write()
    {
        NbtCompound nbt = new NbtCompound();
        
        // Blacklist Flag
        nbt.putBoolean("Blacklist", blacklist);
        
        // Mask Contents
        if (mask.size() > 0)
        {
            NbtList maskNBT = new NbtList();
            for (BlockType maskEntry : mask) maskNBT.add(NbtHelper.fromBlockState(maskEntry.getMinecraftBlock()));
            nbt.put("Mask", maskNBT);
        }
        
        // Any Variant Mask Contents
        if (anyVariantMask.size() > 0)
        {
            NbtList anyVariantNBT = new NbtList();
            for (net.minecraft.block.Block block : anyVariantMask) anyVariantNBT.add(NbtString.of(Registry.BLOCK.getId(block).toString()));
            nbt.put("AnyVariantMask", anyVariantNBT);
        }
        
        return nbt;
    }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Overwrite the contents of this mask with a mask NBT compound
     * @param nbt The NBT of the mask to overwrite this with
     */
    public void read(NbtCompound nbt)
    {
        // Blacklist Flag
        this.blacklist = nbt.getBoolean("Blacklist");
        
        // Mask Contents
        this.mask.clear();
        if (nbt.contains("Mask", NbtElement.LIST_TYPE))
        {
            NbtList maskNBT = nbt.getList("Mask", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < maskNBT.size(); i++)
            {
                BlockState state = NbtHelper.toBlockState(maskNBT.getCompound(i));
                this.mask.add(BlockTypeRegistry.fromMinecraftBlock(state));
            }
        }
        
        // Any Variant Mask Contents
        this.anyVariantMask.clear();
        if (nbt.contains("AnyVariantMask", NbtElement.LIST_TYPE))
        {
            NbtList anyVariantNBT = nbt.getList("AnyVariantMask", NbtElement.STRING_TYPE);
            for (int i = 0; i < anyVariantNBT.size(); i++) anyVariantMask.add(Registry.BLOCK.get(new Identifier(anyVariantNBT.getString(i))));
        }
    }
    // endregion
    //region API
    //region Serialization
    /**
     * Create a new {@link BlockMask} from a mask NBT compound.
     * @param nbt The NBT compound to read the mask from
     * @return The loaded {@link BlockMask}
     */
    public static BlockMask load(NbtCompound nbt)
    {
        BlockMask mask = new BlockMask();
        mask.read(nbt);
        return mask;
    }
    /**
     * Read a new {@link BlockMask} from a file.
     * @param file The {@link File} to read the mask from
     * @return The loaded {@link BlockMask}
     */
    public static BlockMask load(File file)
    {
        BlockMask mask = new BlockMask();
        mask.read(file);
        return mask;
    }
    
    /**
     * Save this {@link BlockMask} to a file.
     * @param file The {@link File} to save this mask to
     */
    public void write(File file)
    {
        try
        {
            NbtIo.write(write(), file);
        }
        catch (IOException e)
        {
            Keystone.LOGGER.error("Failed to write BlockMask to '" + file.getPath() + "'!");
            e.printStackTrace();
        }
    }
    /**
     * Read this {@link BlockMask} from a file.
     * @param file The {@link File} to read this mask from
     */
    public void read(File file)
    {
        try
        {
            read(NbtIo.read(file));
        }
        catch (IOException e)
        {
            Keystone.LOGGER.error("Failed to read BlockMask from '" + file.getPath() + "'!");
            e.printStackTrace();
        }
    }
    //endregion
    //region With
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
    //endregion
    //region Without
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
    //endregion
    //region Whitelist / Blacklist
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
    //endregion
    //region Checking
    /**
     * Check if a {@link Block} is matched by this mask
     * @param block The {@link Block} to check
     * @return Whether the {@link Block} is matched by this mask
     */
    public boolean valid(@NotNull Block block) { return valid(block.blockType()); }
    /**
     * Check if a {@link BlockType} is matched by this mask
     * @param blockType The {@link BlockType} to check
     * @return Whether the {@link BlockType} is matched by this mask
     */
    public boolean valid(@NotNull BlockType blockType)
    {
        boolean matches = mask.contains(blockType) || anyVariantMask.contains(blockType.getMinecraftBlock().getBlock());
        return matches != blacklist;
    }
    //endregion
    //region Utils
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
     * Run a function on every {@link BlockType} in the mask contents
     * @param variantConsumer The function to run on property-specific blocks
     * @param anyVariantConsumer The function to run on property agnostic blocks
     */
    public void forEach(Consumer<BlockType> variantConsumer, Consumer<net.minecraft.block.Block> anyVariantConsumer)
    {
        mask.forEach(variantConsumer);
        anyVariantMask.forEach(anyVariantConsumer);
    }
    //endregion
    //endregion
}
