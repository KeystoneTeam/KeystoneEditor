import keystone.api.filters.FilterBox;
import keystone.api.filters.KeystoneFilter;
import keystone.api.filters.Variable;
import keystone.api.wrappers.BlockMask;
import keystone.api.wrappers.BlockPalette;

public class Walls extends KeystoneFilter
{
    @Variable BlockMask mask = whitelist("minecraft:air");
    @Variable BlockPalette palette = palette("minecraft:stone");
    @Variable int wallDepth = 1;
    @Variable boolean roof = false;
    @Variable boolean floor = false;
    @Variable boolean useMask = false;

    @Override
    public void processBlock(int x, int y, int z, FilterBox box)
    {
        if (!useMask || mask.valid(box.getBlock(x, y, z)))
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