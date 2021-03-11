package keystone.api.wrappers;

import keystone.api.Keystone;
import keystone.api.filters.KeystoneFilter;
import keystone.core.modules.filter.providers.BlockProvider;
import keystone.core.modules.filter.providers.IBlockProvider;
import keystone.core.modules.filter.providers.TagBlockProvider;
import net.minecraft.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A weighted palette of {@link keystone.api.wrappers.Block Blocks} for a filter. Used to set a block to
 * a random state from a list
 */
public class BlockPalette
{
    private class PaletteEntry extends WeightedRandom.Item
    {
        public final IBlockProvider blockProvider;

        public PaletteEntry(IBlockProvider provider, int weight)
        {
            super(weight);
            this.blockProvider = provider;
        }
    }

    private List<PaletteEntry> palette = new ArrayList<>();
    private Map<IBlockProvider, PaletteEntry> weights = new HashMap<>();
    private int totalWeight;

    /**
     * Create a new {@link keystone.api.wrappers.BlockPalette} with the same contents as this one
     * @return The cloned {@link keystone.api.wrappers.BlockPalette}
     */
    public BlockPalette clone()
    {
        BlockPalette clone = new BlockPalette();
        for (PaletteEntry entry : palette)
        {
            PaletteEntry entryClone = new PaletteEntry(entry.blockProvider.clone(), entry.itemWeight);
            clone.palette.add(entryClone);
            clone.weights.put(entryClone.blockProvider, entryClone);
        }
        clone.totalWeight = totalWeight;
        return clone;
    }

    //region With
    /**
     * Add a block ID to the palette. Any ID that is a valid ID for the /setblock command will work. Add a number
     * to the end to specify the block's weight in the palette, with a higher weight being more likely to be
     * chosen. [e.g. "minecraft:stone_slab[type=top] 10"]
     * @param block The block ID and optional weight
     * @return The modified {@link keystone.api.wrappers.BlockPalette}
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
     * @return The modified {@link keystone.api.wrappers.BlockPalette}
     */
    public BlockPalette with(String block, int weight)
    {
        if (block.startsWith("#"))
        {
            ITagCollection<net.minecraft.block.Block> tags = BlockTags.getCollection();
            ITag<net.minecraft.block.Block> tag = tags.get(new ResourceLocation(block.substring(1)));
            if (tag != null && tag instanceof ITag.INamedTag) with(new TagBlockProvider((ITag.INamedTag<net.minecraft.block.Block>)tag), weight);
            return this;
        }
        else return with(KeystoneFilter.block(block), weight);
    }
    /**
     * Add a {@link keystone.api.wrappers.Block} to the palette with a weight of 1
     * @param block The {@link keystone.api.wrappers.Block} top add
     * @return The modified {@link keystone.api.wrappers.BlockPalette}
     */
    public BlockPalette with(Block block) { return with(block, 1); }
    /**
     * Add a {@link keystone.api.wrappers.Block} to the palette with a given weight. A higher weight is more
     * likely to be chosen
     * @param block The {@link keystone.api.wrappers.Block} to add
     * @param weight The weight of the block
     * @return The modified {@link keystone.api.wrappers.BlockPalette}
     */
    public BlockPalette with(Block block, int weight) { return with(new BlockProvider(block), weight); }
    /**
     * Add an {@link keystone.core.modules.filter.providers.IBlockProvider} to the palette with a weight of 1
     * @param block The {@link keystone.core.modules.filter.providers.IBlockProvider} to add
     * @return The modified {@link keystone.api.wrappers.BlockPalette}
     */
    public BlockPalette with(IBlockProvider block) { return with(block, 1); }
    /**
     * Add am {@link keystone.core.modules.filter.providers.IBlockProvider} to the palette with a given weight. A
     * higher weight is more likely to be chosen
     * @param block The {@link keystone.core.modules.filter.providers.IBlockProvider} to add
     * @param weight The weight of the block provider
     * @return The modified {@link keystone.api.wrappers.BlockPalette}
     */
    public BlockPalette with(IBlockProvider block, int weight)
    {
        if (weights.containsKey(block))
        {
            PaletteEntry old = weights.get(block);
            palette.remove(old);
            weights.remove(old);
            totalWeight -= old.itemWeight;
            weight += old.itemWeight;
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
     * @return The modified {@link keystone.api.wrappers.BlockPalette}
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
     * @return The modified {@link keystone.api.wrappers.BlockPalette}
     */
    public BlockPalette without(String block, int weight)
    {
        if (block.startsWith("#"))
        {
            ITagCollection<net.minecraft.block.Block> tags = BlockTags.getCollection();
            ITag<net.minecraft.block.Block> tag = tags.get(new ResourceLocation(block.substring(1)));
            if (tag != null && tag instanceof ITag.INamedTag) without(new TagBlockProvider((ITag.INamedTag<net.minecraft.block.Block>)tag), weight);
            return this;
        }
        else return without(KeystoneFilter.block(block), weight);
    }
    /**
     * Remove a {@link keystone.api.wrappers.Block} from the palette
     * @param block The {@link keystone.api.wrappers.Block} to remove
     * @return The modified {@link keystone.api.wrappers.BlockPalette}
     */
    public BlockPalette without(Block block) { return without(block, Integer.MAX_VALUE); }
    /**
     * Remove weight from a {@link keystone.api.wrappers.Block} in the palette. If the remaining weight is zero or less,
     * the entry will be removed
     * @param block The {@link keystone.api.wrappers.Block} to effect
     * @param weight The weight to remove
     * @return The modified {@link keystone.api.wrappers.BlockPalette}
     */
    public BlockPalette without(Block block, int weight) { return without(new BlockProvider(block), weight); }
    /**
     * Remove an {@link keystone.core.modules.filter.providers.IBlockProvider} from the palette
     * @param block The {@link keystone.core.modules.filter.providers.IBlockProvider} to remove
     * @return The modified {@link keystone.api.wrappers.BlockPalette}
     */
    public BlockPalette without(IBlockProvider block)
    {
        return without(block, Integer.MAX_VALUE);
    }
    /**
     * Remove weight from an {@link keystone.core.modules.filter.providers.IBlockProvider} in the palette. If the remaining
     * weight is zero or less, the entry will be removed
     * @param block The {@link keystone.core.modules.filter.providers.IBlockProvider} to effect
     * @param weight The weight to remove
     * @return The modified {@link keystone.api.wrappers.BlockPalette}
     */
    public BlockPalette without(IBlockProvider block, int weight)
    {
        if (weights.containsKey(block))
        {
            PaletteEntry old = weights.get(block);
            palette.remove(old);
            weights.remove(block);
            totalWeight -= old.itemWeight;

            if (old.itemWeight > weight)
            {
                int newWeight = old.itemWeight - weight;
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
     * Check if the palette contains a {@link keystone.api.wrappers.Block}
     * @param block The {@link keystone.api.wrappers.Block} to check
     * @return Whether the palette contains the block
     */
    public boolean contains(Block block)
    {
        return contains(new BlockProvider(block));
    }
    /**
     * Check if the palette contains an {@link keystone.core.modules.filter.providers.IBlockProvider}
     * @param provider The {@link keystone.core.modules.filter.providers.IBlockProvider} to check
     * @return Whether the palette contains the block provider
     */
    public boolean contains(IBlockProvider provider)
    {
        return this.weights.containsKey(provider);
    }
    //endregion

    /**
     * @return A random {@link keystone.api.wrappers.Block} from the palette
     */
    public Block randomBlock()
    {
        if (palette.size() == 0)
        {
            Keystone.abortFilter("Cannot get block from empty BlockPalette!");
            return new Block(Blocks.AIR.getDefaultState());
        }
        return WeightedRandom.getRandomItem(Keystone.RANDOM, palette, totalWeight).blockProvider.get();
    }
    /**
     * @return A random index from the palette
     */
    public int randomIndex()
    {
        if (palette.size() == 0)
        {
            Keystone.abortFilter("Cannot get random index from empty BlockPalette!");
            return 0;
        }

        PaletteEntry entry = WeightedRandom.getRandomItem(Keystone.RANDOM, palette, totalWeight);
        return palette.indexOf(entry);
    }
    /**
     * Get a {@link keystone.api.wrappers.Block} at a given index in the palette
     * @param index The index
     * @return The block at the given index
     */
    public Block getBlock(int index)
    {
        if (palette.size() == 0)
        {
            Keystone.abortFilter("Cannot get block at index from empty BlockPalette!");
            return new Block(Blocks.AIR.getDefaultState());
        }

        while (index >= palette.size()) index -= palette.size();
        return palette.get(index).blockProvider.get();
    }
    /**
     * Run a function on every entry in the palette
     * @param consumer The function to run
     */
    public void forEach(BiConsumer<IBlockProvider, Integer> consumer) { palette.forEach(entry -> consumer.accept(entry.blockProvider, entry.itemWeight)); }
}
