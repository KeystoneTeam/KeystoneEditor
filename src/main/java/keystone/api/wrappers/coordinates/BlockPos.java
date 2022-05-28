package keystone.api.wrappers.coordinates;

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
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * @return The Minecraft block position
     */
    public net.minecraft.util.math.BlockPos getMinecraftBlockPos() { return pos; }

    @Override
    public int hashCode()
    {
        return pos.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        else if (!(obj instanceof BlockPos other)) return false;
        else return pos.equals(other.pos);
    }

    @Override
    public String toString()
    {
        return this.pos.toString();
    }
}
