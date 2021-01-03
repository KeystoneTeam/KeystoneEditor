package keystone.modules.selection.boxes;

import keystone.core.math.RayIntersections;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.common.BoundingBoxType;
import keystone.core.renderer.common.models.Coords;
import keystone.core.renderer.common.models.SelectableBoundingBox;
import keystone.modules.selection.SelectedFace;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

public class SelectionBoundingBox extends SelectableBoundingBox
{
    protected SelectionBoundingBox(Coords corner1, Coords corner2)
    {
        super(corner1, corner2, BoundingBoxType.get("selection_box"));
    }
    public static SelectionBoundingBox startNew(Coords coords)
    {
        return new SelectionBoundingBox(coords, coords);
    }

    public SelectionBoundingBox clone()
    {
        return new SelectionBoundingBox(getCorner1().clone(), getCorner2().clone());
    }

    @Override
    public int getPriority()
    {
        return 1000;
    }
    @Override
    public void drag(SelectedFace face)
    {
        // Get perpendicular plane through selection point
        Vector3d point = face.getSelectionPoint();
        Vector3d normal = new Vector3d(0, 1, 0);
        if (face.getFaceDirection() == Direction.UP || face.getFaceDirection() == Direction.DOWN)
        {
            normal = new Vector3d(0, 0, 1);
            if (Math.abs(Player.getLookDirection().dotProduct(new Vector3d(1, 0, 0))) > Math.abs(Player.getLookDirection().dotProduct(normal))) normal = new Vector3d(1, 0, 0);
            if (Player.getLookDirection().dotProduct(normal) < 0) normal = normal.scale(-1);
        }
        if (face.getFaceDirection() == Direction.NORTH || face.getFaceDirection() == Direction.SOUTH)
        {
            normal = new Vector3d(1, 0, 0);
            if (Math.abs(Player.getLookDirection().dotProduct(new Vector3d(0, 1, 0))) > Math.abs(Player.getLookDirection().dotProduct(normal))) normal = new Vector3d(0, 1, 0);
            if (Player.getLookDirection().dotProduct(normal) < 0) normal = normal.scale(-1);
        }
        if (face.getFaceDirection() == Direction.WEST || face.getFaceDirection() == Direction.EAST)
        {
            normal = new Vector3d(0, 0, 1);
            if (Math.abs(Player.getLookDirection().dotProduct(new Vector3d(0, 1, 0))) > Math.abs(Player.getLookDirection().dotProduct(normal))) normal = new Vector3d(0, 1, 0);
            if (Player.getLookDirection().dotProduct(normal) < 0) normal = normal.scale(-1);
        }

        // Cast ray onto plane
        Vector3d projectedPoint = RayIntersections.rayPlaneIntersection(Player.getEyePosition(), Player.getLookDirection(), point, normal);
        if (projectedPoint == null) return;

        // Do dragging
        if (face.getFaceDirection() == Direction.UP || face.getFaceDirection() == Direction.DOWN) moveFace(face.getFaceDirection(), (int)projectedPoint.getY());
        if (face.getFaceDirection() == Direction.NORTH || face.getFaceDirection() == Direction.SOUTH) moveFace(face.getFaceDirection(), (int)projectedPoint.getZ());
        if (face.getFaceDirection() == Direction.WEST || face.getFaceDirection() == Direction.EAST) moveFace(face.getFaceDirection(), (int)projectedPoint.getX());
    }
}
