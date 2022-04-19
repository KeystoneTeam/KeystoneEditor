package keystone.core.renderer;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class RenderBox extends Box
{
    private static final double nudgeSize = 0.01F;

    public RenderBox(double x1, double y1, double z1, double x2, double y2, double z2)
    {
        super(x1, y1, z1, x2, y2, z2);
    }

    public RenderBox(BlockPos pos)
    {
        super(pos, pos.add(1, 1, 1));
    }

    public RenderBox(BlockPos pos1, BlockPos pos2)
    {
        super(pos1, pos2.add(1, 1, 1));
    }

    public RenderBox(Vec3d pos1, Vec3d pos2)
    {
        super(pos1, pos2);
    }

    public RenderBox(Vec3i pos) { super(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1); }
    public RenderBox(Vec3i pos1, Vec3i pos2)
    {
        super (pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX() + 1, pos2.getY() + 1, pos2.getZ() + 1);
    }

    public RenderBox grow(double x, double y, double z)
    {
        return new RenderBox(minX - x, minY - y, minZ - z, maxX + x, maxY +  y, maxZ + z);
    }
    public RenderBox nudge()
    {
        if (minY == maxY)
        {
            return new RenderBox(minX - nudgeSize, minY + nudgeSize, minZ - nudgeSize, maxX + nudgeSize, maxY + nudgeSize, maxZ + nudgeSize);
        }
        return grow(nudgeSize, nudgeSize, nudgeSize);
    }
}
