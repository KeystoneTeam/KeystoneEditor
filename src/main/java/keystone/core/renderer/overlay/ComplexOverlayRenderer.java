package keystone.core.renderer.overlay;

import keystone.api.Keystone;
import keystone.core.renderer.Color4f;
import keystone.core.renderer.RenderBox;
import keystone.core.renderer.interfaces.IAlphaProvider;
import keystone.core.renderer.interfaces.IColorProvider;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.Collections;
import java.util.Map;

public class ComplexOverlayRenderer implements IOverlayRenderer
{
    public enum DrawMode
    {
        FILL,
        WIREFRAME
    }

    private final Map<DrawMode, IOverlayRenderer> renderers;
    private final IOverlayRenderer fallbackRenderer;
    private DrawMode drawMode;

    public ComplexOverlayRenderer(DrawMode startingDrawMode, Map<DrawMode, IOverlayRenderer> renderers, IOverlayRenderer fallbackRenderer)
    {
        this.renderers = Collections.unmodifiableMap(renderers);
        this.fallbackRenderer = fallbackRenderer;
        this.drawMode = startingDrawMode;
    }
    public ComplexOverlayRenderer(DrawMode startingDrawMode, Map<DrawMode, IOverlayRenderer> renderers)
    {
        this.renderers = Collections.unmodifiableMap(renderers);
        this.fallbackRenderer = new IOverlayRenderer()
        {
            @Override
            public void drawCuboid(RenderBox box, IColorProvider colorProvider, IAlphaProvider alphaProvider)
            {
                Keystone.LOGGER.error("This ComplexOverlayRenderer does not support DrawMode." + drawMode + "!");
            }

            @Override
            public void drawGrid(Vec3d min, Vec3i size, double scale, IColorProvider colorProvider, IAlphaProvider alphaProvider, float lineWidth, boolean drawEdges)
            {
                Keystone.LOGGER.error("This ComplexOverlayRenderer does not support DrawMode." + drawMode + "!");
            }

            @Override
            public void drawPlane(Vec3d center, Direction planeNormal, double gridScale, IColorProvider colorProvider, IAlphaProvider fillAlphaProvider, IAlphaProvider lineAlphaProvider, float lineWidth)
            {
                Keystone.LOGGER.error("This ComplexOverlayRenderer does not support DrawMode." + drawMode + "!");
            }

            @Override
            public void drawDiamond(Vec3d center, double xRadius, double yRadius, double zRadius, Color4f color)
            {
                Keystone.LOGGER.error("This ComplexOverlayRenderer does not support DrawMode." + drawMode + "!");
            }

            @Override
            public void drawSphere(Vec3d center, double xRadius, double yRadius, double zRadius, Color4f color)
            {
                Keystone.LOGGER.error("This ComplexOverlayRenderer does not support DrawMode." + drawMode + "!");
            }
        };
        this.drawMode = startingDrawMode;
    }

    public ComplexOverlayRenderer drawMode(DrawMode drawMode)
    {
        this.drawMode = drawMode;
        return this;
    }

    protected IOverlayRenderer getRenderer()
    {
        return this.renderers.getOrDefault(this.drawMode, this.fallbackRenderer);
    }

    @Override
    public void drawCuboid(RenderBox box, IColorProvider colorProvider, IAlphaProvider alphaProvider)
    {
        getRenderer().drawCuboid(box, colorProvider, alphaProvider);
    }

    @Override
    public void drawGrid(Vec3d min, Vec3i size, double scale, IColorProvider colorProvider, IAlphaProvider alphaProvider, float lineWidth, boolean drawEdges)
    {
        getRenderer().drawGrid(min, size, scale, colorProvider, alphaProvider, lineWidth, drawEdges);
    }

    @Override
    public void drawPlane(Vec3d center, Direction planeNormal, double gridScale, IColorProvider colorProvider, IAlphaProvider fillAlphaProvider, IAlphaProvider lineAlphaProvider, float lineWidth)
    {
        getRenderer().drawPlane(center, planeNormal, gridScale, colorProvider, fillAlphaProvider, lineAlphaProvider, lineWidth);
    }

    @Override
    public void drawDiamond(Vec3d center, double xRadius, double yRadius, double zRadius, Color4f color)
    {
        getRenderer().drawDiamond(center, xRadius, yRadius, zRadius, color);
    }

    @Override
    public void drawSphere(Vec3d center, double xRadius, double yRadius, double zRadius, Color4f color)
    {
        getRenderer().drawSphere(center, xRadius, yRadius, zRadius, color);
    }
}
