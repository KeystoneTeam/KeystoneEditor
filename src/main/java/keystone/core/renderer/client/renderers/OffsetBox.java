package keystone.core.renderer.client.renderers;

import keystone.core.renderer.common.models.Coords;

public class OffsetBox
{
    private static final double nudgeSize = 0.01F;

    private final OffsetPoint min;
    private final OffsetPoint max;
    private final Coords size;
    private final OffsetPoint center;

    public OffsetBox(Coords minCoords, Coords maxCoords)
    {
        this.min = new OffsetPoint(minCoords);
        this.max = new OffsetPoint(maxCoords).offset(1, 1, 1);
        this.size = new Coords(maxCoords.getX() - minCoords.getX(), maxCoords.getY() - minCoords.getY(), maxCoords.getZ() - minCoords.getZ()).add(1, 1, 1);
        this.center = this.min.offset(this.size.getX() * 0.5, this.size.getY() * 0.5, this.size.getZ() * 0.5);
    }
    public OffsetBox(OffsetPoint min, OffsetPoint max)
    {
        this.min = min;
        this.max = max;
        this.size = new Coords(max.getPoint().getX() - min.getPoint().getX(), max.getPoint().getY() - min.getPoint().getY(), max.getPoint().getZ() - min.getPoint().getZ()).add(1, 1, 1);
        this.center = this.min.offset(this.size.getX() * 0.5, this.size.getY() * 0.5, this.size.getZ() * 0.5);
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
    public Coords getSize() { return size; }
    public OffsetPoint getCenter() { return center; }
}
