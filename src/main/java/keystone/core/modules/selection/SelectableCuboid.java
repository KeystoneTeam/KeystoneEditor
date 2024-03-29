package keystone.core.modules.selection;

import keystone.core.client.Player;
import keystone.core.math.RayTracing;
import keystone.core.renderer.shapes.Cuboid;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.List;

public abstract class SelectableCuboid extends Cuboid
{
    private boolean selectable;

    protected SelectableCuboid(Vec3i corner1, Vec3i corner2)
    {
        super(corner1, corner2);
        this.selectable = true;
    }

    public boolean isSelectable() { return selectable; }
    public final void setSelectable(boolean selectable) { this.selectable = selectable; }

    public int getPriority() { return 0; }
    public abstract boolean isEnabled();
    public void startDrag(SelectedFace face) {}
    public abstract boolean drag(SelectedFace face);
    public void endDrag(SelectedFace face) {}

    public void getSelectedFaces(List<SelectedFace> result)
    {
        if (!selectable) return;

        Vec3d origin = Player.getEyePosition();
        Vec3d heading = Player.getLookDirection();

        for (Direction direction : Direction.values())
        {
            Vec3d intersection = RayTracing.rayFaceIntersection(origin, heading, getMin(), getMax().add(1, 1, 1), direction);
            if (intersection != null)
            {
                double distance = origin.distanceTo(intersection);
                result.add(new SelectedFace(this, direction, intersection, distance));
            }
        }
    }
}
