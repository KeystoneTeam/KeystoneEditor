package keystone.api.wrappers;

/**
 * A wrapper for a Minecraft block position. Used in filters to prevent obfuscation issues
 */
public class BlockPos
{
    private final net.minecraft.util.math.BlockPos pos;
    public final int x;
    public final int y;
    public final int z;

    public BlockPos(int x, int y, int z) { this(new net.minecraft.util.math.BlockPos(x, y, z)); }
    public BlockPos(net.minecraft.util.math.BlockPos pos)
    {
        this.pos = pos;
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @return The Minecraft block position
     */
    public net.minecraft.util.math.BlockPos getMinecraftBlockPos() { return pos; }

    @Override
    public String toString()
    {
        return this.pos.toString();
    }
}
