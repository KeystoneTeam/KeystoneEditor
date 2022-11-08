import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.variables.IntRange;
import keystone.api.variables.Tooltip;
import keystone.api.variables.Variable;
import keystone.api.wrappers.blocks.BlockMask;
import keystone.api.wrappers.blocks.BlockPalette;
import keystone.api.wrappers.blocks.BlockType;

public class Topsoil extends KeystoneFilter
{
    @Tooltip("The minimum number of Air Mask blocks above a block required to count as topsoil.")
    @Variable @IntRange(min = 1) int minimumAir = 1;
    
    @Tooltip("The depth to perform the topsoil operation to.")
    @Variable @IntRange(min = 1) int depth = 1;
    
    @Tooltip("The distance to shift the topsoil operation up or down. Positive numbers add new block layers.")
    @Variable @IntRange(min = 0) int yOffset = 0;
    
    @Tooltip("Only blocks matching this will perform topsoil operations.")
    @Variable BlockMask surfaceMask = blacklist();
    
    @Tooltip("The blocks that represent empty space.")
    @Variable BlockMask airMask = whitelist("minecraft:air");
    
    @Tooltip("The palette to use for the uppermost layer of solid topsoil blocks.")
    @Variable BlockPalette surfacePalette = palette("minecraft:grass_block");
    
    @Tooltip("The palette to use for layers beneath the surface layer.")
    @Variable BlockPalette depthPalette = palette("minecraft:dirt");
    
    @Tooltip("The palette to use for a special layer placed above the surface layer.")
    @Variable BlockPalette foliagePalette = palette("minecraft:air 4", "minecraft:grass 1");

    @Override
    public void processBlock(int x, int y, int z, WorldRegion region)
    {
        // Surface Check
        BlockType blockType = region.getBlockType(x, y, z);
        if (!surfaceMask.valid(blockType)) return;

        // Minimum Air Check
        for (int airCheck = 1; airCheck <= minimumAir; airCheck++)
        {
            blockType = region.getBlockType(x, y + airCheck, z);
            if (!airMask.valid(blockType)) return;
        }

        // Place Topsoil
        region.setBlock(x, y + yOffset, z, surfacePalette);
        region.setBlock(x, y + yOffset + 1, z, foliagePalette);
        for (int dy = 1; dy < depth; dy++) if (!airMask.valid(region.getBlockType(x, y + yOffset - dy, z))) region.setBlock(x, y + yOffset - dy, z, depthPalette);
    }
}