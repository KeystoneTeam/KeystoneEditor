package keystone.core.modules.selection;

import keystone.core.renderer.shapes.SelectableBoundingBox;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class SelectedFace
{
    private Direction faceDirection;
    private SelectableBoundingBox box;
    private Vec3i relativeSelectedBlock;
    private double distance;

    private double selectionU;
    private double selectionV;
    private double internalDistance;
    private boolean isDraggingFace;

    public SelectedFace(SelectableBoundingBox box, Direction faceDirection, Vec3d selectionPoint, double distance)
    {
        this.faceDirection = faceDirection;
        this.box = box;
        this.distance = distance;

        if (faceDirection == Direction.UP || faceDirection == Direction.DOWN)
        {
            selectionU = selectionPoint.x;
            selectionV = selectionPoint.z;

            double distance1 = Math.abs(selectionU - box.getMin().getX());
            double distance2 = Math.abs(box.getMax().getX() - selectionU);
            double distance3 = Math.abs(selectionV - box.getMin().getZ());
            double distance4 = Math.abs(box.getMax().getZ() - selectionV);
            internalDistance = Math.min(Math.min(distance1, distance2), Math.min(distance3, distance4));
        }
        if (faceDirection == Direction.NORTH || faceDirection == Direction.SOUTH)
        {
            selectionU = selectionPoint.x;
            selectionV = selectionPoint.y;

            double distance1 = Math.abs(selectionU - box.getMin().getX());
            double distance2 = Math.abs(box.getMax().getX() - selectionU);
            double distance3 = Math.abs(selectionV - box.getMin().getY());
            double distance4 = Math.abs(box.getMax().getY() - selectionV);
            internalDistance = Math.min(Math.min(distance1, distance2), Math.min(distance3, distance4));
        }
        if (faceDirection == Direction.EAST || faceDirection == Direction.WEST)
        {
            selectionU = selectionPoint.y;
            selectionV = selectionPoint.z;

            double distance1 = Math.abs(selectionU - box.getMin().getY());
            double distance2 = Math.abs(box.getMax().getY() - selectionU);
            double distance3 = Math.abs(selectionV - box.getMin().getZ());
            double distance4 = Math.abs(box.getMax().getZ() - selectionV);
            internalDistance = Math.min(Math.min(distance1, distance2), Math.min(distance3, distance4));
        }

        isDraggingFace = false;
        relativeSelectedBlock = new Vec3i(selectionPoint.x - box.getMin().getX(), selectionPoint.y - box.getMin().getY(), selectionPoint.z - box.getMin().getZ());
    }

    public void startDrag() { this.isDraggingFace = true; }
    public void endDrag() { this.isDraggingFace = false; }

    public Direction getFaceDirection() { return faceDirection; }
    public SelectableBoundingBox getBox() { return box; }
    public Vec3i getRelativeSelectedBlock() { return relativeSelectedBlock; }
    public double getDistance() { return distance; }
    public double getInternalDistance() { return internalDistance; }
    public Vec3d getSelectionPoint()
    {
        if (faceDirection == Direction.UP) return new Vec3d(selectionU, box.getMax().getY() + 1, selectionV);
        if (faceDirection == Direction.DOWN) return new Vec3d(selectionU, box.getMin().getY(), selectionV);
        if (faceDirection == Direction.SOUTH) return new Vec3d(selectionU, selectionV, box.getMax().getZ() + 1);
        if (faceDirection == Direction.NORTH) return new Vec3d(selectionU, selectionV, box.getMin().getZ());
        if (faceDirection == Direction.EAST) return new Vec3d(box.getMax().getX() + 1, selectionU, selectionV);
        if (faceDirection == Direction.WEST) return new Vec3d(box.getMin().getX(), selectionU, selectionV);

        return null;
    }
    public boolean isDraggingFace() { return isDraggingFace; }
}
