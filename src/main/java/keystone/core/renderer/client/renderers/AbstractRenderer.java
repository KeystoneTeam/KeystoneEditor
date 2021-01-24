package keystone.core.renderer.client.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.core.renderer.client.models.Point;
import keystone.core.renderer.common.MathHelper;
import keystone.core.renderer.common.models.AbstractBoundingBox;
import keystone.core.renderer.config.KeystoneConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.Direction;

import java.awt.*;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractRenderer<T extends AbstractBoundingBox>
{
    private static final double TAU = 6.283185307179586D;
    public static final double PHI_SEGMENT = TAU / 90D;
    private static final double PI = TAU / 2D;
    public static final double THETA_SEGMENT = PHI_SEGMENT / 2D;

    public abstract void render(MatrixStack stack, T boundingBox);
    public void modifyRenderer(Renderer renderer, Direction faceDirection) {}

    protected void renderCuboid(OffsetBox bb, Color color, boolean alwaysDrawOutline, boolean alwaysDrawFaces) { renderCuboid(bb, direction -> color, direction -> 32, alwaysDrawOutline, alwaysDrawFaces); }
    protected void renderCuboid(OffsetBox bb, Function<Direction, Color> colorProvider, Function<Direction, Integer> alphaProvider, boolean alwaysDrawOutline, boolean alwaysDrawFaces)
    {
        OffsetBox nudge = bb.nudge();
        renderOutlinedCuboid(nudge, colorProvider, direction -> 255, alwaysDrawOutline);
        renderFilledFaces(nudge.getMin(), nudge.getMax(), colorProvider, alphaProvider, alwaysDrawFaces);
    }

    protected void renderOutlinedCuboid(OffsetBox bb, Color color, boolean ignoreDepth) { renderOutlinedCuboid(bb, direction -> color, direction -> 255, ignoreDepth); }
    protected void renderOutlinedCuboid(OffsetBox bb, Function<Direction, Color> colorProvider, Function<Direction, Integer> alphaProvider, boolean ignoreDepth)
    {
        RenderHelper.polygonModeLine();
        OffsetPoint min = bb.getMin();
        OffsetPoint max = bb.getMax();
        RenderQueue.deferRendering(() -> renderFaces(min, max, colorProvider, alphaProvider, min.getY() == max.getY() ? Renderer::startLineLoop : Renderer::startLines, ignoreDepth));
    }

    private void renderFaces(OffsetPoint min, OffsetPoint max, Color color, int alpha, Supplier<Renderer> rendererSupplier, boolean ignoreDepth) { renderFaces(min, max, direction -> color, direction -> 255, rendererSupplier, ignoreDepth); }
    private void renderFaces(OffsetPoint min, OffsetPoint max, Function<Direction, Color> colorProvider, Function<Direction, Integer> alphaProvider, Supplier<Renderer> rendererSupplier, boolean ignoreDepth)
    {
        if (ignoreDepth) RenderHelper.disableDepthTest();
        else RenderHelper.enableDepthTest();

        double minX = min.getX();
        double minY = min.getY();
        double minZ = min.getZ();

        double maxX = max.getX();
        double maxY = max.getY();
        double maxZ = max.getZ();

        Renderer renderer = rendererSupplier.get();

        if (minX != maxX && minZ != maxZ)
        {
            renderer.setColor(colorProvider.apply(Direction.DOWN)).setAlpha(alphaProvider.apply(Direction.DOWN));
            modifyRenderer(renderer, Direction.DOWN);

            renderer.addPoint(minX, minY, minZ)
                    .addPoint(maxX, minY, minZ)
                    .addPoint(maxX, minY, maxZ)
                    .addPoint(minX, minY, maxZ);

            if (minY != maxY)
            {
                renderer.setColor(colorProvider.apply(Direction.UP)).setAlpha(alphaProvider.apply(Direction.UP));
                modifyRenderer(renderer, Direction.UP);

                renderer.addPoint(minX, maxY, minZ)
                        .addPoint(maxX, maxY, minZ)
                        .addPoint(maxX, maxY, maxZ)
                        .addPoint(minX, maxY, maxZ);
            }
        }

        if (minX != maxX && minY != maxY)
        {
            renderer.setColor(colorProvider.apply(Direction.SOUTH)).setAlpha(alphaProvider.apply(Direction.SOUTH));
            modifyRenderer(renderer, Direction.SOUTH);

            renderer.addPoint(minX, minY, maxZ)
                    .addPoint(minX, maxY, maxZ)
                    .addPoint(maxX, maxY, maxZ)
                    .addPoint(maxX, minY, maxZ);

            if (minZ != maxZ)
            {
                renderer.setColor(colorProvider.apply(Direction.NORTH)).setAlpha(alphaProvider.apply(Direction.NORTH));
                modifyRenderer(renderer, Direction.NORTH);

                renderer.addPoint(minX, minY, minZ)
                        .addPoint(minX, maxY, minZ)
                        .addPoint(maxX, maxY, minZ)
                        .addPoint(maxX, minY, minZ);
            }
        }
        if (minY != maxY && minZ != maxZ)
        {
            renderer.setColor(colorProvider.apply(Direction.WEST)).setAlpha(alphaProvider.apply(Direction.WEST));
            modifyRenderer(renderer, Direction.WEST);

            renderer.addPoint(minX, minY, minZ)
                    .addPoint(minX, minY, maxZ)
                    .addPoint(minX, maxY, maxZ)
                    .addPoint(minX, maxY, minZ);

            if (minX != maxX)
            {
                renderer.setColor(colorProvider.apply(Direction.EAST)).setAlpha(alphaProvider.apply(Direction.EAST));
                modifyRenderer(renderer, Direction.EAST);

                renderer.addPoint(maxX, minY, minZ)
                        .addPoint(maxX, minY, maxZ)
                        .addPoint(maxX, maxY, maxZ)
                        .addPoint(maxX, maxY, minZ);
            }
        }
        renderer.render();

        RenderHelper.enableDepthTest();
    }

    private boolean playerInsideBoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
    {
        return minX < 0 && maxX > 0 && minY < 0 && maxY > 0 && minZ < 0 && maxZ > 0;
    }

    protected void renderLine(OffsetPoint startPoint, OffsetPoint endPoint, Color color)
    {
        RenderHelper.polygonModeLine();
        Renderer.startLines()
                .setColor(color)
                .addPoint(startPoint)
                .addPoint(endPoint)
                .render();
    }

    protected void renderFilledFaces(OffsetPoint min, OffsetPoint max, Color color, boolean ignoreDepth) { renderFilledFaces(min, max, direction -> color, ignoreDepth); }
    protected void renderFilledFaces(OffsetPoint min, OffsetPoint max, Function<Direction, Color> colorProvider, boolean ignoreDepth)
    {
        renderFilledFaces(min, max, colorProvider, direction -> 32, ignoreDepth);
    }

    protected void renderFilledFaces(OffsetPoint min, OffsetPoint max, Color color, int alpha, boolean ignoreDepth) { renderFilledFaces(min, max, direction -> color, direction -> alpha, ignoreDepth); }
    protected void renderFilledFaces(OffsetPoint min, OffsetPoint max, Function<Direction, Color> colorProvider, Function<Direction, Integer> alphaProvider, boolean ignoreDepth)
    {
        RenderQueue.deferRendering(() -> renderFaces(min, max, colorProvider, alphaProvider, Renderer::startQuads, ignoreDepth));
    }

    protected void renderText(OffsetPoint offsetPoint, String... texts)
    {
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;

        RenderHelper.beforeRenderFont(offsetPoint);
        float top = -(fontRenderer.FONT_HEIGHT * texts.length) / 2f;
        for (String text : texts)
        {
            float left = fontRenderer.getStringWidth(text) / 2f;
            fontRenderer.drawString(new MatrixStack(), text, -left, top, -1);
            top += fontRenderer.FONT_HEIGHT;
        }
        RenderHelper.afterRenderFont();
    }

    protected void renderSphere(Point center, double radius, Color color)
    {
        if (KeystoneConfig.renderSphereAsDots)
        {
            renderDotSphere(center, radius, color);
        } else
        {
            renderLineSphere(center, radius, color);
        }
    }

    protected void renderPoint(Point center, Color color)
    {
        RenderHelper.enablePointSmooth();
        RenderHelper.pointSize5();
        Renderer renderer = Renderer.startPoints()
                .setColor(color)
                .addPoint(new OffsetPoint(center));
        renderer.render();
    }

    private void renderLineSphere(Point center, double radius, Color color)
    {
        RenderHelper.lineWidth2();

        double offset = ((radius - (int) radius) == 0) ? center.getY() - (int) center.getY() : 0;
        int dyStep = radius < 64 ? 1 : MathHelper.floor(radius / 32);
        for (double dy = offset - radius; dy <= radius + 1; dy += dyStep)
        {
            double circleRadius = Math.sqrt((radius * radius) - (dy * dy));
            if (circleRadius == 0) circleRadius = Math.sqrt(2) / 2;
            renderCircle(center, circleRadius, color, dy + 0.001F);
        }
    }

    private void renderCircle(Point center, double radius, Color color, double dy)
    {
        Renderer renderer = Renderer.startLineLoop()
                .setColor(color);

        for (double phi = 0.0D; phi < TAU; phi += PHI_SEGMENT)
        {
            renderer.addPoint(new OffsetPoint(center.offset(Math.cos(phi) * radius, dy, Math.sin(phi) * radius)));
        }

        renderer.render();
    }

    private void renderDotSphere(Point center, double radius, Color color)
    {
        RenderHelper.enablePointSmooth();
        RenderHelper.pointSize5();
        Renderer renderer = Renderer.startPoints()
                .setColor(color);

        for (double phi = 0.0D; phi < TAU; phi += PHI_SEGMENT)
        {
            double dy = radius * Math.cos(phi);
            double radiusBySinPhi = radius * Math.sin(phi);
            for (double theta = 0.0D; theta < PI; theta += THETA_SEGMENT)
            {
                double dx = radiusBySinPhi * Math.cos(theta);
                double dz = radiusBySinPhi * Math.sin(theta);

                renderer.addPoint(new OffsetPoint(center.offset(dx, dy, dz)));
            }
        }
        renderer.render();
    }
}
