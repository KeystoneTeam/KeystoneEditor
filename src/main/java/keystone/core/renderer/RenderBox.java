package keystone.core.renderer;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class RenderBox extends Box
{
    public RenderBox(BlockPos pos)
    {
        super(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }
    public RenderBox(Vec3i pos) { super(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1); }
    
    public RenderBox(BlockPos pos1, BlockPos pos2)
    {
        super(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX() + 1, pos2.getY() + 1, pos2.getZ() + 1);
    }
    public RenderBox(Vec3i pos1, Vec3i pos2)
    {
        super (pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX() + 1, pos2.getY() + 1, pos2.getZ() + 1);
    }
    public RenderBox(Vec3d pos1, Vec3d pos2)
    {
        super(pos1, pos2);
    }
}
