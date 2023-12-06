package keystone.api.wrappers.coordinates;


/**
 * A wrapper for a Minecraft Vector3f. Used in filters to prevent obfuscation issues
 */
public class Vector3f
{
    private final org.joml.Vector3f vec;
    public final float x;
    public final float y;
    public final float z;

    /**
     * Create a 3-dimensional float vector
     * @param x The x value
     * @param y The y value
     * @param z The z value
     */
    public Vector3f(float x, float y, float z) { this(new org.joml.Vector3f(x, y, z)); }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * @param vec The Minecraft Vector3f
     */
    public Vector3f(org.joml.Vector3f vec)
    {
        this.vec = vec;
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
    }

    public Vector3f cross(Vector3f other)
    {
        org.joml.Vector3f copy = new org.joml.Vector3f(vec);
        copy.cross(other.vec);
        return new Vector3f(copy);
    }
    public float dot(Vector3f other)
    {
        return vec.dot(other.vec);
    }
    public Vector3f normalize()
    {
        org.joml.Vector3f copy = new org.joml.Vector3f(vec);
        copy.normalize();
        return new Vector3f(copy);
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
        else if (!(obj instanceof Vector3f other)) return false;
        else return vec.equals(other.vec);
    }

    @Override
    public String toString()
    {
        return vec.toString();
    }
}
