package keystone.core.renderer.client.renderers;

import keystone.core.renderer.client.Camera;
import keystone.core.renderer.client.models.Point;
import keystone.core.renderer.common.models.Coords;

public class OffsetPoint
{
    private final Point point;

    public OffsetPoint(double x, double y, double z)
    {
        this(new Point(x, y, z));
    }
    public OffsetPoint(Coords coords)
    {
        this(new Point(coords));
    }
    public OffsetPoint(Point point)
    {
        this.point = point;
    }

    public double getX()
    {
        return point.getX() - Camera.getX();
    }
    public double getY()
    {
        return point.getY() - Camera.getY();
    }
    public double getZ()
    {
        return point.getZ() - Camera.getZ();
    }

    public OffsetPoint offset(double x, double y, double z)
    {
        return new OffsetPoint(point.offset(x, y, z));
    }
    public double getDistance(OffsetPoint offsetPoint)
    {
        return this.point.getDistance(offsetPoint.point);
    }
}
