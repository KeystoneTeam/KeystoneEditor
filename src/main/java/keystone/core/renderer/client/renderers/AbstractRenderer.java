package keystone.core.renderer.client.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.core.renderer.client.models.Point;
import keystone.core.renderer.common.models.AbstractBoundingBox;
import keystone.core.renderer.common.models.Coords;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

import java.awt.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractRenderer<T extends AbstractBoundingBox>
{
    private static final double TAU = 6.283185307179586D;
    public static final double PHI_SEGMENT = TAU / 90D;
    private static final double PI = TAU / 2D;
    public static final double THETA_SEGMENT = PHI_SEGMENT / 2D;

    private static final Point[] UNIT_SPHERE_VERTICES = new SphereMesh(50).getPoints();

    public abstract void render(MatrixStack stack, T boundingBox);
    public void modifyRenderer(Renderer renderer, Direction faceDirection) {}

    protected void renderCuboid(OffsetBox bb, Color color, boolean alwaysDrawOutline, boolean alwaysDrawFaces) { renderCuboid(bb, direction -> color, direction -> 32, alwaysDrawOutline, alwaysDrawFaces); }
    protected void renderCuboid(OffsetBox bb, Function<Direction, Color> colorProvider, Function<Direction, Integer> alphaProvider, boolean alwaysDrawOutline, boolean alwaysDrawFaces)
    {
        OffsetBox nudge = bb.nudge();
        renderOutlinedCuboid(nudge, colorProvider, direction -> 255, alwaysDrawOutline);
        renderCuboidFaces(nudge.getMin(), nudge.getMax(), colorProvider, alphaProvider, alwaysDrawFaces);
    }

    protected void renderDiamond(OffsetPoint center, double xRadius, double yRadius, double zRadius, Color color, int alpha, boolean alwaysDrawOutline, boolean alwaysDrawFaces, boolean cull)
    {
        renderOutlinedDiamond(center, xRadius, yRadius, zRadius, color, 255, alwaysDrawOutline);
        renderFilledDiamond(center, xRadius, yRadius, zRadius, color, alpha, alwaysDrawFaces, cull);
    }

    protected void renderSphere(OffsetPoint center, double xRadius, double yRadius, double zRadius, Color color, int alpha, boolean alwaysDrawOutline, boolean alwaysDrawFaces, boolean cull)
    {
        renderOutlinedSphere(center, xRadius, yRadius, zRadius, color, 255, alwaysDrawOutline);
        renderFilledSphere(center, xRadius, yRadius, zRadius, color, alpha, alwaysDrawFaces, cull);
    }

    protected void renderOutlinedCuboid(OffsetBox bb, Color color, boolean ignoreDepth) { renderOutlinedCuboid(bb, direction -> color, direction -> 255, ignoreDepth); }
    protected void renderOutlinedCuboid(OffsetBox bb, Function<Direction, Color> colorProvider, Function<Direction, Integer> alphaProvider, boolean ignoreDepth)
    {
        RenderHelper.polygonModeLine();
        OffsetPoint min = bb.getMin();
        OffsetPoint max = bb.getMax();
        RenderQueue.deferRendering(() -> renderCuboidFaces(min, max, colorProvider, alphaProvider, min.getY() == max.getY() ? Renderer::startLineLoop : Renderer::startLines, ignoreDepth));
    }

    protected void renderOutlinedDiamond(OffsetPoint center, double xRadius, double yRadius, double zRadius, Color color, int alpha, boolean ignoreDepth)
    {
        RenderQueue.deferRendering(() ->
        {
            RenderHelper.polygonModeLine();
            renderDiamondFaces(center, xRadius, yRadius, zRadius, color, alpha, Renderer::startTriangles, ignoreDepth);
        });
    }
    protected void renderFilledDiamond(OffsetPoint center, double xRadius, double yRadius, double zRadius, Color color, int alpha, boolean ignoreDepth, boolean cull)
    {
        RenderQueue.deferRendering(() ->
        {
            RenderHelper.polygonModeFill();
            if (cull) RenderHelper.enableCull();
            renderDiamondFaces(center, xRadius, yRadius, zRadius, color, alpha, Renderer::startTriangles, ignoreDepth);
        });
    }

    protected void renderOutlinedSphere(OffsetPoint center, double xRadius, double yRadius, double zRadius, Color color, int alpha, boolean ignoreDepth)
    {
        RenderQueue.deferRendering(() ->
        {
            renderCircle(center, (x, y) -> new Vector3d(0, x * yRadius, y * zRadius), color, alpha);
            renderCircle(center, (x, y) -> new Vector3d(x * xRadius, 0, y * zRadius), color, alpha);
            renderCircle(center, (x, y) -> new Vector3d(x * xRadius, y * yRadius, 0), color, alpha);
        });
    }
    protected void renderFilledSphere(OffsetPoint center, double xRadius, double yRadius, double zRadius, Color color, int alpha, boolean ignoreDepth, boolean cull)
    {
        RenderQueue.deferRendering(() ->
        {
            RenderHelper.polygonModeFill();
            if (cull) RenderHelper.enableCull();
            renderSphereSurface(center, xRadius, yRadius, zRadius, color, alpha, Renderer::startTriangles, ignoreDepth);
        });
    }

    private void renderCuboidFaces(OffsetPoint min, OffsetPoint max, Function<Direction, Color> colorProvider, Function<Direction, Integer> alphaProvider, Supplier<Renderer> rendererSupplier, boolean ignoreDepth)
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
    private void renderDiamondFaces(OffsetPoint center, double xRadius, double yRadius, double zRadius, Color color, int alpha, Supplier<Renderer> rendererSupplier, boolean ignoreDepth)
    {
        if (ignoreDepth) RenderHelper.disableDepthTest();
        else RenderHelper.enableDepthTest();

        Renderer renderer = rendererSupplier.get();
        renderer.setColor(color).setAlpha(alpha);

        OffsetPoint nX = center.offset(-xRadius, 0, 0);
        OffsetPoint pX = center.offset(xRadius, 0, 0);
        OffsetPoint nY = center.offset(0, -yRadius, 0);
        OffsetPoint pY = center.offset(0, yRadius, 0);
        OffsetPoint nZ = center.offset(0, 0, -zRadius);
        OffsetPoint pZ = center.offset(0, 0, zRadius);

        renderer.addPoints(new OffsetPoint[]
        {
                pY, pX, nZ,
                nY, nZ, pX,

                pY, nZ, nX,
                nY, nX, nZ,

                pY, nX, pZ,
                nY, pZ, nX,

                pY, pZ, pX,
                nY, pX, pZ
        });

        renderer.render();
        RenderHelper.enableDepthTest();
    }
    private void renderSphereSurface(OffsetPoint center, double xRadius, double yRadius, double zRadius, Color color, int alpha, Supplier<Renderer> rendererSupplier, boolean ignoreDepth)
    {
        if (ignoreDepth) RenderHelper.disableDepthTest();
        else RenderHelper.enableDepthTest();

        Renderer renderer = rendererSupplier.get();
        renderer.setColor(color).setAlpha(alpha);

        if (yRadius <= 0) yRadius = xRadius;
        if (zRadius <= 0) zRadius = xRadius;

        for (Point point : UNIT_SPHERE_VERTICES)
        {
            renderer.addPoint(center.offset(point.getX() * xRadius, point.getY() * yRadius, point.getZ() * zRadius));
        }
        renderer.render();
        RenderHelper.enableDepthTest();
    }

    protected void renderCuboidFaces(OffsetPoint min, OffsetPoint max, Function<Direction, Color> colorProvider, Function<Direction, Integer> alphaProvider, boolean ignoreDepth)
    {
        RenderQueue.deferRendering(() -> renderCuboidFaces(min, max, colorProvider, alphaProvider, Renderer::startQuads, ignoreDepth));
    }

    protected void renderText(OffsetPoint offsetPoint, String... texts)
    {
        FontRenderer fontRenderer = Minecraft.getInstance().font;

        RenderHelper.beforeRenderFont(offsetPoint);
        float top = -(fontRenderer.lineHeight * texts.length) / 2f;
        for (String text : texts)
        {
            float left = fontRenderer.width(text) / 2f;
            fontRenderer.draw(new MatrixStack(), text, -left, top, -1);
            top += fontRenderer.lineHeight;
        }
        RenderHelper.afterRenderFont();
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

    protected void renderPlane(OffsetPoint center, Direction planeNormal, double gridScale, Function<Direction, Color> colorProvider, Function<Direction, Integer> alphaProvider, boolean ignoreDepth)
    {
        int halfSize = Minecraft.getInstance().options.renderDistance << 4;
        int fullSize = halfSize * 2;

        OffsetPoint min = center;
        Coords size = new Coords(0, 0, 0);
        switch (planeNormal)
        {
            case NORTH:
            case SOUTH:
                min = center.offset(-halfSize, -halfSize, 0);
                size = new Coords(fullSize, fullSize, 0);
                break;
            case EAST:
            case WEST:
                min = center.offset(0, -halfSize, -halfSize);
                size = new Coords(0, fullSize, fullSize);
                break;
            case UP:
            case DOWN:
                min = center.offset(-halfSize, 0, -halfSize);
                size = new Coords(fullSize, 0, fullSize);
                break;
        }

        renderGrid(min, size, gridScale, colorProvider, alphaProvider, ignoreDepth, false);
        renderCuboid(new OffsetBox(min, min.offset(size.getX(), size.getY(), size.getZ())).nudge(), colorProvider, alphaProvider, ignoreDepth, ignoreDepth);
    }
    protected void renderGrid(OffsetPoint min, Coords size, double scale, Function<Direction, Color> colorProvider, Function<Direction, Integer> alphaProvider, boolean ignoreDepth, boolean drawEdges)
    {
        RenderQueue.deferRendering(() ->
        {
            int sizeX = size.getX();
            int sizeY = size.getY();
            int sizeZ = size.getZ();
            Renderer renderer = Renderer.startLines();

            //region -X Face
            renderer = renderer.setColor(colorProvider.apply(Direction.WEST)).setAlpha(alphaProvider.apply(Direction.WEST));
            for (int y = 1; y < sizeY; y++)
            {
                renderer.addPoint(min.offset(0, y * scale, 0));
                renderer.addPoint(min.offset(0, y * scale, sizeZ * scale));
            }
            for (int z = 1; z < sizeZ; z++)
            {
                renderer.addPoint(min.offset(0, 0, z * scale));
                renderer.addPoint(min.offset(0, sizeY * scale, z * scale));
            }
            //endregion
            //region +X Face
            renderer = renderer.setColor(colorProvider.apply(Direction.EAST)).setAlpha(alphaProvider.apply(Direction.EAST));
            for (int y = 1; y < sizeY; y++)
            {
                renderer.addPoint(min.offset(sizeX * scale, y * scale, 0));
                renderer.addPoint(min.offset(sizeX * scale, y * scale, sizeZ * scale));
            }
            for (int z = 1; z < sizeZ; z++)
            {
                renderer.addPoint(min.offset(sizeX * scale, 0, z * scale));
                renderer.addPoint(min.offset(sizeX * scale, sizeY * scale, z * scale));
            }
            //endregion
            //region -Y Face
            renderer = renderer.setColor(colorProvider.apply(Direction.DOWN)).setAlpha(alphaProvider.apply(Direction.DOWN));
            for (int x = 1; x < sizeX; x++)
            {
                renderer.addPoint(min.offset(x * scale, 0, 0));
                renderer.addPoint(min.offset(x * scale, 0, sizeZ * scale));
            }
            for (int z = 1; z < sizeZ; z++)
            {
                renderer.addPoint(min.offset(0, 0, z * scale));
                renderer.addPoint(min.offset(sizeX * scale, 0, z * scale));
            }
            //endregion
            //region +Y Face
            renderer = renderer.setColor(colorProvider.apply(Direction.UP)).setAlpha(alphaProvider.apply(Direction.UP));
            for (int x = 1; x < sizeX; x++)
            {
                renderer.addPoint(min.offset(x * scale, sizeY * scale, 0));
                renderer.addPoint(min.offset(x * scale, sizeY * scale, sizeZ * scale));
            }
            for (int z = 1; z < sizeZ; z++)
            {
                renderer.addPoint(min.offset(0, sizeY * scale, z * scale));
                renderer.addPoint(min.offset(sizeX * scale, sizeY * scale, z * scale));
            }
            //endregion
            //region -Z Face
            renderer = renderer.setColor(colorProvider.apply(Direction.NORTH)).setAlpha(alphaProvider.apply(Direction.NORTH));
            for (int y = 1; y < sizeY; y++)
            {
                renderer.addPoint(min.offset(0, y * scale, 0));
                renderer.addPoint(min.offset(sizeX * scale, y * scale, 0));
            }
            for (int x = 1; x < sizeX; x++)
            {
                renderer.addPoint(min.offset(x * scale, 0, 0));
                renderer.addPoint(min.offset(x * scale, sizeY * scale, 0));
            }
            //endregion
            //region +Z Face
            renderer = renderer.setColor(colorProvider.apply(Direction.SOUTH)).setAlpha(alphaProvider.apply(Direction.SOUTH));
            for (int y = 1; y < sizeY; y++)
            {
                renderer.addPoint(min.offset(0, y * scale, sizeZ * scale));
                renderer.addPoint(min.offset(sizeX * scale, y * scale, sizeZ * scale));
            }
            for (int x = 1; x < sizeX; x++)
            {
                renderer.addPoint(min.offset(x * scale, 0, sizeZ * scale));
                renderer.addPoint(min.offset(x * scale, sizeY * scale, sizeZ * scale));
            }
            //endregion

            renderer.render();
        });
    }
    private void renderCircle(OffsetPoint center, BiFunction<Double, Double, Vector3d> pointTransformer, Color color, int alpha)
    {
        Renderer renderer = Renderer.startLineLoop().setColor(color).setAlpha(alpha);

        for (double phi = 0.0D; phi < TAU; phi += PHI_SEGMENT)
        {
            double localX = Math.cos(phi);
            double localY = Math.sin(phi);
            Vector3d point = pointTransformer.apply(localX, localY);
            renderer.addPoint(center.offset(point.x, point.y, point.z));
        }

        renderer.render();
    }
}
