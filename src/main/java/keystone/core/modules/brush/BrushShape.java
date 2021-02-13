package keystone.core.modules.brush;

import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class BrushShape
{
    //region Default Shapes
    public static final BrushShape ROUND = new BrushShape()
    {
        public ITextComponent getName() { return new TranslationTextComponent("keystone.shape.round"); }
        protected boolean isPositionInShape(float x, float y, float z, float sizeX, float sizeY, float sizeZ)
        {
            float nX = (2 * x / sizeX);
            float nY = (2 * y / sizeY);
            float nZ = (2 * z / sizeZ);
            return nX * nX + nY * nY + nZ * nZ < 1;
        }
    };
    public static final BrushShape DIAMOND = new BrushShape()
    {
        public ITextComponent getName() { return new TranslationTextComponent("keystone.shape.diamond"); }
        protected boolean isPositionInShape(float x, float y, float z, float sizeX, float sizeY, float sizeZ)
        {
            x = Math.abs(x) - (sizeX % 2 == 0 ? 0.5f : 0);
            y = Math.abs(y) - (sizeY % 2 == 0 ? 0.5f : 0);
            z = Math.abs(z) - (sizeZ % 2 == 0 ? 0.5f : 0);
            sizeX *= 0.5f;
            sizeY *= 0.5f;
            sizeZ *= 0.5f;
            x /= sizeX;
            y /= sizeY;
            z /= sizeZ;
            return x + y + z < 1;
        }
    };
    public static final BrushShape SQUARE = new BrushShape()
    {
        public ITextComponent getName() { return new TranslationTextComponent("keystone.shape.square"); }
        protected boolean isPositionInShape(float x, float y, float z, float sizeX, float sizeY, float sizeZ) { return true; }
    };
    //endregion

    public abstract ITextComponent getName();
    protected abstract boolean isPositionInShape(float x, float y, float z, float sizeX, float sizeY, float sizeZ);

    private int getArrayIndex(int x, int y, int z, int sizeX, int sizeY, int sizeZ)
    {
        return x + y * sizeX + z * sizeX * sizeY;
    }
    public final boolean[] getShapeMask(int sizeX, int sizeY, int sizeZ)
    {
        // Calculate block centers, offset by -0.5 on each even axis
        Vector3f[] blockCenters = new Vector3f[sizeX * sizeY * sizeZ];
        float[] halfSize = new float[]
        {
                (sizeX >> 1) - ((sizeX & 1) == 0 ? 0.5f : 0),
                (sizeY >> 1) - ((sizeY & 1) == 0 ? 0.5f : 0),
                (sizeZ >> 1) - ((sizeZ & 1) == 0 ? 0.5f : 0)
        };
        for (int z = 0; z < sizeZ; z++)
        {
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    blockCenters[getArrayIndex(x, y, z, sizeX, sizeY, sizeZ)] = new Vector3f(x - halfSize[0], y - halfSize[1], z - halfSize[2]);
                }
            }
        }

        // Convert block centers to mask values by calling isPositionInShape()
        boolean[] mask = new boolean[sizeX * sizeY * sizeZ];
        for (int i = 0; i < mask.length; i++)
        {
            Vector3f center = blockCenters[i];
            mask[i] = isPositionInShape(center.getX(), center.getY(), center.getZ(), sizeX, sizeY, sizeZ);
        }
        return mask;
    }
}
