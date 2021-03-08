import keystone.api.BlockRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.variables.Variable;
import keystone.api.variables.IntRange;
import keystone.api.wrappers.Block;
import keystone.api.wrappers.BlockMask;
import keystone.api.wrappers.BlockPalette;

import java.util.Random;

public enum TreeType
{
    SMALL_OAK,
    SMALL_SPRUCE
}

public class Forester extends KeystoneFilter
{
    @Variable BlockMask groundMask = whitelist("minecraft:grass_block");
    @Variable BlockPalette logPalette = palette("minecraft:oak_log");
    @Variable BlockPalette leavesPalette = palette("minecraft:oak_leaves");
    @Variable boolean pairLogsAndLeaves = true;
    @Variable @IntRange(min = 25, scrollStep = 25) int treeRarity = 100;
    @Variable TreeType treeType = TreeType.SMALL_OAK;
    @Variable int seed = 0;

    private Random random;
    private float treeChance;

    //region Filter Overrides
    public boolean allowBlocksOutsideRegion()
    {
        return true;
    }
    public void prepare()
    {
        treeChance = 1.0f / treeRarity;

        if (seed == 0) random = new Random();
        else random = new Random(seed);
    }
    public void processRegion(BlockRegion region)
    {
        for (int x = region.min.x; x <= region.max.x; x++)
        {
            for (int z = region.min.z; z <= region.max.z; z++)
            {
                if (random.nextFloat() >= treeChance) continue;

                int y = region.getTopBlock(x, z);
                if (!groundMask.valid(region.getBlock(x, y, z))) continue;

                if (pairLogsAndLeaves)
                {
                    Block[] blocks = resolvePalettes(logPalette, leavesPalette);
                    generateTree(x, y + 1, z, blocks[0], blocks[1], region);
                }
                else generateTree(x, y + 1, z, logPalette.randomBlock(), leavesPalette.randomBlock(), region);
            }
        }
    }
    //endregion
    private void generateTree(int x, int y, int z, Block log, Block leaves, BlockRegion box)
    {
        int height;

        switch (treeType)
        {
            case SMALL_OAK:
                height = 4 + random.nextInt(3);
                generateStraightTrunk(x, y, z, height, log, box);
                generateBlobLeaves(x, y + height, z, leaves, box);
                return;
            case SMALL_SPRUCE:
                height = 5 + random.nextInt(4);
                generateStraightTrunk(x, y, z, height, log, box);
                generateSmallSpruceLeaves(x, y, z, height, leaves, box);
                return;
        }
    }

    //region Trunks
    private void generateStraightTrunk(int x, int y, int z, int height, Block log, BlockRegion box)
    {
        for (int i = 0; i < height; i++) box.setBlock(x, y + i, z, log);
    }
    //endregion
    //region Leaves
    private void generateBlobLeaves(int x, int y, int z, Block leaves, BlockRegion box)
    {
        box.setBlock(x, y, z, leaves);
        box.setBlock(x + 1, y, z, leaves);
        box.setBlock(x - 1, y, z, leaves);
        box.setBlock(x, y, z + 1, leaves);
        box.setBlock(x, y, z - 1, leaves);

        y--;
        box.setBlock(x + 1, y, z, leaves);
        box.setBlock(x - 1, y, z, leaves);
        box.setBlock(x, y, z + 1, leaves);
        box.setBlock(x, y, z - 1, leaves);
        if (random.nextBoolean()) box.setBlock(x + 1, y, z + 1, leaves);
        if (random.nextBoolean()) box.setBlock(x + 1, y, z - 1, leaves);
        if (random.nextBoolean()) box.setBlock(x - 1, y, z + 1, leaves);
        if (random.nextBoolean()) box.setBlock(x - 1, y, z - 1, leaves);

        for (int k = 0; k < 2; k++)
        {
            y--;
            for (int i = -2; i <= 2; i++) for (int j = -1; j <= 1; j++) if (i != 0 || j != 0) box.setBlock(x + i, y, z + j, leaves);
            for (int i = -1; i <= 1; i++) for (int j = -2; j <= 2; j++) if (i != 0 || j != 0) box.setBlock(x + i, y, z + j, leaves);
            if (random.nextBoolean()) box.setBlock(x + 2, y, z + 2, leaves);
            if (random.nextBoolean()) box.setBlock(x + 2, y, z - 2, leaves);
            if (random.nextBoolean()) box.setBlock(x - 2, y, z + 2, leaves);
            if (random.nextBoolean()) box.setBlock(x - 2, y, z - 2, leaves);
        }
    }
    private void generateSmallSpruceLeaves(int x, int y, int z, int height, Block leaves, BlockRegion box)
    {
        int radius = 1;
        for (int i = height, j = 0; i > 0; i--, j++)
        {
            if (i % 2 == height % 2 || i == height - 1)
            {
                for (int k = -radius; k <= radius; k++)
                {
                    for (int l = -radius; l <= radius; l++)
                    {
                        if (i != height && k == 0 && l == 0) continue;

                        double distance = Math.sqrt(k * k + l * l);
                        if (distance <= radius - 0.1) box.setBlock(x + k, y + i, z + l, leaves);
                        else if (distance <= radius + 0.25 && random.nextBoolean()) box.setBlock(x + k, y + i, z + l, leaves);
                    }
                }
            }
            else
            {
                for (int k = -radius; k <= radius; k++)
                {
                    for (int l = -radius; l <= radius; l++)
                    {
                        if (i != height && k == 0 && l == 0) continue;

                        double distance = Math.sqrt(k * k + l * l);
                        if (distance <= radius - 1.5) box.setBlock(x + k, y + i, z + l, leaves);
                        else if (distance <= radius - 0.5 && random.nextBoolean()) box.setBlock(x + k, y + i, z + l, leaves);
                    }
                }
            }

            if (j % 2 == 0) radius++;
        }
    }
    //endregion
}