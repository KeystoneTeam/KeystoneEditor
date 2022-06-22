package keystone.api.wrappers.coordinates;

import net.minecraft.util.math.Direction;

/**
 * A wrapper for a Minecraft axis. Used in filters to prevent obfuscation issues
 */
public enum Axis
{
    X(Direction.Axis.X),
    Y(Direction.Axis.Y),
    Z(Direction.Axis.Z);

    final Direction.Axis minecraftAxis;

    Axis(Direction.Axis minecraftAxis)
    {
        this.minecraftAxis = minecraftAxis;
    }

    /**
     * @return Whether this is a horizontal axis. (x- or z-axis)
     */
    public boolean isHorizontal() { return this.minecraftAxis.isHorizontal(); }
    /**
     * @return Whether this is a vertical axis. (y-axis)
     */
    public boolean isVertical() { return this.minecraftAxis.isVertical(); }
}
