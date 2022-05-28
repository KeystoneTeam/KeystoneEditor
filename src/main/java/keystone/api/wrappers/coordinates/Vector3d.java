package keystone.api.wrappers.coordinates;

import net.minecraft.util.math.Vec3d;

/**
 * A wrapper for a Minecraft Vec3d. Used in filters to prevent obfuscation issues
 */
public class Vector3d
{
    private final Vec3d vec;
    public final double x;
    public final double y;
    public final double z;

    /**
     * Create a 3-dimensional double vector
     * @param x The x value
     * @param y The y value
     * @param z The z value
     */
    public Vector3d(double x, double y, double z) { this(new Vec3d(x, y, z)); }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * @param vec The Minecraft Vec3d
     */
    public Vector3d(Vec3d vec)
    {
        this.vec = vec;
        this.x = vec.getX();
        this.y = vec.getY();
        this.z = vec.getZ();
    }

    public Vector3d cross(Vector3d other)
    {
        return new Vector3d(vec.crossProduct(other.vec));
    }
    public double dot(Vector3d other)
    {
        return vec.dotProduct(other.vec);
    }
    public Vector3d normalize()
    {
        return new Vector3d(vec.normalize());
    }

    @Override
    public int hashCode()
    {
        return vec.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        else if (!(obj instanceof Vector3d other)) return false;
        else return vec.equals(other.vec);
    }

    @Override
    public String toString()
    {
        return vec.toString();
    }
}
