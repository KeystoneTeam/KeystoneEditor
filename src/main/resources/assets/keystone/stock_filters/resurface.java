import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.variables.IntRange;
import keystone.api.variables.Variable;
import keystone.api.wrappers.blocks.BlockMask;
import keystone.api.wrappers.blocks.BlockPalette;
import keystone.api.wrappers.blocks.BlockType;

public class Resurface extends KeystoneFilter
{
    @Variable BlockMask airMask = whitelist("minecraft:air");
    @Variable BlockMask surfaceMask = blacklist();
    @Variable BlockPalette surfacePalette = palette("minecraft:white_stained_glass");
    @Variable @IntRange(min = 1, max = 7) int depth = 3;

    public void processBlock(int x, int y, int z, WorldRegion region)
    {
        BlockType blockType = region.getBlockType(x, y, z);
        if (airMask.valid(blockType) || !surfaceMask.valid(blockType)) return;

        for (int i = -depth; i <= depth; i++)
        {
            for (int j = -depth; j <= depth; j++)
            {
                for (int k = -depth; k <= depth; k++)
                {
                    BlockType original = region.getBlockType(x + i, y + j, z + k);
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
