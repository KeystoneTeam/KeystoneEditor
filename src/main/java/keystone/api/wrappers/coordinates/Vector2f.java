package keystone.api.wrappers.coordinates;

/**
 * A wrapper for a Minecraft Vector2f. Used in filters to prevent obfuscation issues
 */
public class Vector2f
{
    private final net.minecraft.client.util.math.Vector2f vec;
    public final float x;
    public final float y;

    /**
     * Create a 2-dimensional float vector
     * @param x The x value
     * @param y The y value
     */
    public Vector2f(float x, float y) { this(new net.minecraft.client.util.math.Vector2f(x, y)); }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * @param vec The Minecraft Vector2f
     */
    public Vector2f(net.minecraft.client.util.math.Vector2f vec)
    {
        this.vec = vec;
        this.x = vec.getX();
        this.y = vec.getY();
    }
}
