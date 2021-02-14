package keystone.core.modules.brush.boxes;

import keystone.core.renderer.common.BoundingBoxType;
import keystone.core.renderer.common.models.AbstractBoundingBox;
import keystone.core.renderer.common.models.Coords;

public class BrushPositionBox extends AbstractBoundingBox
{
    private Coords position;

    public BrushPositionBox(Coords position)
    {
        super(BoundingBoxType.get("brush_position"));
        this.position = position;
    }

    @Override
    public Boolean intersectsBounds(int minX, int minZ, int maxX, int maxZ)
    {
        return position.getX() >= minX && position.getX() <= maxX && position.getZ() >= minZ && position.getZ() <= maxZ;
    }

    public Coords getPosition() { return position; }

    @Override
    protected double getDistanceX(double x)
    {
        return x - position.getX();
    }
    @Override
    protected double getDistanceY(double y)
    {
        return y - position.getY();
    }
    @Override
    protected double getDistanceZ(double z)
    {
        return z - position.getZ();
    }
}