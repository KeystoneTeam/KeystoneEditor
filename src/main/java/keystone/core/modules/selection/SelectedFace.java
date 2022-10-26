package keystone.core.modules.selection;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class SelectedFace
{
    private Direction faceDirection;
    private SelectableCuboid box;
    private Vec3i relativeSelectedBlock;
    private double distance;

    private double selectionU;
    private double selectionV;
    private double internalDistance;
    private double closestEdgeLength;
    private boolean isDraggingFace;

    public SelectedFace(SelectableCuboid box, Direction faceDirection, Vec3d selectionPoint, double distance)
    {
        this.faceDirection = faceDirection;
        this.box = box;
        this.distance = distance;

        if (faceDirection == Direction.UP || faceDirection == Direction.DOWN)
        {
            selectionU = selectionPoint.x;
            selectionV = selectionPoint.z;

            double xDistance = Math.min(Math.abs(selectionU - box.getMin().getX()), Math.abs(box.getMax().getX() - selectionU));
            double zDistance = Math.min(Math.abs(selectionV - box.getMin().getZ()), Math.abs(box.getMax().getZ() - selectionV));
            if (xDistance < zDistance)
            {
                internalDistance = xDistance;
                closestEdgeLength = box.getSize().getX();
            }
            else
            {
                internalDistance = zDistance;
                closestEdgeLength = box.getSize().getZ();
            }
        }
        if (faceDirection == Direction.NORTH || faceDirection == Direction.SOUTH)
        {
            selectionU = selectionPoint.x;
            selectionV = selectionPoint.y;

            double xDistance = Math.min(Math.abs(selectionU - box.getMin().getX()), Math.abs(box.getMax().getX() - selectionU));
            double yDistance = Math.min(Math.abs(selectionU - box.getMin().getY()), Math.abs(box.getMax().getY() - selectionU));
            if (xDistance < yDistance)
            {
                internalDistance = xDistance;
                closestEdgeLength = box.getSize().getX();
            }
            else
            {
                internalDistance = yDistance;
                closestEdgeLength = box.getSize().getY();
            }
        }
        if (faceDirection == Direction.EAST || faceDirection == Direction.WEST)
        {
            selectionU = selectionPoint.y;
            selectionV = selectionPoint.z;

            double yDistance = Math.min(Math.abs(selectionU - box.getMin().getY()), Math.abs(box.getMax().getY() - selectionU));
            double zDistance = Math.min(Math.abs(selectionV - box.getMin().getZ()), Math.abs(box.getMax().getZ() - selectionV));
            if (yDistance < zDistance)
            {
                internalDistance = yDistance;
                closestEdgeLength = box.getSize().getY();
            }
            else
            {
                internalDistance = zDistance;
                closestEdgeLength = box.getSize().getZ();
            }
        }

        isDraggingFace = false;
        relativeSelectedBlock = new Vec3i(selectionPoint.x - box.getMin().getX(), selectionPoint.y - box.getMin().getY(), selectionPoint.z - box.getMin().getZ());
    }

    public void startDrag() { this.isDraggingFace = true; }
    public void endDrag() { this.isDraggingFace = false; }
    public void swapFaceDirection() { faceDirection = faceDirection.getOpposite(); }
    
    public Direction getFaceDirection() { return faceDirection; }
    public SelectableCuboid getBox() { return box; }
    public Vec3i getRelativeSelectedBlock() { return relativeSelectedBlock; }
    public double getDistance() { return distance; }
    public double getInternalDistance() { return internalDistance; }
    public double getClosestEdgeLength() { return closestEdgeLength; }
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
