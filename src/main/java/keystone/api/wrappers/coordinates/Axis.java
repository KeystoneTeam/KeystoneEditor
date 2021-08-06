package keystone.api.wrappers.coordinates;

import net.minecraft.util.Direction;

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

    public boolean isHorizontal() { return this.minecraftAxis.isHorizontal(); }
    public boolean isVertical() { return this.minecraftAxis.isVertical(); }
}
