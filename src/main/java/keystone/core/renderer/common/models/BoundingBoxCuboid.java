package keystone.core.renderer.common.models;

import keystone.api.Keystone;
import keystone.core.events.KeystoneSelectionChangedEvent;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.renderer.common.BoundingBoxType;
import keystone.core.renderer.common.MathHelper;
import keystone.core.renderer.common.TypeHelper;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.system.CallbackI;

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
