package keystone.core.renderer.overlay;

import keystone.core.client.Camera;
import keystone.core.renderer.Color4f;
import keystone.core.renderer.RenderBox;
import keystone.core.renderer.interfaces.IAlphaProvider;
import keystone.core.renderer.interfaces.IColorProvider;
import keystone.core.renderer.interfaces.IRenderer;
import keystone.core.renderer.shapes.SphereMesh;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class FillOverlayRenderer implements IOverlayRenderer
{
    private static final Vec3d[] UNIT_SPHERE_VERTICES = new SphereMesh(50).getVertices();

    private final IRenderer renderer;

    public FillOverlayRenderer(IRenderer renderer)
    {
        this.renderer = renderer;
    }

    public IRenderer getRenderer() { return this.renderer; }

    //region Helpers

    //endregion
    //region Draw Calls
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
        renderer.quads(VertexFormats.POSITION_COLOR);
        
        if (minX != maxX && minZ != maxZ)
        {
            color = colorProvider.apply(Direction.DOWN).withAlpha(alphaProvider.apply(Direction.DOWN));
            renderer.vertex(minX, minY, minZ).color(color).next()
                    .vertex(maxX, minY, minZ).color(color).next()
                    .vertex(maxX, minY, maxZ).color(color).next()
                    .vertex(minX, minY, maxZ).color(color).next();

            if (minY != maxY)
            {
                color = colorProvider.apply(Direction.UP).withAlpha(alphaProvider.apply(Direction.UP));
                renderer.vertex(minX, maxY, maxZ).color(color).next()
                        .vertex(maxX, maxY, maxZ).color(color).next()
                        .vertex(maxX, maxY, minZ).color(color).next()
                        .vertex(minX, maxY, minZ).color(color).next();
            }
        }

        if (minX != maxX && minY != maxY)
        {
            color = colorProvider.apply(Direction.SOUTH).withAlpha(alphaProvider.apply(Direction.SOUTH));
            renderer.vertex(maxX, minY, maxZ).color(color).next()
                    .vertex(maxX, maxY, maxZ).color(color).next()
                    .vertex(minX, maxY, maxZ).color(color).next()
                    .vertex(minX, minY, maxZ).color(color).next();

            if (minZ != maxZ)
            {
                color = colorProvider.apply(Direction.NORTH).withAlpha(alphaProvider.apply(Direction.NORTH));
                renderer.vertex(minX, minY, minZ).color(color).next()
                        .vertex(minX, maxY, minZ).color(color).next()
                        .vertex(maxX, maxY, minZ).color(color).next()
                        .vertex(maxX, minY, minZ).color(color).next();
            }
        }
        if (minY != maxY && minZ != maxZ)
        {
            color = colorProvider.apply(Direction.WEST).withAlpha(alphaProvider.apply(Direction.WEST));
            renderer.vertex(minX, minY, minZ).color(color).next()
                    .vertex(minX, minY, maxZ).color(color).next()
                    .vertex(minX, maxY, maxZ).color(color).next()
                    .vertex(minX, maxY, minZ).color(color).next();

            if (minX != maxX)
            {
                color = colorProvider.apply(Direction.EAST).withAlpha(alphaProvider.apply(Direction.EAST));
                renderer.vertex(maxX, maxY, minZ).color(color).next()
                        .vertex(maxX, maxY, maxZ).color(color).next()
                        .vertex(maxX, minY, maxZ).color(color).next()
                        .vertex(maxX, minY, minZ).color(color).next();
            }
        }
        renderer.draw();
    }
    @Override
    public void drawGrid(Vec3d min, Vec3i size, double scale, IColorProvider colorProvider, IAlphaProvider alphaProvider, float lineWidth, boolean drawEdges)
    {
        if (alphaProvider == null) alphaProvider = direction -> null;

        int sizeX = size.getX();
        int sizeY = size.getY();
        int sizeZ = size.getZ();
        
        Color4f color;
        renderer.lines(lineWidth, VertexFormats.POSITION_COLOR);

        //region -X Face
        color = colorProvider.apply(Direction.WEST).withAlpha(alphaProvider.apply(Direction.WEST));
        for (int y = 1; y < sizeY; y++)
        {
            renderer.vertex(min.add(0, y * scale, 0)).color(color).next();
            renderer.vertex(min.add(0, y * scale, sizeZ * scale)).color(color).next();
        }
        for (int z = 1; z < sizeZ; z++)
        {
            renderer.vertex(min.add(0, 0, z * scale)).color(color).next();
            renderer.vertex(min.add(0, sizeY * scale, z * scale)).color(color).next();
        }
        //endregion
        //region +X Face
        color = colorProvider.apply(Direction.EAST).withAlpha(alphaProvider.apply(Direction.EAST));
        for (int y = 1; y < sizeY; y++)
        {
            renderer.vertex(min.add(sizeX * scale, y * scale, 0)).color(color).next();
            renderer.vertex(min.add(sizeX * scale, y * scale, sizeZ * scale)).color(color).next();
        }
        for (int z = 1; z < sizeZ; z++)
        {
            renderer.vertex(min.add(sizeX * scale, 0, z * scale)).color(color).next();
            renderer.vertex(min.add(sizeX * scale, sizeY * scale, z * scale)).color(color).next();
        }
        //endregion
        //region -Y Face
        color = colorProvider.apply(Direction.DOWN).withAlpha(alphaProvider.apply(Direction.DOWN));
        for (int x = 1; x < sizeX; x++)
        {
            renderer.vertex(min.add(x * scale, 0, 0)).color(color).next();
            renderer.vertex(min.add(x * scale, 0, sizeZ * scale)).color(color).next();
        }
        for (int z = 1; z < sizeZ; z++)
        {
            renderer.vertex(min.add(0, 0, z * scale)).color(color).next();
            renderer.vertex(min.add(sizeX * scale, 0, z * scale)).color(color).next();
        }
        //endregion
        //region +Y Face
        color = colorProvider.apply(Direction.UP).withAlpha(alphaProvider.apply(Direction.UP));
        for (int x = 1; x < sizeX; x++)
        {
            renderer.vertex(min.add(x * scale, sizeY * scale, 0)).color(color).next();
            renderer.vertex(min.add(x * scale, sizeY * scale, sizeZ * scale)).color(color).next();
        }
        for (int z = 1; z < sizeZ; z++)
        {
            renderer.vertex(min.add(0, sizeY * scale, z * scale)).color(color).next();
            renderer.vertex(min.add(sizeX * scale, sizeY * scale, z * scale)).color(color).next();
        }
        //endregion
        //region -Z Face
        color = colorProvider.apply(Direction.NORTH).withAlpha(alphaProvider.apply(Direction.NORTH));
        for (int y = 1; y < sizeY; y++)
        {
            renderer.vertex(min.add(0, y * scale, 0)).color(color).next();
            renderer.vertex(min.add(sizeX * scale, y * scale, 0)).color(color).next();
        }
        for (int x = 1; x < sizeX; x++)
        {
            renderer.vertex(min.add(x * scale, 0, 0)).color(color).next();
            renderer.vertex(min.add(x * scale, sizeY * scale, 0)).color(color).next();
        }
        //endregion
        //region +Z Face
        color = colorProvider.apply(Direction.SOUTH).withAlpha(alphaProvider.apply(Direction.SOUTH));
        for (int y = 1; y < sizeY; y++)
        {
            renderer.vertex(min.add(0, y * scale, sizeZ * scale)).color(color).next();
            renderer.vertex(min.add(sizeX * scale, y * scale, sizeZ * scale)).color(color).next();
        }
        for (int x = 1; x < sizeX; x++)
        {
            renderer.vertex(min.add(x * scale, 0, sizeZ * scale)).color(color).next();
            renderer.vertex(min.add(x * scale, sizeY * scale, sizeZ * scale)).color(color).next();
        }
        //endregion

        renderer.draw();
    }
    @Override
    public void drawPlane(Vec3d center, Direction planeNormal, double gridScale, IColorProvider colorProvider, IAlphaProvider fillAlphaProvider, IAlphaProvider lineAlphaProvider, float lineWidth)
    {
        if (fillAlphaProvider == null) fillAlphaProvider = direction -> null;
        if (lineAlphaProvider == null) lineAlphaProvider = direction -> null;

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

        drawCuboid(new RenderBox(min, min.add(size.getX(), size.getY(), size.getZ())).nudge(), colorProvider, fillAlphaProvider);
        drawGrid(min, size, gridScale, colorProvider, lineAlphaProvider, lineWidth, true);
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

        renderer.triangles(VertexFormats.POSITION_COLOR);

        renderer.vertex(pY).color(color).next()
                .vertex(pX).color(color).next()
                .vertex(nZ).color(color).next();
        renderer.vertex(nY).color(color).next()
                .vertex(nZ).color(color).next()
                .vertex(pX).color(color).next();

        renderer.vertex(pY).color(color).next()
                .vertex(nZ).color(color).next()
                .vertex(nX).color(color).next();
        renderer.vertex(nY).color(color).next()
                .vertex(nX).color(color).next()
                .vertex(nZ).color(color).next();

        renderer.vertex(pY).color(color).next()
                .vertex(nX).color(color).next()
                .vertex(pZ).color(color).next();
        renderer.vertex(nY).color(color).next()
                .vertex(pZ).color(color).next()
                .vertex(nX).color(color).next();

        renderer.vertex(pY).color(color).next()
                .vertex(pZ).color(color).next()
                .vertex(pX).color(color).next();
        renderer.vertex(nY).color(color).next()
                .vertex(pX).color(color).next()
                .vertex(pZ).color(color).next();

        renderer.draw();
    }
    @Override
    public void drawSphere(Vec3d center, double xRadius, double yRadius, double zRadius, Color4f color)
    {
        if (yRadius <= 0) yRadius = xRadius;
        if (zRadius <= 0) zRadius = xRadius;

        renderer.triangles(VertexFormats.POSITION_COLOR);
        for (Vec3d vertex : UNIT_SPHERE_VERTICES) renderer.vertex(center.add(vertex.getX() * xRadius, vertex.getY() * yRadius, vertex.getZ() * zRadius)).color(color).next();
        renderer.draw();
    }
    //endregion
}
