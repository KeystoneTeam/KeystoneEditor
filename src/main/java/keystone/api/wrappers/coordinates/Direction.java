package keystone.api.wrappers.coordinates;

public enum Direction
{
    DOWN(net.minecraft.util.math.Direction.DOWN),
    UP(net.minecraft.util.math.Direction.UP),
    NORTH(net.minecraft.util.math.Direction.NORTH),
    SOUTH(net.minecraft.util.math.Direction.SOUTH),
    WEST(net.minecraft.util.math.Direction.WEST),
    EAST(net.minecraft.util.math.Direction.EAST);

    final net.minecraft.util.math.Direction minecraftDirection;

    Direction(net.minecraft.util.math.Direction minecraftDirection)
    {
        this.minecraftDirection = minecraftDirection;
    }

    public Axis getAxis()
    {
        return switch (this)
                {
                    case DOWN, UP -> Axis.Y;
                    case NORTH, SOUTH -> Axis.Z;
                    case WEST, EAST -> Axis.X;
                };
    }
    public Vector3i getVector()
    {
        return new Vector3i(minecraftDirection.getVector());
    }
    public Direction getOpposite()
    {
        return switch (this)
                {
                    case DOWN -> UP;
                    case UP -> DOWN;
                    case NORTH -> SOUTH;
                    case SOUTH -> NORTH;
                    case WEST -> EAST;
                    case EAST -> WEST;
                };
    }
}
