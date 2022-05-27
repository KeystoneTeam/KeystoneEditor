package keystone.core.utils;

import java.util.List;
import net.minecraft.util.math.random.Random;

public final class WeightedRandom
{
    public static class Item
    {
        public final int weight;

        public Item(int weight)
        {
            this.weight = weight;
        }
    }

    public static <T extends Item> T getRandomItem(Random random, List<T> items, int totalWeight)
    {
        int rand = random.nextInt(totalWeight);
        for (T item : items)
        {
            rand -= item.weight;
            if (rand <= 0) return item;
        }
        return items.get(0);
    }
}
