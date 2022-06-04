import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.variables.Variable;
import keystone.api.wrappers.blocks.BlockPalette;
import keystone.api.wrappers.blocks.BlockType;
import keystone.api.wrappers.coordinates.BlockPos;

public class Hollow extends KeystoneFilter
{
    @Variable BlockPalette hollowingPalette = palette("minecraft:air");
    @Variable int thickness = 1;

    @Override
    public void processBlock(int x, int y, int z, WorldRegion region)
    {
        for (int dx = -thickness; dx <= thickness; dx++)
        {
            for (int dy = -thickness; dy <= thickness; dy++)
            {
                for (int dz = -thickness; dz <= thickness; dz++)
                {
                    if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) > thickness) continue;
                    BlockPos pos = new BlockPos(x + dx, y + dy, z + dz);
                    BlockType type = region.getBlockType(pos.x, pos.y, pos.z);
                    if (!type.isOpaque() || !type.isCube(pos.x, pos.y, pos.z)) return;
                }
            }
        }
        region.setBlock(x, y, z, hollowingPalette);
    }
}