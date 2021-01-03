package keystone.modules.selection;

import keystone.core.renderer.common.models.Coords;
import keystone.core.renderer.common.models.SelectableBoundingBox;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

public class SelectedFace
{
    private Direction faceDirection;
    private SelectableBoundingBox box;
    private Coords relativeSelectedBlock;
    private double distance;

    private double selectionU;
    private double selectionV;

    public SelectedFace(SelectableBoundingBox box, Direction faceDirection, Vector3d selectionPoint, double distance)
    {
        this.faceDirection = faceDirection;
        this.box = box;
        this.distance = distance;

        if (faceDirection == Direction.UP || faceDirection == Direction.DOWN)
        {
            selectionU = selectionPoint.x;
            selectionV = selectionPoint.z;
        }
        if (faceDirection == Direction.NORTH || faceDirection == Direction.SOUTH)
        {
            selectionU = selectionPoint.x;
            selectionV = selectionPoint.y;
        }
        if (faceDirection == Direction.EAST || faceDirection == Direction.WEST)
        {
            selectionU = selectionPoint.y;
            selectionV = selectionPoint.z;
        }

        relativeSelectedBlock = new Coords(selectionPoint.getX() - box.getMinCoords().getX(), selectionPoint.getY() - box.getMinCoords().getY(), selectionPoint.getZ() - box.getMinCoords().getZ());
    }

    public Direction getFaceDirection() { return faceDirection; }
    public SelectableBoundingBox getBox() { return box; }
    public Coords getRelativeSelectedBlock() { return relativeSelectedBlock; }
    public double getDistance() { return distance; }
    public Vector3d getSelectionPoint()
    {
        if (faceDirection == Direction.UP) return new Vector3d(selectionU, box.getMaxCoords().getY() + 1, selectionV);
        if (faceDirection == Direction.DOWN) return new Vector3d(selectionU, box.getMinCoords().getY(), selectionV);
        if (faceDirection == Direction.SOUTH) return new Vector3d(selectionU, selectionV, box.getMaxCoords().getZ() + 1);
        if (faceDirection == Direction.NORTH) return new Vector3d(selectionU, selectionV, box.getMinCoords().getZ());
        if (faceDirection == Direction.EAST) return new Vector3d(box.getMaxCoords().getX() + 1, selectionU, selectionV);
        if (faceDirection == Direction.WEST) return new Vector3d(box.getMinCoords().getX(), selectionU, selectionV);

        return null;
    }
}
