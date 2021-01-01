package keystone.core.renderer.common.models;

import keystone.core.renderer.common.BoundingBoxType;
import keystone.core.renderer.common.MathHelper;
import keystone.core.renderer.common.TypeHelper;
import net.minecraft.util.Direction;

public class BoundingBoxCuboid extends AbstractBoundingBox
{
    private Coords minCoords;
    private Coords maxCoords;
    private Coords corner1;
    private Coords corner2;

    protected BoundingBoxCuboid(Coords corner1, Coords corner2, BoundingBoxType type)
    {
        super(type);
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.minCoords = new Coords(Math.min(corner1.getX(), corner2.getX()), Math.min(corner1.getY(), corner2.getY()), Math.min(corner1.getZ(), corner2.getZ()));
        this.maxCoords = new Coords(Math.max(corner1.getX(), corner2.getX()), Math.max(corner1.getY(), corner2.getY()), Math.max(corner1.getZ(), corner2.getZ()));
    }
    public static BoundingBoxCuboid from(Coords minCoords, Coords maxCoords, BoundingBoxType type)
    {
        return new BoundingBoxCuboid(minCoords, maxCoords, type);
    }

    @Override
    public int hashCode()
    {
        return TypeHelper.combineHashCodes(minCoords.hashCode(), maxCoords.hashCode());
    }
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BoundingBoxCuboid other = (BoundingBoxCuboid) obj;
        return minCoords.equals(other.minCoords) && maxCoords.equals(other.maxCoords);
    }

    public Coords getMinCoords()
    {
        return minCoords;
    }
    public Coords getMaxCoords()
    {
        return maxCoords;
    }
    public Coords getCorner1()
    {
        return corner1;
    }
    public Coords getCorner2()
    {
        return corner2;
    }

    public final void refreshMinMax()
    {
        minCoords = new Coords(Math.min(corner1.getX(), corner2.getX()), Math.min(corner1.getY(), corner2.getY()), Math.min(corner1.getZ(), corner2.getZ()));
        maxCoords = new Coords(Math.max(corner1.getX(), corner2.getX()), Math.max(corner1.getY(), corner2.getY()), Math.max(corner1.getZ(), corner2.getZ()));
    }

    public void setCorner1(Coords coords)
    {
        corner1 = coords;
        refreshMinMax();
    }
    public void setCorner2(Coords coords)
    {
        corner2 = coords;
        refreshMinMax();
    }
    public void moveFace(Direction direction, int newPosition)
    {
        switch (direction)
        {
            case UP:
                if (corner1.getY() == maxCoords.getY()) corner1 = new Coords(corner1.getX(), newPosition, corner1.getZ());
                else corner2 = new Coords(corner2.getX(), newPosition, corner2.getZ());
                break;
            case DOWN:
                if (corner1.getY() == minCoords.getY()) corner1 = new Coords(corner1.getX(), newPosition, corner1.getZ());
                else corner2 = new Coords(corner2.getX(), newPosition, corner2.getZ());
                break;
            case NORTH:
                if (corner1.getZ() == minCoords.getZ()) corner1 = new Coords(corner1.getX(), corner1.getY(), newPosition);
                else corner2 = new Coords(corner2.getX(), corner2.getY(), newPosition);
                break;
            case SOUTH:
                if (corner1.getZ() == maxCoords.getZ()) corner1 = new Coords(corner1.getX(), corner1.getY(), newPosition);
                else corner2 = new Coords(corner2.getX(), corner2.getY(), newPosition);
                break;
            case WEST:
                if (corner1.getX() == minCoords.getX()) corner1 = new Coords(newPosition, corner1.getY(), corner1.getZ());
                else corner2 = new Coords(newPosition, corner2.getY(), corner2.getZ());
                break;
            case EAST:
                if (corner1.getX() == maxCoords.getX()) corner1 = new Coords(newPosition, corner1.getY(), corner1.getZ());
                else corner2 = new Coords(newPosition, corner2.getY(), corner2.getZ());
                break;
        }
        refreshMinMax();
    }

    @Override
    public Boolean intersectsBounds(int minX, int minZ, int maxX, int maxZ)
    {
        boolean minXWithinBounds = isBetween(minCoords.getX(), minX, maxX);
        boolean maxXWithinBounds = isBetween(maxCoords.getX(), minX, maxX);
        boolean minZWithinBounds = isBetween(minCoords.getZ(), minZ, maxZ);
        boolean maxZWithinBounds = isBetween(maxCoords.getZ(), minZ, maxZ);

        return (minXWithinBounds && minZWithinBounds) ||
                (maxXWithinBounds && maxZWithinBounds) ||
                (minXWithinBounds && maxZWithinBounds) ||
                (maxXWithinBounds && minZWithinBounds);
    }

    private boolean isBetween(int val, int min, int max)
    {
        return val >= min && val <= max;
    }

    @Override
    public double getDistanceX(double x)
    {
        return x - MathHelper.clamp(x, minCoords.getX(), maxCoords.getX());
    }
    @Override
    public double getDistanceY(double y)
    {
        return y - MathHelper.clamp(y, minCoords.getY(), maxCoords.getY());
    }
    @Override
    public double getDistanceZ(double z)
    {
        return z - MathHelper.clamp(z, minCoords.getZ(), maxCoords.getZ());
    }
}
