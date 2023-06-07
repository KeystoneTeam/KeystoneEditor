package keystone.core.renderer.overlay;

import keystone.core.client.Camera;
import keystone.core.renderer.Color4f;
import keystone.core.renderer.RendererProperties;
import keystone.core.renderer.ShapeRenderer;
import keystone.core.renderer.ShapeRenderers;
import keystone.core.renderer.interfaces.IAlphaProvider;
import keystone.core.renderer.interfaces.IColorProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.function.BiFunction;

public class WireframeOverlayRenderer implements IOverlayRenderer
{
    private static final int CIRCLE_RESOLUTION = 90;
    private static final Vec2f[] UNIT_CIRCLE_VERTICES = createUnitCircleVertices();
    private static final Vec3d[] CIRCLE_VERTICES_BUFFER = new Vec3d[UNIT_CIRCLE_VERTICES.length];
    private static Vec2f[] createUnitCircleVertices()
    {
        double step = 6.283185307179586D / CIRCLE_RESOLUTION;
        Vec2f[] ret = new Vec2f[CIRCLE_RESOLUTION];
        for (int i = 0; i < CIRCLE_RESOLUTION; i++)
        {
            double theta = step * i;
            double localX = Math.cos(theta);
            double localY = Math.sin(theta);
            ret[i] = new Vec2f((float)localX, (float)localY);
        }
        return ret;
    }

    private final ShapeRenderer renderer;

    public WireframeOverlayRenderer(RendererProperties properties)
    {
        this.renderer = ShapeRenderers.getOrCreate(properties.copy(VertexFormat.DrawMode.LINES));
    }

    public ShapeRenderer getRenderer() { return this.renderer; }

    //region Draw Calls
    private void drawLineLoop(Color4f color, Vec3d[] vertices)
    {
        for (int i = 0; i < vertices.length; i++)
        {
            Vec3d a = vertices[i];
            Vec3d b = vertices[(i + 1) % vertices.length];
            Vec3d normal = b.subtract(a).normalize();
            renderer.vertex(a).color(color).normal(normal).next();
            renderer.vertex(b).color(color).normal(normal).next();
        }
    }

    @Override
    public void drawCuboid(Box box, IColorProvider colorProvider, IAlphaProvider alphaProvider)
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

        //region X
        if (sizeY > 0 && sizeZ > 0)
        {
            //region WEST
            color = colorProvider.apply(Direction.WEST).withAlpha(alphaProvider.apply(Direction.WEST));
            for (int i = 0; i < sizeZ; i++)
            {
                for (int k = 0; k < sizeY; k += 128)
                {
                    Vec3d a = min.add(0, k, i);
                    Vec3d b = min.add(0, Math.min(sizeY, k + 128), i);
                    Vec3d normal = b.subtract(a);
                    renderer.vertex(a).color(color).normal(normal).next();
                    renderer.vertex(b).color(color).normal(normal).next();
                }
            }
            for (int i = 0; i < sizeY; i++)
            {
                for (int k = 0; k < sizeZ; k += 128)
                {
                    Vec3d a = min.add(0, i, k);
                    Vec3d b = min.add(0, i, Math.min(sizeZ, k + 128));
                    Vec3d normal = b.subtract(a);
                    renderer.vertex(a).color(color).normal(normal).next();
                    renderer.vertex(b).color(color).normal(normal).next();
                }
            }
            //endregion
            //region EAST
            if (sizeX > 0)
            {
                color = colorProvider.apply(Direction.EAST).withAlpha(alphaProvider.apply(Direction.EAST));
                for (int i = 0; i < sizeZ; i++)
                {
                    for (int k = 0; k < sizeY; k += 128)
                    {
                        Vec3d a = min.add(sizeX, k, i);
                        Vec3d b = min.add(sizeX, Math.min(sizeY, k + 128), i);
                        Vec3d normal = b.subtract(a);
                        renderer.vertex(a).color(color).normal(normal).next();
                        renderer.vertex(b).color(color).normal(normal).next();
                    }
                }
                for (int i = 0; i < sizeY; i++)
                {
                    for (int k = 0; k < sizeZ; k += 128)
                    {
                        Vec3d a = min.add(sizeX, i, k);
                        Vec3d b = min.add(sizeX, i, Math.min(sizeZ, k + 128));
                        Vec3d normal = b.subtract(a);
                        renderer.vertex(a).color(color).normal(normal).next();
                        renderer.vertex(b).color(color).normal(normal).next();
                    }
                }
            }
            //endregion
        }
        //endregion
        //region Y
        if (sizeX > 0 && sizeZ > 0)
        {
            //region DOWN
            color = colorProvider.apply(Direction.DOWN).withAlpha(alphaProvider.apply(Direction.DOWN));
            for (int i = 0; i < sizeX; i++)
            {
                for (int k = 0; k < sizeZ; k += 128)
                {
                    Vec3d a = min.add(i, 0, k);
                    Vec3d b = min.add(i, 0, Math.min(sizeZ, k + 128));
                    Vec3d normal = b.subtract(a);
                    renderer.vertex(a).color(color).normal(normal).next();
                    renderer.vertex(b).color(color).normal(normal).next();
                }
            }
            for (int i = 0; i < sizeZ; i++)
            {
                for (int k = 0; k < sizeX; k += 128)
                {
                    Vec3d a = min.add(k, 0, i);
                    Vec3d b = min.add(Math.min(sizeX, k + 128), 0, i);
                    Vec3d normal = b.subtract(a);
                    renderer.vertex(a).color(color).normal(normal).next();
                    renderer.vertex(b).color(color).normal(normal).next();
                }
            }
            //endregion
            //region UP
            if (sizeY > 0)
            {
                color = colorProvider.apply(Direction.UP).withAlpha(alphaProvider.apply(Direction.UP));
                for (int i = 0; i < sizeX; i++)
                {
                    for (int k = 0; k < sizeZ; k += 128)
                    {
                        Vec3d a = min.add(i, sizeY, k);
                        Vec3d b = min.add(i, sizeY, Math.min(sizeZ, k + 128));
                        Vec3d normal = b.subtract(a);
                        renderer.vertex(a).color(color).normal(normal).next();
                        renderer.vertex(b).color(color).normal(normal).next();
                    }
                }
                for (int i = 0; i < sizeZ; i++)
                {
                    for (int k = 0; k < sizeX; k += 128)
                    {
                        Vec3d a = min.add(k, sizeY, i);
                        Vec3d b = min.add(Math.min(sizeX, k + 128), sizeY, i);
                        Vec3d normal = b.subtract(a);
                        renderer.vertex(a).color(color).normal(normal).next();
                        renderer.vertex(b).color(color).normal(normal).next();
                    }
                }
            }
            //endregion
        }
        //endregion
        //region Z
        if (sizeX > 0 && sizeY > 0)
        {
            //region NORTH
            color = colorProvider.apply(Direction.NORTH).withAlpha(alphaProvider.apply(Direction.NORTH));
            for (int i = 0; i < sizeX; i++)
            {
                for (int k = 0; k < sizeY; k += 128)
                {
                    Vec3d a = min.add(i, k, 0);
                    Vec3d b = min.add(i, Math.min(sizeY, k + 128), 0);
                    Vec3d normal = b.subtract(a);
                    renderer.vertex(a).color(color).normal(normal).next();
                    renderer.vertex(b).color(color).normal(normal).next();
                }
            }
            for (int i = 0; i < sizeY; i++)
            {
                for (int k = 0; k < sizeX; k += 128)
                {
                    Vec3d a = min.add(k, i, 0);
                    Vec3d b = min.add(Math.min(sizeX, k + 128), i, 0);
                    Vec3d normal = b.subtract(a);
                    renderer.vertex(a).color(color).normal(normal).next();
                    renderer.vertex(b).color(color).normal(normal).next();
                }
            }
            //endregion
            //region SOUTH
            if (sizeZ > 0)
            {
                color = colorProvider.apply(Direction.SOUTH).withAlpha(alphaProvider.apply(Direction.SOUTH));
                for (int i = 0; i < sizeX; i++)
                {
                    for (int k = 0; k < sizeY; k += 128)
                    {
                        Vec3d a = min.add(i, k, sizeZ);
                        Vec3d b = min.add(i, Math.min(sizeY, k + 128), sizeZ);
                        Vec3d normal = b.subtract(a);
                        renderer.vertex(a).color(color).normal(normal).next();
                        renderer.vertex(b).color(color).normal(normal).next();
                    }
                }
                for (int i = 0; i < sizeY; i++)
                {
                    for (int k = 0; k < sizeX; k += 128)
                    {
                        Vec3d a = min.add(k, i, sizeZ);
                        Vec3d b = min.add(Math.min(sizeX, k + 128), i, sizeZ);
                        Vec3d normal = b.subtract(a);
                        renderer.vertex(a).color(color).normal(normal).next();
                        renderer.vertex(b).color(color).normal(normal).next();
                    }
                }
            }
            //endregion
        }
        //endregion
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
            case NORTH, SOUTH ->
            {
                min = new Vec3d(center.x - halfSize, world.getBottomY(), center.z);
                size = new Vec3i(fullSize, world.getHeight(), 0);
            }
            case EAST, WEST ->
            {
                min = new Vec3d(center.x, world.getBottomY(), center.z - halfSize);
                size = new Vec3i(0, world.getHeight(), fullSize);
            }
            case UP, DOWN ->
            {
                min = center.add(-halfSize, 0, -halfSize);
                size = new Vec3i(fullSize, 0, fullSize);
            }
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
        for (int i = 0; i < UNIT_CIRCLE_VERTICES.length; i++)
        {
            Vec2f normalA = UNIT_CIRCLE_VERTICES[i];
            CIRCLE_VERTICES_BUFFER[i] = center.add(pointTransformer.apply((double)normalA.x, (double)normalA.y));
        }
        drawLineLoop(color, CIRCLE_VERTICES_BUFFER);
    }
    //endregion
}
