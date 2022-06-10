package keystone.core.modules.selection;

import keystone.api.Keystone;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.core.client.Player;
import keystone.core.events.keystone.KeystoneLifecycleEvents;
import keystone.core.math.RayTracing;
import keystone.core.modules.history.HistoryModule;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class SelectionBoundingBox extends SelectableCuboid
{
    private final SelectionModule selectionModule;

    protected SelectionBoundingBox(Vec3i corner1, Vec3i corner2)
    {
        super(corner1, corner2);
        this.selectionModule = Keystone.getModule(SelectionModule.class);
    }
    public static SelectionBoundingBox startNew(Vec3i coords)
    {
        return new SelectionBoundingBox(coords, coords);
    }
    public static SelectionBoundingBox createFromBoundingBox(BoundingBox boundingBox)
    {
        Vec3i corner1 = new Vec3i(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
        Vec3i corner2 = new Vec3i(boundingBox.maxX - 1, boundingBox.maxY - 1, boundingBox.maxZ - 1);
        return new SelectionBoundingBox(corner1, corner2);
    }
    public static SelectionBoundingBox create(int corner1X, int corner1Y, int corner1Z, int corner2X, int corner2Y, int corner2Z)
    {
        Vec3i corner1 = new Vec3i(corner1X, corner1Y, corner1Z);
        Vec3i corner2 = new Vec3i(corner2X, corner2Y, corner2Z);
        return new SelectionBoundingBox(corner1, corner2);
    }

    public SelectionBoundingBox clone()
    {
        return new SelectionBoundingBox(getCorner1(), getCorner2());
    }

    @Override
    public int getPriority()
    {
        return 1000;
    }
    @Override
    public boolean isEnabled() { return selectionModule.isEnabled(); }
    @Override
    public void drag(SelectedFace face)
    {
        // Get perpendicular plane through selection point
        Vec3d point = face.getSelectionPoint();
        Vec3d normal = new Vec3d(0, 1, 0);
        if (face.getFaceDirection() == Direction.UP || face.getFaceDirection() == Direction.DOWN)
        {
            normal = new Vec3d(0, 0, 1);
            if (Math.abs(Player.getLookDirection().dotProduct(new Vec3d(1, 0, 0))) > Math.abs(Player.getLookDirection().dotProduct(normal))) normal = new Vec3d(1, 0, 0);
            if (Player.getLookDirection().dotProduct(normal) < 0) normal = normal.multiply(-1);
        }
        if (face.getFaceDirection() == Direction.NORTH || face.getFaceDirection() == Direction.SOUTH)
        {
            normal = new Vec3d(1, 0, 0);
            if (Math.abs(Player.getLookDirection().dotProduct(new Vec3d(0, 1, 0))) > Math.abs(Player.getLookDirection().dotProduct(normal))) normal = new Vec3d(0, 1, 0);
            if (Player.getLookDirection().dotProduct(normal) < 0) normal = normal.multiply(-1);
        }
        if (face.getFaceDirection() == Direction.WEST || face.getFaceDirection() == Direction.EAST)
        {
            normal = new Vec3d(0, 0, 1);
            if (Math.abs(Player.getLookDirection().dotProduct(new Vec3d(0, 1, 0))) > Math.abs(Player.getLookDirection().dotProduct(normal))) normal = new Vec3d(0, 1, 0);
            if (Player.getLookDirection().dotProduct(normal) < 0) normal = normal.multiply(-1);
        }

        // Cast ray onto plane
        Vec3d projectedPoint = RayTracing.rayPlaneIntersection(Player.getEyePosition(), Player.getLookDirection(), point, normal);
        if (projectedPoint == null) return;

        // Do dragging
        if (face.getFaceDirection() == Direction.UP || face.getFaceDirection() == Direction.DOWN) moveFace(face.getFaceDirection(), (int)projectedPoint.y);
        if (face.getFaceDirection() == Direction.NORTH || face.getFaceDirection() == Direction.SOUTH) moveFace(face.getFaceDirection(), (int)projectedPoint.z);
        if (face.getFaceDirection() == Direction.WEST || face.getFaceDirection() == Direction.EAST) moveFace(face.getFaceDirection(), (int)projectedPoint.x);

        // Post event
        KeystoneLifecycleEvents.SELECTION_CHANGED.invoker().selectionChanged(selectionModule.getSelectionBoundingBoxes(), false, false);
    }
    @Override
    public void endDrag(SelectedFace face)
    {
        HistoryModule historyModule = Keystone.getModule(HistoryModule.class);
        historyModule.beginHistoryEntry();
        selectionModule.addHistoryEntry();
        historyModule.endHistoryEntry();
    }
}
