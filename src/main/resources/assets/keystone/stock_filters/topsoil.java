import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.variables.IntRange;
import keystone.api.variables.Variable;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.blocks.BlockMask;
import keystone.api.wrappers.blocks.BlockPalette;

public class Topsoil extends KeystoneFilter
{
    @Variable @IntRange(min = 1) int minimumAir = 1;
    @Variable @IntRange(min = 1) int minimumDepth = 1;
    @Variable @IntRange(min = 0) int depthVariance = 0;
    @Variable @IntRange(min = 0) int yOffset = 0;
    @Variable BlockMask surfaceMask = blacklist();
    @Variable BlockMask airMask = whitelist("minecraft:air");
    @Variable BlockPalette surfacePalette = palette("minecraft:grass_block");
    @Variable BlockPalette depthPalette = palette("minecraft:dirt");
    @Variable BlockPalette foliagePalette = palette("minecraft:air 4", "minecraft:grass 1");

    @Override
    public void processBlock(int x, int y, int z, WorldRegion region)
    {
        // Surface Check
        Block block = region.getBlock(x, y, z);
        if (!surfaceMask.valid(block)) return;

        // Minimum Air Check
        for (int airCheck = 1; airCheck <= minimumAir; airCheck++)
        {
            block = region.getBlock(x, y + airCheck, z);
            if (!airMask.valid(block)) return;
        }

        // Place Topsoil
        int depth = minimumDepth;
        region.setBlock(x, y + yOffset, z, surfacePalette);
        region.setBlock(x, y + yOffset + 1, z, foliagePalette);
        for (int dy = 1; dy < depth; dy++) if (!airMask.valid(region.getBlock(x, y + yOffset - dy, z))) region.setBlock(x, y + yOffset - dy, z, depthPalette);
    }
}