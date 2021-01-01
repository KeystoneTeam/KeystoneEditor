package keystone.modules.selection.boxes;

import keystone.core.math.RayIntersections;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.common.BoundingBoxType;
import keystone.core.renderer.common.models.BoundingBoxCuboid;
import keystone.core.renderer.common.models.Coords;
import keystone.modules.selection.SelectionFace;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

public class SelectionBoundingBox extends BoundingBoxCuboid
{
    protected SelectionBoundingBox(Coords minCoords, Coords maxCoords)
    {
        super(minCoords, maxCoords, BoundingBoxType.get("selection_box"));
    }
    public static SelectionBoundingBox startNew(Coords coords)
    {
        return new SelectionBoundingBox(coords, coords);
    }

    public SelectionFace getSelectedFace()
    {
        Vector3d origin = Player.getEyePosition();
        Vector3d heading = Player.getLookDirection();

        SelectionFace closest = null;
        for (Direction direction : Direction.values())
        {
            Vector3d intersection = RayIntersections.rayFaceIntersection(origin, heading, getMinCoords(), getMaxCoords().add(new Vector3d(1, 1, 1)), direction);
            if (intersection != null)
            {
                double distanceSqr = origin.squareDistanceTo(intersection);
                if (closest == null || closest.distanceSqr > distanceSqr) closest = new SelectionFace(this, direction, intersection, distanceSqr);
            }
        }
        return closest;
    }
}
