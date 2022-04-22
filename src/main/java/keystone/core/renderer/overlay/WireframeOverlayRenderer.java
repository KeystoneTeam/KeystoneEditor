package keystone.core.renderer.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import keystone.core.client.Camera;
import keystone.core.renderer.Color4f;
import keystone.core.renderer.RenderBox;
import keystone.core.renderer.interfaces.IAlphaProvider;
import keystone.core.renderer.interfaces.IColorProvider;
import keystone.core.renderer.interfaces.IRenderer;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.function.BiFunction;

public class WireframeOverlayRenderer implements IOverlayRenderer
{
    private static final Vec2f[] UNIT_CIRCLE_VERTICES = createUnitCircleVertices(90);
    private static Vec2f[] createUnitCircleVertices(int resolution)
    {
        double step = 6.283185307179586D / resolution;
        Vec2f[] ret = new Vec2f[resolution];
        for (int i = 0; i < resolution; i++)
        {
            double theta = step * i;
            double localX = Math.cos(theta);
            double localY = Math.sin(theta);
            ret[i] = new Vec2f((float)localX, (float)localY);
        }
        return ret;
    }

    private final IRenderer renderer;
    private final float defaultLineWidth;
    private float lineWidth;

    public WireframeOverlayRenderer(IRenderer renderer, float lineWidth)
    {
        this.renderer = renderer;
        this.defaultLineWidth = lineWidth;
        this.lineWidth = lineWidth;
    }

    public IRenderer getRenderer() { return this.renderer; }
    public void lineWidth(float lineWidth)
    {
        this.lineWidth = lineWidth;
    }
    public void revertLineWidth()
    {
        this.lineWidth = defaultLineWidth;
    }

    //region Draw Calls
    private void drawLineLoop(Color4f color, Vec3d[] vertices)
    {
        renderer.lines(lineWidth, VertexFormats.LINES);
        for (int i = 0; i < vertices.length; i++)
        {
            Vec3d a = vertices[i];
            Vec3d b = vertices[(i + 1) % vertices.length];
            Vec3d normal = b.subtract(a).normalize();
            renderer.vertex(a).color(color).normal(normal).next();
            renderer.vertex(b).color(color).normal(normal).next();
        }
        renderer.draw();
    }

    @Override
    public void drawCuboid(RenderBox box, IColorProvider colorProvider, IAlphaProvider alphaProvider)
    {
        if (alphaProvider == null) alphaProvider = direction -> null;

        double minX = box.minX;
        double minY = box.minY;
        double minZ = box.minZ;

        double maxX = box.maxX;
        double maxY = box.maxY;
        double maxZ = box.maxZ;

        Color4f color;
        Vec3d[] vertices;

        //region WEST
        color = colorProvider.apply(Direction.WEST);
        vertices = new Vec3d[]
        {
                new Vec3d(minX, minY, minZ),
                new Vec3d(minX, maxY, minZ),
                new Vec3d(minX, maxY, maxZ),
                new Vec3d(minX, minY, maxZ)
        };
        drawLineLoop(color, vertices);
        //endregion
        //region EAST
        color = colorProvider.apply(Direction.EAST);
        vertices = new Vec3d[]
        {
                new Vec3d(maxX, minY, minZ),
                new Vec3d(maxX, maxY, minZ),
                new Vec3d(maxX, maxY, maxZ),
                new Vec3d(maxX, minY, maxZ)
        };
        drawLineLoop(color, vertices);
        //endregion
        //region DOWN
        color = colorProvider.apply(Direction.DOWN);
        vertices = new Vec3d[]
        {
                new Vec3d(minX, minY, minZ),
                new Vec3d(maxX, minY, minZ),
                new Vec3d(maxX, minY, maxZ),
                new Vec3d(minX, minY, maxZ)
        };
        drawLineLoop(color, vertices);
        //endregion
        //region UP
        color = colorProvider.apply(Direction.UP);
        vertices = new Vec3d[]
        {
                new Vec3d(minX, maxY, minZ),
                new Vec3d(maxX, maxY, minZ),
                new Vec3d(maxX, maxY, maxZ),
                new Vec3d(minX, maxY, maxZ)
        };
        drawLineLoop(color, vertices);
        //endregion
        //region NORTH
        color = colorProvider.apply(Direction.NORTH);
        vertices = new Vec3d[]
        {
                new Vec3d(minX, minY, minZ),
                new Vec3d(maxX, minY, minZ),
                new Vec3d(maxX, maxY, minZ),
                new Vec3d(minX, maxY, minZ)
        };
        drawLineLoop(color, vertices);
        //endregion
        //region SOUTH
        color = colorProvider.apply(Direction.SOUTH);
        vertices = new Vec3d[]
        {
                new Vec3d(minX, minY, maxZ),
                new Vec3d(maxX, minY, maxZ),
                new Vec3d(maxX, maxY, maxZ),
                new Vec3d(minX, maxY, maxZ)
        };
        drawLineLoop(color, vertices);
        //endregion
    }
    @Override
    public void drawGrid(Vec3d min, Vec3i size, double scale, IColorProvider colorProvider, IAlphaProvider alphaProvider, boolean drawEdges)
    {
        if (alphaProvider == null) alphaProvider = direction -> null;

        int sizeX = size.getX();
        int sizeY = size.getY();
        int sizeZ = size.getZ();
        
        Color4f color;
        Vec3f normal;
        renderer.lines(lineWidth, VertexFormats.LINES);

        //region X
        if (sizeY > 0 && sizeZ > 0)
        {
            //region -X Face
            color = colorProvider.apply(Direction.WEST).withAlpha(alphaProvider.apply(Direction.WEST));
            normal = new Vec3f(0, 0, 1);
            for (int y = 1; y < sizeY; y++)
            {
                renderer.vertex(min.add(0, y * scale, 0)).color(color).normal(normal).next();
                renderer.vertex(min.add(0, y * scale, sizeZ * scale)).color(color).normal(normal).next();
            }
            normal = new Vec3f(0, 1, 0);
            for (int z = 1; z < sizeZ; z++)
            {
                renderer.vertex(min.add(0, 0, z * scale)).color(color).normal(normal).next();
                renderer.vertex(min.add(0, sizeY * scale, z * scale)).color(color).normal(normal).next();
            }
            //endregion
            if (sizeX > 0)
            {
                //region +X Face
                color = colorProvider.apply(Direction.EAST).withAlpha(alphaProvider.apply(Direction.EAST));
                normal = new Vec3f(0, 0, 0);
                for (int y = 1; y < sizeY; y++)
                {
                    renderer.vertex(min.add(sizeX * scale, y * scale, 0)).color(color).normal(normal).next();
                    renderer.vertex(min.add(sizeX * scale, y * scale, sizeZ * scale)).color(color).normal(normal).next();
                }
                normal = new Vec3f(0, 1, 0);
                for (int z = 1; z < sizeZ; z++)
                {
                    renderer.vertex(min.add(sizeX * scale, 0, z * scale)).color(color).normal(normal).next();
                    renderer.vertex(min.add(sizeX * scale, sizeY * scale, z * scale)).color(color).normal(normal).next();
                }
                //endregion
            }
        }
        //endregion
        //region Y
        if (sizeX > 0 && sizeZ > 0)
        {
            //region -Y Face
            color = colorProvider.apply(Direction.DOWN).withAlpha(alphaProvider.apply(Direction.DOWN));
            normal = new Vec3f(0, 0, 1);
            for (int x = 1; x < sizeX; x++)
            {
                renderer.vertex(min.add(x * scale, 0, 0)).color(color).normal(normal).next();
                renderer.vertex(min.add(x * scale, 0, sizeZ * scale)).color(color).normal(normal).next();
            }
            normal = new Vec3f(1, 0, 0);
            for (int z = 1; z < sizeZ; z++)
            {
                renderer.vertex(min.add(0, 0, z * scale)).color(color).normal(normal).next();
                renderer.vertex(min.add(sizeX * scale, 0, z * scale)).color(color).normal(normal).next();
            }
            //endregion
            if (sizeY > 0)
            {
                //region +Y Face
                color = colorProvider.apply(Direction.UP).withAlpha(alphaProvider.apply(Direction.UP));
                normal = new Vec3f(0, 0, 1);
                for (int x = 1; x < sizeX; x++)
                {
                    renderer.vertex(min.add(x * scale, sizeY * scale, 0)).color(color).normal(normal).next();
                    renderer.vertex(min.add(x * scale, sizeY * scale, sizeZ * scale)).color(color).normal(normal).next();
                }
                normal = new Vec3f(1, 0, 0);
                for (int z = 1; z < sizeZ; z++)
                {
                    renderer.vertex(min.add(0, sizeY * scale, z * scale)).color(color).normal(normal).next();
                    renderer.vertex(min.add(sizeX * scale, sizeY * scale, z * scale)).color(color).normal(normal).next();
                }
                //endregion
            }
        }
        //endregion
        //region Z
        if (sizeX > 0 && sizeY > 0)
        {
            //region -Z Face
            color = colorProvider.apply(Direction.NORTH).withAlpha(alphaProvider.apply(Direction.NORTH));
            normal = new Vec3f(1, 0, 0);
            for (int y = 1; y < sizeY; y++)
            {
                renderer.vertex(min.add(0, y * scale, 0)).color(color).normal(normal).next();
                renderer.vertex(min.add(sizeX * scale, y * scale, 0)).color(color).normal(normal).next();
            }
            normal = new Vec3f(0, 1, 0);
            for (int x = 1; x < sizeX; x++)
            {
                renderer.vertex(min.add(x * scale, 0, 0)).color(color).normal(normal).next();
                renderer.vertex(min.add(x * scale, sizeY * scale, 0)).color(color).normal(normal).next();
            }
            //endregion
            if (sizeZ > 0)
            {
                //region +Z Face
                color = colorProvider.apply(Direction.SOUTH).withAlpha(alphaProvider.apply(Direction.SOUTH));
                normal = new Vec3f(1, 0, 0);
                for (int y = 1; y < sizeY; y++)
                {
                    renderer.vertex(min.add(0, y * scale, sizeZ * scale)).color(color).normal(normal).next();
                    renderer.vertex(min.add(sizeX * scale, y * scale, sizeZ * scale)).color(color).normal(normal).next();
                }
                normal = new Vec3f(0, 1, 0);
                for (int x = 1; x < sizeX; x++)
                {
                    renderer.vertex(min.add(x * scale, 0, sizeZ * scale)).color(color).normal(normal).next();
                    renderer.vertex(min.add(x * scale, sizeY * scale, sizeZ * scale)).color(color).normal(normal).next();
                }
                //endregion
            }
        }
        //endregion

        renderer.draw();
    }
    @Override
    public void drawPlane(Vec3d center, Direction planeNormal, double gridScale, IColorProvider colorProvider, IAlphaProvider alphaProvider)
    {
        if (alphaProvider == null) alphaProvider = direction -> null;

        World world = MinecraftClient.getInstance().world;
        int halfSize = Camera.getRenderDistanceBlocks();
        int fullSize = halfSize * 2;

        Vec3d min = center;
        Vec3i size = Vec3i.ZERO;
        switch (planeNormal)
        {
            case NORTH:
            case SOUTH:
                min = new Vec3d(center.x - halfSize, world.getBottomY(), center.z);
                size = new Vec3i(fullSize, world.getHeight(), 0);
                break;
            case EAST:
            case WEST:
                min = new Vec3d(center.x, world.getBottomY(), center.z - halfSize);
                size = new Vec3i(0, world.getHeight(), fullSize);
                break;
            case UP:
            case DOWN:
                min = center.add(-halfSize, 0, -halfSize);
                size = new Vec3i(fullSize, 0, fullSize);
                break;
        }

        drawGrid(min, size, gridScale, colorProvider, alphaProvider, true);
    }
    @Override
    public void drawDiamond(Vec3d center, double xRadius, double yRadius, double zRadius, Color4f color)
    {
        Vec3d nX = center.add(-xRadius, 0, 0);
        Vec3d pX = center.add(xRadius, 0, 0);
        Vec3d nY = center.add(0, -yRadius, 0);
        Vec3d pY = center.add(0, yRadius, 0);
        Vec3d nZ = center.add(0, 0, -zRadius);
        Vec3d pZ = center.add(0, 0, zRadius);

        drawLineLoop(color, new Vec3d[] { pY, pX, nY, nX });
        drawLineLoop(color, new Vec3d[] { pY, pZ, nY, nZ });
        drawLineLoop(color, new Vec3d[] { pX, pZ, nX, nZ });
    }
    @Override
    public void drawSphere(Vec3d center, double xRadius, double yRadius, double zRadius, Color4f color)
    {
        drawCircle(center, (x, y) -> new Vec3d(0, x * yRadius, y * zRadius), color);
        drawCircle(center, (x, y) -> new Vec3d(x * xRadius, 0, y * zRadius), color);
        drawCircle(center, (x, y) -> new Vec3d(x * xRadius, y * yRadius, 0), color);
    }
    //endregion
    //region Helpers
    private void drawCircle(Vec3d center, BiFunction<Double, Double, Vec3d> pointTransformer, Color4f color)
    {
        renderer.lineStrip(lineWidth, VertexFormats.POSITION_COLOR);
        for (int i = 0; i < UNIT_CIRCLE_VERTICES.length; i++)
        {
            Vec2f normalA = UNIT_CIRCLE_VERTICES[i];
            Vec2f normalB = UNIT_CIRCLE_VERTICES[(i + 1) % UNIT_CIRCLE_VERTICES.length];
            Vec3d a = center.add(pointTransformer.apply((double)normalA.x, (double)normalA.y));
            Vec3d b = center.add(pointTransformer.apply((double)normalB.x, (double)normalB.y));
            Vec3d normal = b.subtract(a).normalize();
            renderer.vertex(a).color(color).normal(normal).next();
            renderer.vertex(b).color(color).normal(normal).next();
        }
        renderer.draw();
    }
    //endregion
}
