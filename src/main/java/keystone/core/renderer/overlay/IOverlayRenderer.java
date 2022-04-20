package keystone.core.renderer.overlay;

import keystone.core.renderer.Color4f;
import keystone.core.renderer.RenderBox;
import keystone.core.renderer.interfaces.IAlphaProvider;
import keystone.core.renderer.interfaces.IColorProvider;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public interface IOverlayRenderer
{
    void drawCuboid(RenderBox box, IColorProvider colorProvider, IAlphaProvider alphaProvider);
    void drawGrid(Vec3d min, Vec3i size, double scale, IColorProvider colorProvider, IAlphaProvider alphaProvider, float lineWidth, boolean drawEdges);
    void drawPlane(Vec3d center, Direction planeNormal, double gridScale, IColorProvider colorProvider, IAlphaProvider fillAlphaProvider, IAlphaProvider lineAlphaProvider, float lineWidth);
    
    default void drawCuboid(RenderBox box, Color4f color) { drawCuboid(box, direction -> color, direction -> color.a); }
    default void drawGrid(Vec3d min, Vec3i size, double scale, Color4f color, float lineWidth, boolean drawEdges) { drawGrid(min, size, scale, direction -> color, direction -> color.a, lineWidth, drawEdges); }
    default void drawPlane(Vec3d center, Direction planeNormal, double gridScale, Color4f color, float lineAlpha, float lineWidth) { drawPlane(center, planeNormal, gridScale, direction -> color, direction -> color.a, direction -> lineAlpha, lineWidth); }
    
    default void modifyRenderer(Direction direction) { }
}
