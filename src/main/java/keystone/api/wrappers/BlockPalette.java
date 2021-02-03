package keystone.api.wrappers;

import keystone.api.Keystone;
import keystone.api.filters.KeystoneFilter;
import keystone.api.tools.interfaces.IBlockTool;
import keystone.core.filters.providers.BlockProvider;
import keystone.core.filters.providers.IBlockProvider;
import keystone.core.filters.providers.TagBlockProvider;
import net.minecraft.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedRandom;

import java.util.*;
import java.util.function.BiConsumer;

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

    public BlockPalette clone()
    {
        BlockPalette clone = new BlockPalette();
        for (Map.Entry<IBlockProvider, PaletteEntry> entry : weights.entrySet())
        {
            PaletteEntry entryClone = new PaletteEntry(entry.getValue().blockProvider.clone(), entry.getValue().itemWeight);
            clone.palette.add(entryClone);
            clone.weights.put(entryClone.blockProvider, entryClone);
        }
        clone.totalWeight = totalWeight;
        return clone;
    }

    //region With
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
    public BlockPalette with(Block block) { return with(block, 1); }
    public BlockPalette with(Block block, int weight) { return with(new BlockProvider(block), weight); }
    public BlockPalette with(IBlockProvider block) { return with(block, 1); }
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
    public BlockPalette without(Block block) { return without(block, Integer.MAX_VALUE); }
    public BlockPalette without(Block block, int weight) { return without(new BlockProvider(block), weight); }
    public BlockPalette without(IBlockProvider block)
    {
        return without(block, Integer.MAX_VALUE);
    }
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
    public boolean contains(Block block)
    {
        return contains(new BlockProvider(block));
    }
    public boolean contains(IBlockProvider provider)
    {
        return this.weights.containsKey(provider);
    }
    //endregion

    public Block randomBlock()
    {
        if (palette.size() == 0)
        {
            Keystone.abortFilter("Cannot get block from empty BlockPalette!");
            return new Block(Blocks.AIR.getDefaultState());
        }
        return WeightedRandom.getRandomItem(Keystone.RANDOM, palette, totalWeight).blockProvider.get();
    }
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
    public Block getBlock(int index)
    {
        if (palette.size() == 0)
        {
            Keystone.abortFilter("Cannot get block from empty BlockPalette!");
            return new Block(Blocks.AIR.getDefaultState());
        }

        while (index >= palette.size()) index -= palette.size();
        return palette.get(index).blockProvider.get();
    }
    public void forEach(BiConsumer<IBlockProvider, Integer> consumer) { palette.forEach(entry -> consumer.accept(entry.blockProvider, entry.itemWeight)); }
}
