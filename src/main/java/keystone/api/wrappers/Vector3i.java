package keystone.api.wrappers;

/**
 * A wrapper for a Minecraft Vector3i. Used in filters to prevent obfuscation issues
 */
public class Vector3i
{
    private final net.minecraft.util.math.vector.Vector3i vec;
    public final int x;
    public final int y;
    public final int z;

    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param vec The Minecraft Vector3i
     */
    public Vector3i(net.minecraft.util.math.vector.Vector3i vec)
    {
        this.vec = vec;
        this.x = vec.getX();
        this.y = vec.getY();
        this.z = vec.getZ();
    }
}
