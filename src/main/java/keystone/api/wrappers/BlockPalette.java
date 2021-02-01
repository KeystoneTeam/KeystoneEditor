package keystone.api.wrappers;

import keystone.api.Keystone;
import keystone.api.filters.KeystoneFilter;
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

    public BlockPalette with(String block) { return with(KeystoneFilter.block(block), 1); }
    public BlockPalette with(String block, int weight) { return with(KeystoneFilter.block(block), weight); }

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
    public void forEach(BiConsumer<Block, Integer> consumer) { palette.forEach(entry -> consumer.accept(entry.block, entry.itemWeight)); }
}
