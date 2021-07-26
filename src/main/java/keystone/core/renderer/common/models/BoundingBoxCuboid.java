package keystone.core.renderer.common.models;

import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.core.renderer.common.BoundingBoxType;
import keystone.core.renderer.common.MathHelper;
import keystone.core.renderer.common.TypeHelper;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

public class BoundingBoxCuboid extends AbstractBoundingBox
{
    private Coords minCoords;
    private Coords maxCoords;
    private Coords corner1;
    private Coords corner2;
    private Vector3i size;
    private Vector3d center;

    protected BoundingBoxCuboid(Coords corner1, Coords corner2, BoundingBoxType type)
    {
        super(type);
        this.corner1 = corner1;
        this.corner2 = corner2;
        refreshMinMax();
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
    public Vector3i getSize() { return size; }
    public int getAxisSize(Direction.Axis axis)
    {
        switch (axis)
        {
            case X: return size.getX();
            case Y: return size.getY();
            case Z: return size.getZ();
            default: return -1;
        }
    }
    public Vector3d getCenter() { return center; }
    public BoundingBox getBoundingBox() { return new BoundingBox(minCoords.toBlockPos(), maxCoords.toBlockPos()); }

    public final void refreshMinMax()
    {
        minCoords = new Coords(Math.min(corner1.getX(), corner2.getX()), Math.min(corner1.getY(), corner2.getY()), Math.min(corner1.getZ(), corner2.getZ()));
        maxCoords = new Coords(Math.max(corner1.getX(), corner2.getX()), Math.max(corner1.getY(), corner2.getY()), Math.max(corner1.getZ(), corner2.getZ()));
        size = new Vector3i(maxCoords.getX() - minCoords.getX() + 1, maxCoords.getY() - minCoords.getY() + 1, maxCoords.getZ() - minCoords.getZ() + 1);
        center = minCoords.toVector3d().add(size.getX() * 0.5, size.getY() * 0.5, size.getZ() * 0.5);
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
    public void setMinCoords(Coords coords)
    {
        int corner1X = corner1.getX() == minCoords.getX() ? coords.getX() : corner1.getX();
        int corner1Y = corner1.getY() == minCoords.getY() ? coords.getY() : corner1.getY();
        int corner1Z = corner1.getZ() == minCoords.getZ() ? coords.getZ() : corner1.getZ();
        int corner2X = corner2.getX() == minCoords.getX() ? coords.getX() : corner2.getX();
        int corner2Y = corner2.getY() == minCoords.getY() ? coords.getY() : corner2.getY();
        int corner2Z = corner2.getZ() == minCoords.getZ() ? coords.getZ() : corner2.getZ();
        corner1 = new Coords(corner1X, corner1Y, corner1Z);
        corner2 = new Coords(corner2X, corner2Y, corner2Z);
        refreshMinMax();
    }
    public void setMaxCoords(Coords coords)
    {
        int corner1X = corner1.getX() == corner2.getX() ? corner1.getX() : corner1.getX() == maxCoords.getX() ? coords.getX() : corner1.getX();
        int corner1Y = corner1.getY() == corner2.getY() ? corner1.getY() : corner1.getY() == maxCoords.getY() ? coords.getY() : corner1.getY();
        int corner1Z = corner1.getZ() == corner2.getZ() ? corner1.getZ() : corner1.getZ() == maxCoords.getZ() ? coords.getZ() : corner1.getZ();
        int corner2X = corner2.getX() == corner1.getX() ? coords.getX() : corner2.getX() == maxCoords.getX() ? coords.getX() : corner2.getX();
        int corner2Y = corner2.getY() == corner1.getY() ? coords.getY() : corner2.getY() == maxCoords.getY() ? coords.getY() : corner2.getY();
        int corner2Z = corner2.getZ() == corner1.getZ() ? coords.getZ() : corner2.getZ() == maxCoords.getZ() ? coords.getZ() : corner2.getZ();
        corner1 = new Coords(corner1X, corner1Y, corner1Z);
        corner2 = new Coords(corner2X, corner2Y, corner2Z);
        refreshMinMax();
    }
    public boolean isFaceCorner1(Direction face)
    {
        switch (face)
        {
            case NORTH: return corner1.getZ() == minCoords.getZ();
            case SOUTH: return corner1.getZ() == maxCoords.getZ();
            case WEST: return corner1.getX() == minCoords.getX();
            case EAST: return corner1.getX() == maxCoords.getX();
            case UP: return corner1.getY() == maxCoords.getY();
            case DOWN: return corner1.getY() == minCoords.getY();
            default: return true;
        }
    }

    public void nudgeCorner1(Direction direction, int amount)
    {
        corner1 = new Coords(corner1.getX() + direction.getNormal().getX() * amount,
                corner1.getY() + direction.getNormal().getY() * amount,
                corner1.getZ() + direction.getNormal().getZ() * amount);
        refreshMinMax();
    }
    public void nudgeCorner2(Direction direction, int amount)
    {
        corner2 = new Coords(corner2.getX() + direction.getNormal().getX() * amount,
                corner2.getY() + direction.getNormal().getY() * amount,
                corner2.getZ() + direction.getNormal().getZ() * amount);
        refreshMinMax();
    }
    public void nudgeBox(Direction direction, int amount)
    {
        corner1 = new Coords(corner1.getX() + direction.getNormal().getX() * amount,
                corner1.getY() + direction.getNormal().getY() * amount,
                corner1.getZ() + direction.getNormal().getZ() * amount);
        corner2 = new Coords(corner2.getX() + direction.getNormal().getX() * amount,
                corner2.getY() + direction.getNormal().getY() * amount,
                corner2.getZ() + direction.getNormal().getZ() * amount);
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
    public void move(Coords newMin)
    {
        Vector3d diff = new Vector3d(getMaxCoords().getX() - getMinCoords().getX(), getMaxCoords().getY() - getMinCoords().getY(), getMaxCoords().getZ() - getMinCoords().getZ());
        Coords newMax = newMin.add(diff);

        int corner1X, corner1Y, corner1Z;
        int corner2X, corner2Y, corner2Z;

        // Change corner x values
        if (corner1.getX() == minCoords.getX())
        {
            corner1X = newMin.getX();
            corner2X = newMax.getX();
        }
        else
        {
            corner1X = newMax.getX();
            corner2X = newMin.getX();
        }

        // Change corner y values
        if (corner1.getY() == minCoords.getY())
        {
            corner1Y = newMin.getY();
            corner2Y = newMax.getY();
        }
        else
        {
            corner1Y = newMax.getY();
            corner2Y = newMin.getY();
        }

        // Change corner z values
        if (corner1.getZ() == minCoords.getZ())
        {
            corner1Z = newMin.getZ();
            corner2Z = newMax.getZ();
        }
        else
        {
            corner1Z = newMax.getZ();
            corner2Z = newMin.getZ();
        }

        // Update box data
        corner1 = new Coords(corner1X, corner1Y, corner1Z);
        corner2 = new Coords(corner2X, corner2Y, corner2Z);
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
