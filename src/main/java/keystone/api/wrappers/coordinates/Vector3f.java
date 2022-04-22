package keystone.api.wrappers.coordinates;

import net.minecraft.util.math.Vec3f;

/**
 * A wrapper for a Minecraft Vec3f. Used in filters to prevent obfuscation issues
 */
public class Vector3f
{
    private final Vec3f vec;
    public final float x;
    public final float y;
    public final float z;

    /**
     * Create a 3-dimensional float vector
     * @param x The x value
     * @param y The y value
     * @param z The z value
     */
    public Vector3f(float x, float y, float z) { this(new Vec3f(x, y, z)); }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * @param vec The Minecraft Vector3f
     */
    public Vector3f(Vec3f vec)
    {
        this.vec = vec;
        this.x = vec.getX();
        this.y = vec.getY();
        this.z = vec.getZ();
    }

    public Vector3f cross(Vector3f other)
    {
        Vec3f copy = vec.copy();
        copy.cross(other.vec);
        return new Vector3f(copy);
    }
    public float dot(Vector3f other)
    {
        return vec.dot(other.vec);
    }
    public Vector3f normalize()
    {
        Vec3f copy = vec.copy();
        copy.normalize();
        return new Vector3f(copy);
    }
}
