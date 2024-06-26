package keystone.core.modules.brush;

import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public abstract class BrushShape
{
    public static final List<BrushShape> VALUES = new ArrayList<>();
    private final int listIndex;

    //region Default Shapes
    public static final BrushShape ROUND = new BrushShape()
    {
        public Text getName() { return Text.translatable("keystone.shape.round"); }
        protected boolean isLocalizedPositionInShape(float x, float y, float z, float sizeX, float sizeY, float sizeZ)
        {
            float nX = (2 * x / sizeX);
            float nY = (2 * y / sizeY);
            float nZ = (2 * z / sizeZ);
            return nX * nX + nY * nY + nZ * nZ < 1;
        }
    };
    public static final BrushShape DIAMOND = new BrushShape()
    {
        public Text getName() { return Text.translatable("keystone.shape.diamond"); }
        protected boolean isLocalizedPositionInShape(float x, float y, float z, float sizeX, float sizeY, float sizeZ)
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
        public Text getName() { return Text.translatable("keystone.shape.square"); }
        protected boolean isLocalizedPositionInShape(float x, float y, float z, float sizeX, float sizeY, float sizeZ) { return true; }
    };
    //endregion

    protected BrushShape()
    {
        listIndex = VALUES.size();
        VALUES.add(this);
    }

    public abstract Text getName();
    protected abstract boolean isLocalizedPositionInShape(float x, float y, float z, float sizeX, float sizeY, float sizeZ);

    public final ShapeMask getShapeMask(int sizeX, int sizeY, int sizeZ)
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
        byte[] mask = new byte[sizeX * sizeY * sizeZ];
        for (int i = 0; i < mask.length; i++)
        {
            Vector3f center = blockCenters[i];
            mask[i] = isLocalizedPositionInShape(center.x, center.y, center.z, sizeX, sizeY, sizeZ) ? (byte)2 : (byte)0;
        }
        for (int z = 1; z < sizeZ - 1; z++)
        {
            for (int y = 1; y < sizeY - 1; y++)
            {
                for (int x = 1; x < sizeX - 1; x++)
                {
                    byte neighbors = (byte)0;
                    if (mask[getArrayIndex(x + 1, y, z, sizeX, sizeY, sizeZ)] > 0) neighbors++;
                    if (mask[getArrayIndex(x - 1, y, z, sizeX, sizeY, sizeZ)] > 0) neighbors++;
                    if (mask[getArrayIndex(x, y + 1, z, sizeX, sizeY, sizeZ)] > 0) neighbors++;
                    if (mask[getArrayIndex(x, y - 1, z, sizeX, sizeY, sizeZ)] > 0) neighbors++;
                    if (mask[getArrayIndex(x, y, z + 1, sizeX, sizeY, sizeZ)] > 0) neighbors++;
                    if (mask[getArrayIndex(x, y, z - 1, sizeX, sizeY, sizeZ)] > 0) neighbors++;

                    if (neighbors == 6) mask[getArrayIndex(x, y, z, sizeX, sizeY, sizeZ)] = (byte)1;
                }
            }
        }
        return new ShapeMask(new Vec3i(sizeX, sizeY, sizeZ), mask);
    }
    public final boolean isPositionInShape(Vec3d position, Vec3i center, int sizeX, int sizeY, int sizeZ)
    {
        float x = (float)(position.x - center.getX() + sizeX * 0.5);
        float y = (float)(position.y - center.getY() + sizeY * 0.5);
        float z = (float)(position.z - center.getZ() + sizeZ * 0.5);
        return isPositionInShape(x, y, z, sizeX, sizeY, sizeZ);
    }
    public final BrushShape getNextShape()
    {
        return VALUES.get((listIndex + 1) % VALUES.size());
    }

    //region Helper Functions
    private int getArrayIndex(int x, int y, int z, int sizeX, int sizeY, int sizeZ)
    {
        return x + y * sizeX + z * sizeX * sizeY;
    }
    private final boolean isPositionInShape(float x, float y, float z, int sizeX, int sizeY, int sizeZ)
    {
        float[] halfSize = new float[]
        {
                (sizeX >> 1) - ((sizeX & 1) == 0 ? 0.5f : 0),
                (sizeY >> 1) - ((sizeY & 1) == 0 ? 0.5f : 0),
                (sizeZ >> 1) - ((sizeZ & 1) == 0 ? 0.5f : 0)
        };
        return isLocalizedPositionInShape(x - halfSize[0], y - halfSize[1], z - halfSize[2], sizeX, sizeY, sizeZ);
    }
    //endregion
}
