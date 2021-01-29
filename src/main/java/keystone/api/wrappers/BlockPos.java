package keystone.api.wrappers;

public class BlockPos
{
    private net.minecraft.util.math.BlockPos pos;

    public BlockPos(int x, int y, int z) { this(new net.minecraft.util.math.BlockPos(x, y, z)); }
    public BlockPos(net.minecraft.util.math.BlockPos pos)
    {
        this.pos = pos;
    }

    public int getX() { return pos.getX(); }
    public int getY() { return pos.getY(); }
    public int getZ() { return pos.getZ(); }

    public net.minecraft.util.math.BlockPos getMinecraftBlockPos() { return pos; }
}
