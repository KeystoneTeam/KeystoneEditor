import java.util.HashMap;

import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.variables.Variable;
import keystone.api.wrappers.blocks.BlockMask;
import keystone.api.wrappers.blocks.BlockPalette;
import keystone.api.wrappers.coordinates.BlockPos;

public class SpreadLights extends KeystoneFilter
{
    @Variable BlockMask surfaceMask = blacklist("minecraft:air");
    @Variable BlockMask airMask = whitelist("minecraft:air");
    @Variable BlockPalette lights = palette();
    @Variable int spacing = 4;
    
    private HashMap heightmap = new HashMap();
    
    @Override
    public void processRegion(WorldRegion region)
    {
        for (int z = region.min.z; z <= region.max.z; z++)
        {
            columnLoop:
            for (int x = region.min.x; x <= region.max.x; x++)
            {
                BlockPos key = new BlockPos(x, 0, z);
                for (int y = region.max.y; y >= region.min.y; y--)
                {
                    if (surfaceMask.valid(region.getBlockType(x, y, z)))
                    {
                        heightmap.put(key, y);
                        continue columnLoop;
                    }
                }
                heightmap.put(key, Integer.MIN_VALUE);
            }
        }
    }
    
    @Override
    public void processBlock(int x, int y, int z, WorldRegion region)
    {
        if (x % spacing == 0 && z % spacing == 0 && airMask.valid(region.getBlockType(x, y, z)))
        {
            if (surfaceMask.valid(region.getBlockType(x, y - 1, z)))
            {
                int heightmap = (Integer)this.heightmap.get(new BlockPos(x, 0, z));
                if (y < heightmap) region.setBlockType(x, y, z, lights);
            }
        }
    }
}