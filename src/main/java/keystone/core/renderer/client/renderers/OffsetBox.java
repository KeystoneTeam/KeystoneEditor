package keystone.core.renderer.client.renderers;

import keystone.core.renderer.common.models.Coords;

public class OffsetBox
{
    private static final double nudgeSize = 0.01F;

    private final OffsetPoint min;
    private final OffsetPoint max;

    public OffsetBox(Coords minCoords, Coords maxCoords)
    {
        this.min = new OffsetPoint(minCoords);
        this.max = new OffsetPoint(maxCoords).offset(1, 1, 1);
    }

    public OffsetBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
    {
        this.min = new OffsetPoint(minX, minY, minZ);
        this.max = new OffsetPoint(maxX, maxY, maxZ);
    }

    public OffsetBox(OffsetPoint min, OffsetPoint max)
    {
        this.min = min;
        this.max = max;
    }

    public OffsetBox grow(double x, double y, double z)
    {
        return new OffsetBox(min.offset(-x, -y, -z), max.offset(x, y, z));
    }

    public OffsetBox nudge()
    {
        if (min.getY() == max.getY())
        {
            return new OffsetBox(min.offset(-nudgeSize, nudgeSize, -nudgeSize), max.offset(nudgeSize, nudgeSize, nudgeSize));
        }
        return grow(nudgeSize, nudgeSize, nudgeSize);
    }

    public OffsetPoint getMin()
    {
        return min;
    }

    public OffsetPoint getMax()
    {
        return max;
    }
}
