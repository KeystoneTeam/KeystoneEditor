import keystone.api.filters.FilterBox;
import keystone.api.filters.Variable;
import keystone.api.filters.IntRange;
import keystone.api.wrappers.Block;
import keystone.api.wrappers.BlockPalette;

import java.util.Random;

public class Forester extends KeystoneFilter
{
    @Variable BlockMask groundMask = whitelist("minecraft:grass_block", "minecraft:podzol", "minecraft:mycelium");
    @Variable BlockPalette logPalette = palette("minecraft:oak_log", "minecraft:spruce_log", "minecraft:birch_log", "minecraft:jungle_log", "minecraft:acacia_log", "minecraft:dark_oak_log");
    @Variable BlockPalette leavesPalette = palette("minecraft:oak_leaves", "minecraft:spruce_leaves", "minecraft:birch_leaves", "minecraft:jungle_leaves", "minecraft:acacia_leaves", "minecraft:dark_oak_leaves");
    @Variable boolean pairLogsAndLeaves = true;
    @Variable @IntRange(min = 1, scrollStep = 25) int treeRarity = 100;
    @Variable int seed = 0;

    private Random random;
    private float treeChance;

    public void prepare()
    {
        treeChance = 1.0f / treeRarity;

        if (seed == 0) random = new Random();
        else random = new Random(seed);
    }

    public void processBox(FilterBox box)
    {
        for (int x = box.getMin().getX(); x <= box.getMax().getX(); x++)
        {
            for (int z = box.getMin().getZ(); z <= box.getMax().getZ(); z++)
            {
                if (random.nextFloat() >= treeChance) continue;

                int y = box.getTopBlock(x, z);
                if (!groundMask.valid(box.getBlock(x, y, z))) continue;

                if (pairLogsAndLeaves)
                {
                    Block[] blocks = resolvePalettes(logPalette, leavesPalette);
                    generateTree(x, y + 1, z, blocks[0], blocks[1], box);
                }
                else generateTree(x, y + 1, z, logPalette.randomBlock(), leavesPalette.randomBlock(), box);
            }
        }
    }

    private void generateTree(int x, int y, int z, Block log, Block leaves, FilterBox box)
    {
        int height = 4 + random.nextInt(3);
        for (int i = 0; i < height; i++) box.setBlock(x, y + i, z, log);
    }
}