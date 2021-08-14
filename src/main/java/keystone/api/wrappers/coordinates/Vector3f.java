package keystone.api.wrappers.coordinates;

/**
 * A wrapper for a Minecraft Vector3f. Used in filters to prevent obfuscation issues
 */
public class Vector3f
{
    private final net.minecraft.util.math.vector.Vector3f vec;
    public final float x;
    public final float y;
    public final float z;

    /**
     * Create a 3-dimensional float vector
     * @param x The x value
     * @param y The y value
     * @param z The z value
     */
    public Vector3f(float x, float y, float z) { this(new net.minecraft.util.math.vector.Vector3f(x, y, z)); }
    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param vec The Minecraft Vector3f
     */
    public Vector3f(net.minecraft.util.math.vector.Vector3f vec)
    {
        this.vec = vec;
        this.x = vec.x();
        this.y = vec.y();
        this.z = vec.z();
    }

    public Vector3f cross(Vector3f other)
    {
        net.minecraft.util.math.vector.Vector3f copy = vec.copy();
        copy.cross(other.vec);
        return new Vector3f(copy);
    }
    public float dot(Vector3f other)
    {
        return vec.dot(other.vec);
    }
    public Vector3f normalize()
    {
        net.minecraft.util.math.vector.Vector3f copy = vec.copy();
        copy.normalize();
        return new Vector3f(copy);
    }
}
