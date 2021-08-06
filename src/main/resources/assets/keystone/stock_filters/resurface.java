import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.variables.IntRange;
import keystone.api.variables.Variable;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.blocks.BlockMask;
import keystone.api.wrappers.blocks.BlockPalette;

public class Resurface extends KeystoneFilter
{
    @Variable BlockMask airMask = whitelist("minecraft:air");
    @Variable BlockMask surfaceMask = blacklist();
    @Variable BlockPalette surfacePalette = palette("minecraft:white_stained_glass");
    @Variable @IntRange(min = 1, max = 7) int depth = 3;

    public void processBlock(int x, int y, int z, WorldRegion region)
    {
        Block block = region.getBlock(x, y, z);
        if (airMask.valid(block) || !surfaceMask.valid(block)) return;

        for (int i = -depth; i <= depth; i++)
        {
            for (int j = -depth; j <= depth; j++)
            {
                for (int k = -depth; k <= depth; k++)
                {
                    Block original = region.getBlock(x + i, y + j, z + k);
                    if (airMask.valid(original))
                    {
                        region.setBlock(x, y, z, surfacePalette);
                        return;
                    }
                }
            }
        }
    }
}
