package keystone.core.renderer.overlay;

import keystone.core.renderer.Color4f;
import keystone.core.renderer.interfaces.IAlphaProvider;
import keystone.core.renderer.interfaces.IColorProvider;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public interface IOverlayRenderer
{
    void drawCuboid(Box box, IColorProvider colorProvider, IAlphaProvider alphaProvider);
    void drawGrid(Vec3d min, Vec3i size, double scale, IColorProvider colorProvider, IAlphaProvider alphaProvider, boolean drawEdges);
    void drawPlane(Vec3d center, Direction planeNormal, double gridScale, IColorProvider colorProvider, IAlphaProvider alphaProvider);
    void drawDiamond(Vec3d center, double xRadius, double yRadius, double zRadius, Color4f color);
    void drawSphere(Vec3d center, double xRadius, double yRadius, double zRadius, Color4f color);

    default void drawCuboid(Box box, Color4f color) { drawCuboid(box, direction -> color, direction -> color.a); }
    default void drawGrid(Vec3d min, Vec3i size, double scale, Color4f color, boolean drawEdges) { drawGrid(min, size, scale, direction -> color, direction -> color.a, drawEdges); }
    default void drawPlane(Vec3d center, Direction planeNormal, double gridScale, Color4f color) { drawPlane(center, planeNormal, gridScale, direction -> color, direction -> color.a); }
}
