package keystone.modules.selection;

import keystone.core.math.RayIntersections;
import keystone.core.renderer.client.Player;
import keystone.modules.selection.boxes.SelectionBoundingBox;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

public class SelectionFace
{
    public Direction direction;
    public SelectionBoundingBox selectionBox;
    public double distanceSqr;

    private double selectionU;
    private double selectionV;

    public SelectionFace(SelectionBoundingBox box, Direction direction, Vector3d selectionPoint, double distanceSqr)
    {
        this.direction = direction;
        this.selectionBox = box;
        this.distanceSqr = distanceSqr;

        if (direction == Direction.UP || direction == Direction.DOWN)
        {
            selectionU = selectionPoint.x;
            selectionV = selectionPoint.z;
        }
        if (direction == Direction.NORTH || direction == Direction.SOUTH)
        {
            selectionU = selectionPoint.x;
            selectionV = selectionPoint.y;
        }
        if (direction == Direction.EAST || direction == Direction.WEST)
        {
            selectionU = selectionPoint.y;
            selectionV = selectionPoint.z;
        }
    }
    public Vector3d getSelectionPoint()
    {
        if (direction == Direction.UP) return new Vector3d(selectionU, selectionBox.getMaxCoords().getY() + 1, selectionV);
        if (direction == Direction.DOWN) return new Vector3d(selectionU, selectionBox.getMinCoords().getY(), selectionV);
        if (direction == Direction.SOUTH) return new Vector3d(selectionU, selectionV, selectionBox.getMaxCoords().getZ() + 1);
        if (direction == Direction.NORTH) return new Vector3d(selectionU, selectionV, selectionBox.getMinCoords().getZ());
        if (direction == Direction.EAST) return new Vector3d(selectionBox.getMaxCoords().getX() + 1, selectionU, selectionV);
        if (direction == Direction.WEST) return new Vector3d(selectionBox.getMinCoords().getX(), selectionU, selectionV);

        return null;
    }
    public void drag()
    {
        // Get perpendicular plane through selection point
        Vector3d point = getSelectionPoint();
        Vector3d normal = new Vector3d(0, 1, 0);
        if (direction == Direction.UP || direction == Direction.DOWN)
        {
            normal = new Vector3d(0, 0, 1);
            if (Math.abs(Player.getLookDirection().dotProduct(new Vector3d(1, 0, 0))) > Math.abs(Player.getLookDirection().dotProduct(normal))) normal = new Vector3d(1, 0, 0);
            if (Player.getLookDirection().dotProduct(normal) < 0) normal = normal.scale(-1);
        }
        if (direction == Direction.NORTH || direction == Direction.SOUTH)
        {
            normal = new Vector3d(1, 0, 0);
            if (Math.abs(Player.getLookDirection().dotProduct(new Vector3d(0, 1, 0))) > Math.abs(Player.getLookDirection().dotProduct(normal))) normal = new Vector3d(0, 1, 0);
            if (Player.getLookDirection().dotProduct(normal) < 0) normal = normal.scale(-1);
        }
        if (direction == Direction.WEST || direction == Direction.EAST)
        {
            normal = new Vector3d(0, 0, 1);
            if (Math.abs(Player.getLookDirection().dotProduct(new Vector3d(0, 1, 0))) > Math.abs(Player.getLookDirection().dotProduct(normal))) normal = new Vector3d(0, 1, 0);
            if (Player.getLookDirection().dotProduct(normal) < 0) normal = normal.scale(-1);
        }

        // Cast ray onto plane
        Vector3d projectedPoint = RayIntersections.rayPlaneIntersection(Player.getEyePosition(), Player.getLookDirection(), point, normal);
        if (projectedPoint == null) return;

        // Do dragging
        if (direction == Direction.UP || direction == Direction.DOWN) selectionBox.moveFace(direction, (int)projectedPoint.getY());
        if (direction == Direction.NORTH || direction == Direction.SOUTH) selectionBox.moveFace(direction, (int)projectedPoint.getZ());
        if (direction == Direction.WEST || direction == Direction.EAST) selectionBox.moveFace(direction, (int)projectedPoint.getX());
    }
}
