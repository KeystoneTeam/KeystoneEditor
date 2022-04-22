package keystone.core.modules.brush;

import keystone.api.Keystone;
import net.minecraft.util.math.Vec3i;

public class ShapeMask
{
    public enum MaskType { OUTSIDE, INSIDE, EDGE, ERROR }

    private Vec3i size;
    private MaskType[] contents;
    private BrushModule brushModule;
    private int count;

    public ShapeMask(Vec3i size, byte[] contents)
    {
        this.size = size;
        this.contents = new MaskType[contents.length];
        this.brushModule = Keystone.getModule(BrushModule.class);
        for (int i = 0; i < contents.length; i++)
        {
            switch (contents[i])
            {
                case 0: this.contents[i] = MaskType.OUTSIDE; break;
                case 1: this.contents[i] = MaskType.INSIDE; count++; break;
                case 2: this.contents[i] = MaskType.EDGE; count++; break;
                default: this.contents[i] = MaskType.ERROR; break;
            }
        }
    }
    public MaskType getMaskType(int x, int y, int z)
    {
        if (x < 0 || x >= size.getX() || y < 0 || y >= size.getY() || z < 0 || z > size.getZ()) return MaskType.ERROR;
        return contents[x + y * size.getX() + z * size.getX() * size.getY()];
    }
    public boolean test(int x, int y, int z)
    {
        MaskType mask = getMaskType(x, y, z);
        return mask == MaskType.INSIDE || mask == MaskType.EDGE && Keystone.RANDOM.nextInt(100) < brushModule.getNoise();
    }
    public int getCount() { return count; }
}
