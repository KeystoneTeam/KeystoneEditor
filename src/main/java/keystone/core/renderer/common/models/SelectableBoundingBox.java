package keystone.core.renderer.common.models;

import keystone.core.math.RayTracing;
import keystone.core.modules.selection.SelectedFace;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.common.BoundingBoxType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

public abstract class SelectableBoundingBox extends BoundingBoxCuboid
{
    private boolean selectable;

    protected SelectableBoundingBox(Coords corner1, Coords corner2, BoundingBoxType type)
    {
        super(corner1, corner2, type);
        this.selectable = true;
    }

    public boolean isSelectable() { return selectable; }
    public final void setSelectable(boolean selectable) { this.selectable = selectable; }

    public int getPriority() { return 0; }
    public abstract boolean isEnabled();
    public void startDrag(SelectedFace face) {}
    public abstract void drag(SelectedFace face);
    public void endDrag(SelectedFace face) {}

    public SelectedFace getSelectedFace()
    {
        if (!selectable) return null;

        Vector3d origin = Player.getEyePosition();
        Vector3d heading = Player.getLookDirection();

        SelectedFace closest = null;
        for (Direction direction : Direction.values())
        {
            Vector3d intersection = RayTracing.rayFaceIntersection(origin, heading, getMinCoords(), getMaxCoords().add(new Vector3d(1, 1, 1)), direction);
            if (intersection != null)
            {
                double distanceSqr = origin.distanceTo(intersection);
                if (closest == null || closest.getDistance() > distanceSqr) closest = new SelectedFace(this, direction, intersection, distanceSqr);
            }
        }
        return closest;
    }
}
