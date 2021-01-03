package keystone.core.renderer.common.models;

import keystone.core.math.RayIntersections;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.common.BoundingBoxType;
import keystone.modules.selection.SelectedFace;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

public abstract class SelectableBoundingBox extends BoundingBoxCuboid
{
    protected SelectableBoundingBox(Coords corner1, Coords corner2, BoundingBoxType type)
    {
        super(corner1, corner2, type);
    }

    public int getPriority() { return 0; }
    public abstract void drag(SelectedFace face);

    public SelectedFace getSelectedFace()
    {
        Vector3d origin = Player.getEyePosition();
        Vector3d heading = Player.getLookDirection();

        SelectedFace closest = null;
        for (Direction direction : Direction.values())
        {
            Vector3d intersection = RayIntersections.rayFaceIntersection(origin, heading, getMinCoords(), getMaxCoords().add(new Vector3d(1, 1, 1)), direction);
            if (intersection != null)
            {
                double distanceSqr = origin.distanceTo(intersection);
                if (closest == null || closest.getDistance() > distanceSqr) closest = new SelectedFace(this, direction, intersection, distanceSqr);
            }
        }
        return closest;
    }
}
