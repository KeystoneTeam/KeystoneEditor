package keystone.core.renderer.shapes;

import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.core.renderer.RenderBox;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.Objects;

public class Cuboid
{
    private Vec3i min;
    private Vec3i max;
    private Vec3i corner1;
    private Vec3i corner2;
    private Vec3i size;
    private Vec3d center;

    protected Cuboid(Vec3i corner1, Vec3i corner2)
    {
        this.corner1 = corner1;
        this.corner2 = corner2;
        refreshMinMax();
    }
    public static Cuboid from(Vec3i min, Vec3i max)
    {
        return new Cuboid(min, max);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(corner1, corner2);
    }
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Cuboid other = (Cuboid) obj;
        return corner1.equals(other.corner1) && corner2.equals(other.corner2);
    }

    public Vec3i getMin()
    {
        return min;
    }
    public Vec3i getMax()
    {
        return max;
    }
    public Vec3i getCorner1()
    {
        return corner1;
    }
    public Vec3i getCorner2()
    {
        return corner2;
    }
    public Vec3i getSize() { return size; }
    public RenderBox getRenderingBox()
    {
        return new RenderBox(min, max);
    }
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
    public Vec3d getCenter() { return center; }
    public BoundingBox getBoundingBox() { return new BoundingBox(min, max); }

    public final void refreshMinMax()
    {
        min = new Vec3i(Math.min(corner1.getX(), corner2.getX()), Math.min(corner1.getY(), corner2.getY()), Math.min(corner1.getZ(), corner2.getZ()));
        max = new Vec3i(Math.max(corner1.getX(), corner2.getX()), Math.max(corner1.getY(), corner2.getY()), Math.max(corner1.getZ(), corner2.getZ()));
        size = new Vec3i(max.getX() - min.getX() + 1, max.getY() - min.getY() + 1, max.getZ() - min.getZ() + 1);
        center = Vec3d.of(min).add(size.getX() * 0.5, size.getY() * 0.5, size.getZ() * 0.5);
    }

    public void setCorner1(Vec3i coords)
    {
        corner1 = coords;
        refreshMinMax();
    }
    public void setCorner2(Vec3i coords)
    {
        corner2 = coords;
        refreshMinMax();
    }
    public void setMin(Vec3i coords)
    {
        int corner1X = corner1.getX() == min.getX() ? coords.getX() : corner1.getX();
        int corner1Y = corner1.getY() == min.getY() ? coords.getY() : corner1.getY();
        int corner1Z = corner1.getZ() == min.getZ() ? coords.getZ() : corner1.getZ();
        int corner2X = corner2.getX() == min.getX() ? coords.getX() : corner2.getX();
        int corner2Y = corner2.getY() == min.getY() ? coords.getY() : corner2.getY();
        int corner2Z = corner2.getZ() == min.getZ() ? coords.getZ() : corner2.getZ();
        corner1 = new Vec3i(corner1X, corner1Y, corner1Z);
        corner2 = new Vec3i(corner2X, corner2Y, corner2Z);
        refreshMinMax();
    }
    public void setMax(Vec3i coords)
    {
        int corner1X = corner1.getX() == corner2.getX() ? corner1.getX() : corner1.getX() == max.getX() ? coords.getX() : corner1.getX();
        int corner1Y = corner1.getY() == corner2.getY() ? corner1.getY() : corner1.getY() == max.getY() ? coords.getY() : corner1.getY();
        int corner1Z = corner1.getZ() == corner2.getZ() ? corner1.getZ() : corner1.getZ() == max.getZ() ? coords.getZ() : corner1.getZ();
        int corner2X = corner2.getX() == corner1.getX() ? coords.getX() : corner2.getX() == max.getX() ? coords.getX() : corner2.getX();
        int corner2Y = corner2.getY() == corner1.getY() ? coords.getY() : corner2.getY() == max.getY() ? coords.getY() : corner2.getY();
        int corner2Z = corner2.getZ() == corner1.getZ() ? coords.getZ() : corner2.getZ() == max.getZ() ? coords.getZ() : corner2.getZ();
        corner1 = new Vec3i(corner1X, corner1Y, corner1Z);
        corner2 = new Vec3i(corner2X, corner2Y, corner2Z);
        refreshMinMax();
    }
    public boolean isFaceCorner1(Direction face)
    {
        switch (face)
        {
            case NORTH: return corner1.getZ() == min.getZ();
            case SOUTH: return corner1.getZ() == max.getZ();
            case WEST: return corner1.getX() == min.getX();
            case EAST: return corner1.getX() == max.getX();
            case UP: return corner1.getY() == max.getY();
            case DOWN: return corner1.getY() == min.getY();
            default: return true;
        }
    }

    public void nudgeCorner1(Direction direction, int amount)
    {
        corner1 = new Vec3i(corner1.getX() + direction.getVector().getX() * amount,
                corner1.getY() + direction.getVector().getY() * amount,
                corner1.getZ() + direction.getVector().getZ() * amount);
        refreshMinMax();
    }
    public void nudgeCorner2(Direction direction, int amount)
    {
        corner2 = new Vec3i(corner2.getX() + direction.getVector().getX() * amount,
                corner2.getY() + direction.getVector().getY() * amount,
                corner2.getZ() + direction.getVector().getZ() * amount);
        refreshMinMax();
    }
    public void nudgeBox(Direction direction, int amount)
    {
        corner1 = new Vec3i(corner1.getX() + direction.getVector().getX() * amount,
                corner1.getY() + direction.getVector().getY() * amount,
                corner1.getZ() + direction.getVector().getZ() * amount);
        corner2 = new Vec3i(corner2.getX() + direction.getVector().getX() * amount,
                corner2.getY() + direction.getVector().getY() * amount,
                corner2.getZ() + direction.getVector().getZ() * amount);
        refreshMinMax();
    }

    public boolean moveFace(Direction direction, int newPosition)
    {
        boolean cornersSwapped = false;
        switch (direction)
        {
            case UP:
                if (corner1.getY() == max.getY()) corner1 = new Vec3i(corner1.getX(), newPosition, corner1.getZ());
                else corner2 = new Vec3i(corner2.getX(), newPosition, corner2.getZ());
                cornersSwapped = newPosition < min.getY();
                break;
            case DOWN:
                if (corner1.getY() == min.getY()) corner1 = new Vec3i(corner1.getX(), newPosition, corner1.getZ());
                else corner2 = new Vec3i(corner2.getX(), newPosition, corner2.getZ());
                cornersSwapped = newPosition > max.getY();
                break;
            case NORTH:
                if (corner1.getZ() == min.getZ()) corner1 = new Vec3i(corner1.getX(), corner1.getY(), newPosition);
                else corner2 = new Vec3i(corner2.getX(), corner2.getY(), newPosition);
                cornersSwapped = newPosition > max.getZ();
                break;
            case SOUTH:
                if (corner1.getZ() == max.getZ()) corner1 = new Vec3i(corner1.getX(), corner1.getY(), newPosition);
                else corner2 = new Vec3i(corner2.getX(), corner2.getY(), newPosition);
                cornersSwapped = newPosition < min.getZ();
                break;
            case WEST:
                if (corner1.getX() == min.getX()) corner1 = new Vec3i(newPosition, corner1.getY(), corner1.getZ());
                else corner2 = new Vec3i(newPosition, corner2.getY(), corner2.getZ());
                cornersSwapped = newPosition > max.getX();
                break;
            case EAST:
                if (corner1.getX() == max.getX()) corner1 = new Vec3i(newPosition, corner1.getY(), corner1.getZ());
                else corner2 = new Vec3i(newPosition, corner2.getY(), corner2.getZ());
                cornersSwapped = newPosition < min.getX();
                break;
        }
        refreshMinMax();
        return cornersSwapped;
    }
    public void move(Vec3i newMin)
    {
        Vec3i diff = new Vec3i(getMax().getX() - getMin().getX(), getMax().getY() - getMin().getY(), getMax().getZ() - getMin().getZ());
        Vec3i newMax = newMin.add(diff);

        int corner1X, corner1Y, corner1Z;
        int corner2X, corner2Y, corner2Z;

        // Change corner x values
        if (corner1.getX() == min.getX())
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
        if (corner1.getY() == min.getY())
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
        if (corner1.getZ() == min.getZ())
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
        corner1 = new Vec3i(corner1X, corner1Y, corner1Z);
        corner2 = new Vec3i(corner2X, corner2Y, corner2Z);
        refreshMinMax();
    }

    private boolean isBetween(int val, int min, int max)
    {
        return val >= min && val <= max;
    }
}
