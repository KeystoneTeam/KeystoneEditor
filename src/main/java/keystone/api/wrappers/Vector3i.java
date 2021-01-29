package keystone.api.wrappers;

public class Vector3i
{
    private net.minecraft.util.math.vector.Vector3i vec;

    public Vector3i(net.minecraft.util.math.vector.Vector3i vec)
    {
        this.vec = vec;
    }

    public int getX() { return vec.getX(); }
    public int getY() { return vec.getY(); }
    public int getZ() { return vec.getZ(); }
}
