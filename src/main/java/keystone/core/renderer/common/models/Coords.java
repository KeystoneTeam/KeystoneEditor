package keystone.core.renderer.common.models;

import keystone.core.renderer.common.MathHelper;
import keystone.core.renderer.common.TypeHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

import java.util.Vector;

public class Coords
{
    private final int x;
    private final int y;
    private final int z;

    public Coords(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Coords(double x, double y, double z)
    {
        this.x = MathHelper.floor(x);
        this.y = MathHelper.floor(y);
        this.z = MathHelper.floor(z);
    }
    public Coords(Vector3i pos)
    {
        this(pos.getX(), pos.getY(), pos.getZ());
    }
    public Coords(Vector3d pos)
    {
        this(pos.x, pos.y, pos.z);
    }

    public int getX()
    {
        return x;
    }
    public int getY()
    {
        return y;
    }
    public int getZ()
    {
        return z;
    }

    public Coords clone()
    {
        return new Coords(x, y, z);
    }

    public Coords add(double x, double y, double z)
    {
        return new Coords(this.x + x, this.y + y, this.z + z);
    }
    public Coords add(Vector3i add)
    {
        return new Coords(x + add.getX(), y + add.getY(), z + add.getZ());
    }
    public Coords add(Vector3d add)
    {
        return new Coords(x + add.x, y + add.y, z + add.z);
    }
    public Coords add(Coords add)
    {
        return new Coords(x + add.x, y + add.y, z + add.z);
    }

    public Coords sub(double x, double y, double z)
    {
        return new Coords(this.x - x, this.y - y, this.z - z);
    }
    public Coords sub(Vector3i add)
    {
        return new Coords(x - add.getX(), y - add.getY(), z - add.getZ());
    }
    public Coords sub(Vector3d add)
    {
        return new Coords(x - add.x, y - add.y, z - add.z);
    }
    public Coords sub(Coords add)
    {
        return new Coords(x - add.x, y - add.y, z - add.z);
    }

    @Override
    public int hashCode()
    {
        return TypeHelper.combineHashCodes(z, y, x);
    }
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Coords other = (Coords) obj;
        return getX() == other.getX() &&
                getY() == other.getY() &&
                getZ() == other.getZ();

    }
}
