package keystone.api.wrappers.coordinates;

/**
 * A wrapper for a Minecraft Vector2f. Used in filters to prevent obfuscation issues
 */
public class Vector2f
{
    private final net.minecraft.util.math.vector.Vector2f vec;
    public final float x;
    public final float y;

    /**
     * Create a 2-dimensional float vector
     * @param x The x value
     * @param y The y value
     */
    public Vector2f(float x, float y) { this(new net.minecraft.util.math.vector.Vector2f(x, y)); }
    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param vec The Minecraft Vector2f
     */
    public Vector2f(net.minecraft.util.math.vector.Vector2f vec)
    {
        this.vec = vec;
        this.x = vec.x;
        this.y = vec.y;
    }
}
