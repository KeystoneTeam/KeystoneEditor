package keystone.api.wrappers;

import keystone.api.Keystone;
import keystone.api.filters.KeystoneFilter;
import net.minecraft.data.BlockTagsProvider;
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

public class BlockPalette
{
    private class PaletteEntry extends WeightedRandom.Item
    {
        public final Block block;

        public PaletteEntry(Block block, int weight)
        {
            super(weight);
            this.block = block;
        }
    }

    private List<PaletteEntry> palette = new ArrayList<>();
    private Map<Block, PaletteEntry> weights = new HashMap<>();
    private int totalWeight;

    public BlockPalette with(String block) { return with(block, 1); }
    public BlockPalette with(String block, int weight)
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
        else return with(KeystoneFilter.block(block), weight);
    }

    public BlockPalette with(Block block) { return with(block, 1); }
    public BlockPalette with(Block block, int weight)
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

    public Block randomBlock()
    {
        return WeightedRandom.getRandomItem(Keystone.RANDOM, palette, totalWeight).block;
    }
    public int randomIndex()
    {
        PaletteEntry entry = WeightedRandom.getRandomItem(Keystone.RANDOM, palette, totalWeight);
        return palette.indexOf(entry);
    }
    public Block getBlock(int index)
    {
        while (index >= palette.size()) index -= palette.size();
        return palette.get(index).block;
    }
    public void forEach(BiConsumer<Block, Integer> consumer) { palette.forEach(entry -> consumer.accept(entry.block, entry.itemWeight)); }
}
