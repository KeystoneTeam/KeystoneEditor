package keystone.api.wrappers.coordinates;

import net.minecraft.util.math.Vec3i;

/**
 * A wrapper for a Minecraft Vec3i. Used in filters to prevent obfuscation issues
 */
public class Vector3i
{
    private final Vec3i vec;
    public final int x;
    public final int y;
    public final int z;

    /**
     * Create a 3-dimensional integer vector
     * @param x The x value
     * @param y The y value
     * @param z The z value
     */
    public Vector3i(int x, int y, int z) { this(new Vec3i(x, y, z)); }
    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param vec The Minecraft Vector3i
     */
    public Vector3i(Vec3i vec)
    {
        this.vec = vec;
        this.x = vec.getX();
        this.y = vec.getY();
        this.z = vec.getZ();
    }
}
