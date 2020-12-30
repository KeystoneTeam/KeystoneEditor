package keystone.modules.selection.boxes;

import keystone.core.renderer.client.Player;
import keystone.core.renderer.common.BoundingBoxType;
import keystone.core.renderer.common.models.AbstractBoundingBox;
import keystone.core.renderer.common.models.Coords;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;

public class HighlightBoundingBox extends AbstractBoundingBox
{
    public HighlightBoundingBox()
    {
        super(BoundingBoxType.get("highlight_box"));
    }

    public Coords getCoords()
    {
        RayTraceResult ray = Player.getRayTrace();
        if (ray.getType() != RayTraceResult.Type.BLOCK) return Player.getCoords();
        else
        {
            BlockRayTraceResult blockRay = (BlockRayTraceResult)ray;
            return new Coords(blockRay.getPos());
        }
    }

    @Override
    public Boolean intersectsBounds(int minX, int minZ, int maxX, int maxZ)
    {
        Coords pos = getCoords();
        return pos.getX() >= minX && pos.getX() <= maxX && pos.getZ() >= minZ && pos.getZ() <= maxZ;
    }

    @Override
    protected double getDistanceX(double x)
    {
        return x - getCoords().getX();
    }
    @Override
    protected double getDistanceY(double y)
    {
        return y - getCoords().getY();
    }
    @Override
    protected double getDistanceZ(double z)
    {
        return z - getCoords().getZ();
    }
}
