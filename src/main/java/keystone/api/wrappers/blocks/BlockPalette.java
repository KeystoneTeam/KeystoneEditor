package keystone.api.wrappers.blocks;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import keystone.api.Keystone;
import keystone.core.modules.filter.blocks.BlockProviderTypes;
import keystone.core.modules.filter.blocks.BlockTypeProvider;
import keystone.core.modules.filter.blocks.IBlockProvider;
import keystone.core.modules.filter.blocks.BlockListProvider;
import keystone.core.registries.BlockTypeRegistry;
import keystone.core.utils.WeightedRandom;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A weighted palette of {@link BlockType Blocks} for a filter. Used to set a block to
 * a random state from a list
 */
public class BlockPalette
{
    private static class PaletteEntry extends WeightedRandom.Item
    {
        public final IBlockProvider blockProvider;

        public PaletteEntry(IBlockProvider provider, int weight)
        {
            super(weight);
            this.blockProvider = provider;
        }
    }

    private final List<PaletteEntry> palette = new ArrayList<>();
    private final Map<IBlockProvider, PaletteEntry> weights = new HashMap<>();
    private int totalWeight;
    
    //region INTERNAL USE ONLY, DO NOT USE IN FILTERS
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Write the contents of this palette to an NBT compound
     */
    public NbtCompound write()
    {
        NbtCompound nbt = new NbtCompound();
        
        // Palette
        NbtList paletteNBT = new NbtList();
        for (PaletteEntry entry : palette)
        {
            NbtCompound entryNBT = new NbtCompound();
            
            // Block Provider
            NbtCompound providerNBT = entry.blockProvider.write();
            providerNBT.putString("ID", BlockProviderTypes.getID(entry.blockProvider).toString());
            entryNBT.put("BlockProvider", providerNBT);
            
            // Weight
            entryNBT.putInt("Weight", entry.weight);
            
            paletteNBT.add(entryNBT);
        }
        nbt.put("Palette", paletteNBT);
        
        return nbt;
    }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Overwrite the contents of this palette with a palette NBT compound
     * @param nbt The NBT of the palette to overwrite this with
     */
    public void read(NbtCompound nbt)
    {
        // Reset Contents
        palette.clear();
        weights.clear();
        totalWeight = 0;
        
        // Palette
        NbtList paletteNBT = nbt.getList("Palette", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < paletteNBT.size(); i++)
        {
            NbtCompound entryNBT = paletteNBT.getCompound(i);
            
            // Block Provider
            NbtCompound providerNBT = entryNBT.getCompound("BlockProvider");
            Identifier providerTypeID = new Identifier(providerNBT.getString("ID"));
            IBlockProvider provider = BlockProviderTypes.createFromID(providerTypeID);
            provider.read(providerNBT);
            
            // Weight
            int weight = entryNBT.getInt("Weight");
            
            // Add Palette Entry
            PaletteEntry entry = new PaletteEntry(provider, weight);
            palette.add(entry);
            weights.put(provider, entry);
            totalWeight += weight;
        }
    }
    //endregion
    //region API
    //region Serialization
    /**
     * Create a new {@link BlockPalette} from a palette NBT compound.
     * @param nbt The NBT compound to read the palette from
     * @return The loaded {@link BlockPalette}
     */
    public static BlockPalette load(NbtCompound nbt)
    {
        BlockPalette palette = new BlockPalette();
        palette.read(nbt);
        return palette;
    }
    /**
     * Read a new {@link BlockPalette} from a file.
     * @param file The {@link File} to read the palette from
     * @return The loaded {@link BlockPalette}
     */
    public static BlockPalette load(File file)
    {
        BlockPalette palette = new BlockPalette();
        palette.read(file);
        return palette;
    }
    
    /**
     * Save this {@link BlockPalette} to a file.
     * @param file The {@link File} to save this palette to
     */
    public void write(File file)
    {
        try
        {
            NbtIo.write(write(), file);
        }
        catch (IOException e)
        {
            Keystone.LOGGER.error("Failed to write BlockPalette to '" + file.getPath() + "'!");
            e.printStackTrace();
        }
    }
    /**
     * Read this {@link BlockPalette} from a file.
     * @param file The {@link File} to read this palette from
     */
    public void read(File file)
    {
        try
        {
            read(NbtIo.read(file));
        }
        catch (IOException e)
        {
            Keystone.LOGGER.error("Failed to read BlockPalette from '" + file.getPath() + "'!");
            e.printStackTrace();
        }
    }
    //endregion
    //region With
    /**
     * Add a block ID to the palette. Any ID that is a valid ID for the /setblock command will work. Add a number
     * to the end to specify the block's weight in the palette, with a higher weight being more likely to be
     * chosen. [e.g. "minecraft:stone_slab[type=top] 10"]
     * @param block The block ID and optional weight
     * @return The modified {@link BlockPalette}
     */
    public BlockPalette with(String block)
    {
        String[] tokens = block.split(" ");
        if (tokens.length == 1) return with(tokens[0], 1);
        else
        {
            String blockStr = tokens[0];
            for (int i = 1; i < tokens.length - 1; i++) blockStr += tokens[i];

            try
            {
                int weight = Integer.parseInt(tokens[1]);
                return with(blockStr, weight);
            }
            catch (NumberFormatException e)
            {
                blockStr += tokens[tokens.length - 1];
                return with(blockStr, 1);
            }
        }
    }
    /**
     * Add a block ID to the palette with a given weight, with a higher weight being more likely to be
     * chosen. Any ID that is a valid ID for the /setblock command will work
     * @param block The block ID
     * @param weight The weight
     * @return The modified {@link BlockPalette}
     */
    public BlockPalette with(String block, int weight)
    {
        try
        {
            Either<BlockArgumentParser.BlockResult, BlockArgumentParser.TagResult> parser = BlockArgumentParser.blockOrTag(Registry.BLOCK, block, false);
            if (parser.left().isPresent()) return with(BlockTypeRegistry.fromMinecraftBlock(parser.left().get().blockState()), weight);
            if (parser.right().isPresent()) return with(new BlockListProvider(parser.right().get().tag(), parser.right().get().vagueProperties()), weight);
        }
        catch (CommandSyntaxException e)
        {
            e.printStackTrace();
        }
        return this;
    }
    /**
     * Add a {@link BlockType} to the palette with a weight of 1
     * @param blockType The {@link BlockType} top add
     * @return The modified {@link BlockPalette}
     */
    public BlockPalette with(BlockType blockType) { return with(blockType, 1); }
    /**
     * Add a {@link BlockType} to the palette with a given weight. A higher weight is more
     * likely to be chosen
     * @param blockType The {@link BlockType} to add
     * @param weight The weight of the block
     * @return The modified {@link BlockPalette}
     */
    public BlockPalette with(BlockType blockType, int weight) { return with(new BlockTypeProvider(blockType), weight); }
    /**
     * Add an {@link IBlockProvider} to the palette with a weight of 1
     * @param block The {@link IBlockProvider} to add
     * @return The modified {@link BlockPalette}
     */
    public BlockPalette with(IBlockProvider block) { return with(block, 1); }
    /**
     * Add am {@link IBlockProvider} to the palette with a given weight. A
     * higher weight is more likely to be chosen
     * @param block The {@link IBlockProvider} to add
     * @param weight The weight of the block provider
     * @return The modified {@link BlockPalette}
     */
    public BlockPalette with(IBlockProvider block, int weight)
    {
        if (block == null) return this;

        if (weights.containsKey(block))
        {
            PaletteEntry old = weights.get(block);
            palette.remove(old);
            weights.remove(old);
            totalWeight -= old.weight;
            weight += old.weight;
        }

        PaletteEntry entry = new PaletteEntry(block, weight);
        palette.add(entry);
        weights.put(block, entry);
        totalWeight += weight;

        return this;
    }
    //endregion
    //region Without
    /**
     * Remove a block ID from the palette. Any ID that is a valid ID for the /setblock command will work. Add a number
     * to the end to specify the weight to remove from the block in the palette instead of automatically removing it.
     * [e.g. "minecraft:stone_slab[type=top] 10"]
     * @param block The block ID and optional weight
     * @return The modified {@link BlockPalette}
     */
    public BlockPalette without(String block)
    {
        String[] tokens = block.split(" ");
        if (tokens.length == 1) return without(tokens[0], Integer.MAX_VALUE);
        else
        {
            String blockStr = tokens[0];
            for (int i = 1; i < tokens.length - 1; i++) blockStr += tokens[i];

            try
            {
                int weight = Integer.parseInt(tokens[1]);
                return without(blockStr, weight);
            }
            catch (NumberFormatException e)
            {
                blockStr += tokens[tokens.length - 1];
                return without(blockStr, Integer.MAX_VALUE);
            }
        }
    }
    /**
     * Remove weight from a block in the palette. Any ID that is a valid ID for the /setblock command will work. If the
     * remaining weight is zero or less, the entry will be removed. [e.g. "minecraft:stone_slab[type=top] 10"]
     * @param block The block ID
     * @param weight The weight to remove
     * @return The modified {@link BlockPalette}
     */
    public BlockPalette without(String block, int weight)
    {
        try
        {
            Either<BlockArgumentParser.BlockResult, BlockArgumentParser.TagResult> parser = BlockArgumentParser.blockOrTag(Registry.BLOCK, block, false);
            if (parser.left().isPresent()) return without(BlockTypeRegistry.fromMinecraftBlock(parser.left().get().blockState()), weight);
            if (parser.right().isPresent()) return without(new BlockListProvider(parser.right().get().tag(), parser.right().get().vagueProperties()), weight);
        }
        catch (CommandSyntaxException e)
        {
            e.printStackTrace();
        }
        return this;
    }
    /**
     * Remove a {@link BlockType} from the palette
     * @param blockType The {@link BlockType} to remove
     * @return The modified {@link BlockPalette}
     */
    public BlockPalette without(BlockType blockType) { return without(blockType, Integer.MAX_VALUE); }
    /**
     * Remove weight from a {@link BlockType} in the palette. If the remaining weight is zero or less,
     * the entry will be removed
     * @param blockType The {@link BlockType} to effect
     * @param weight The weight to remove
     * @return The modified {@link BlockPalette}
     */
    public BlockPalette without(BlockType blockType, int weight) { return without(new BlockTypeProvider(blockType), weight); }
    /**
     * Remove an {@link IBlockProvider} from the palette
     * @param block The {@link IBlockProvider} to remove
     * @return The modified {@link BlockPalette}
     */
    public BlockPalette without(IBlockProvider block)
    {
        return without(block, Integer.MAX_VALUE);
    }
    /**
     * Remove weight from an {@link IBlockProvider} in the palette. If the remaining
     * weight is zero or less, the entry will be removed
     * @param block The {@link IBlockProvider} to effect
     * @param weight The weight to remove
     * @return The modified {@link BlockPalette}
     */
    public BlockPalette without(IBlockProvider block, int weight)
    {
        if (weights.containsKey(block))
        {
            PaletteEntry old = weights.get(block);
            palette.remove(old);
            weights.remove(block);
            totalWeight -= old.weight;

            if (old.weight > weight)
            {
                int newWeight = old.weight - weight;
                PaletteEntry newEntry = new PaletteEntry(block, newWeight);
                palette.add(newEntry);
                weights.put(block, newEntry);
                totalWeight += newWeight;
            }
        }
        return this;
    }
    //endregion
    //region Contains
    /**
     * Check if the palette contains a {@link BlockType}
     * @param blockType The {@link BlockType} to check
     * @return Whether the palette contains the block
     */
    public boolean contains(BlockType blockType)
    {
        return contains(new BlockTypeProvider(blockType));
    }
    /**
     * Check if the palette contains an {@link IBlockProvider}
     * @param provider The {@link IBlockProvider} to check
     * @return Whether the palette contains the block provider
     */
    public boolean contains(IBlockProvider provider)
    {
        return this.weights.containsKey(provider);
    }
    //endregion
    //region Resolving
    /**
     * @return A random {@link BlockType} from the palette, or air if empty
     */
    public BlockType randomBlock()
    {
        if (palette.size() == 0) return BlockTypeRegistry.AIR;
        return WeightedRandom.getRandomItem(Keystone.RANDOM, palette, totalWeight).blockProvider.get();
    }
    /**
     * @return A random index from the palette
     */
    public int randomIndex()
    {
        if (palette.size() == 0)
        {
            Keystone.tryCancelFilter("Cannot get random index from empty BlockPalette!");
            return 0;
        }

        PaletteEntry entry = WeightedRandom.getRandomItem(Keystone.RANDOM, palette, totalWeight);
        return palette.indexOf(entry);
    }
    /**
     * Get a {@link BlockType} at a given index in the palette
     * @param index The index
     * @return The block at the given index
     */
    public BlockType getBlockType(int index)
    {
        if (palette.size() == 0)
        {
            Keystone.tryCancelFilter("Cannot get block at index from empty BlockPalette!");
            return BlockTypeRegistry.AIR;
        }

        while (index >= palette.size()) index -= palette.size();
        return palette.get(index).blockProvider.get();
    }
    /**
     * Run a function on every entry in the palette
     * @param consumer The function to run
     */
    public void forEach(BiConsumer<IBlockProvider, Integer> consumer) { palette.forEach(entry -> consumer.accept(entry.blockProvider, entry.weight)); }
    //endregion
    //region Utils
    /**
     * Create a new {@link BlockPalette} with the same contents as this one
     * @return The cloned {@link BlockPalette}
     */
    public BlockPalette clone()
    {
        BlockPalette clone = new BlockPalette();
        for (PaletteEntry entry : palette)
        {
            PaletteEntry entryClone = new PaletteEntry(entry.blockProvider.clone(), entry.weight);
            clone.palette.add(entryClone);
            clone.weights.put(entryClone.blockProvider, entryClone);
        }
        clone.totalWeight = totalWeight;
        return clone;
    }
    //endregion
    //endregion
}
