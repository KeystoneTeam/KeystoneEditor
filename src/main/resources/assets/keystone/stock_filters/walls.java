import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.variables.Tooltip;
import keystone.api.variables.Variable;
import keystone.api.wrappers.blocks.BlockMask;
import keystone.api.wrappers.blocks.BlockPalette;

public class Walls extends KeystoneFilter
{
    @Tooltip("Only blocks matching this mask will be replaced.")
    @Variable BlockMask mask = blacklist();
    
    @Tooltip("The palette to create the walls out of.")
    @Variable BlockPalette palette = palette("minecraft:stone");
    
    @Tooltip("The thickness of the walls. This does not effect the time it takes to process.")
    @Variable int wallDepth = 1;
    
    @Tooltip("Whether to fill the roof of the selection.")
    @Variable boolean roof = false;
    
    @Tooltip("Whether to fill the floor of the selection.")
    @Variable boolean floor = false;

    @Override
    public void processBlock(int x, int y, int z, WorldRegion box)
    {
        if (mask.valid(box.getBlockType(x, y, z)))
        {
            if (x - box.min.x < wallDepth) box.setBlock(x, y, z, palette);
            else if (box.max.x - x < wallDepth) box.setBlock(x, y, z, palette);
            else if (z - box.min.z < wallDepth) box.setBlock(x, y, z, palette);
            else if (box.max.z - z < wallDepth) box.setBlock(x, y, z, palette);
            else if (roof && box.max.y - y < wallDepth) box.setBlock(x, y, z, palette);
            else if (floor && y - box.min.y < wallDepth) box.setBlock(x, y, z, palette);
        }
    }
}