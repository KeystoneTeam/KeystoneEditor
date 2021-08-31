import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.variables.IntRange;
import keystone.api.variables.Variable;
import keystone.api.wrappers.blocks.BlockType;
import keystone.api.wrappers.coordinates.BlockPos;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Smooth extends KeystoneFilter
{
    @Variable @IntRange(min = 1, max = 20) int smoothing = 1;

    @Override
    public void processBlock(int x, int y, int z, WorldRegion region)
    {
        region.setBlock(x, y, z, getSmoothed(region, x, y, z, smoothing));
    }

    private BlockType getSmoothed(WorldRegion region, int x, int y, int z, int smoothing)
    {
        HashMap counts = new HashMap();
        int max = 0;
        BlockType smoothed = null;

        for (int dx = -smoothing; dx <= smoothing; dx++)
        {
            for (int dy = -smoothing; dy <= smoothing; dy++)
            {
                for (int dz = -smoothing; dz <= smoothing; dz++)
                {
                    if (dx * dx + dy * dy + dz * dz > smoothing * smoothing) continue;

                    int cx = x + dx;
                    int cy = y + dy;
                    int cz = z + dz;
                    BlockType currentBlock = region.getBlockType(cx, cy, cz);

                    Integer count = (Integer)counts.getOrDefault(currentBlock, 0);
                    count++;
                    counts.put(currentBlock, count);
                    if (count > max || smoothed == null)
                    {
                        smoothed = currentBlock;
                        max = count;
                    }
                }
            }
        }

        return smoothed;
    }
}