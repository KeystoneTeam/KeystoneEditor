package keystone.core.modules.selection.boxes;

import keystone.core.renderer.client.Player;
import keystone.core.renderer.common.BoundingBoxType;
import keystone.core.renderer.common.models.AbstractBoundingBox;
import keystone.core.renderer.common.models.Coords;

public class HighlightBoundingBox extends AbstractBoundingBox
{
    public HighlightBoundingBox()
    {
        super(BoundingBoxType.get("highlight_box"));
    }

    @Override
    public Boolean intersectsBounds(int minX, int minZ, int maxX, int maxZ)
    {
        Coords pos = Player.getHighlightedBlock();
        return pos.getX() >= minX && pos.getX() <= maxX && pos.getZ() >= minZ && pos.getZ() <= maxZ;
    }

    @Override
    protected double getDistanceX(double x)
    {
        return x - Player.getHighlightedBlock().getX();
    }
    @Override
    protected double getDistanceY(double y)
    {
        return y - Player.getHighlightedBlock().getY();
    }
    @Override
    protected double getDistanceZ(double z)
    {
        return z - Player.getHighlightedBlock().getZ();
    }
}
