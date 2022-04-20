package keystone.core.renderer.overlay;

import keystone.core.client.Camera;
import keystone.core.renderer.Color4f;
import keystone.core.renderer.RenderBox;
import keystone.core.renderer.interfaces.IAlphaProvider;
import keystone.core.renderer.interfaces.IColorProvider;
import keystone.core.renderer.interfaces.IRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class WireframeOverlayRenderer implements IOverlayRenderer
{
    private final IRenderer renderer;
    private final float lineWidth;

    public WireframeOverlayRenderer(IRenderer renderer, float lineWidth)
    {
        this.renderer = renderer;
        this.lineWidth = lineWidth;
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
        if (minX != maxX && minZ != maxZ)
        {
            color = colorProvider.apply(Direction.DOWN).withAlpha(alphaProvider.apply(Direction.DOWN));
            modifyRenderer(Direction.DOWN);

            renderer.lineStrip(lineWidth, VertexFormats.POSITION_COLOR);
            renderer.vertex(minX, minY, minZ).color(color).next()
                    .vertex(maxX, minY, minZ).color(color).next()
                    .vertex(maxX, minY, maxZ).color(color).next()
                    .vertex(minX, minY, maxZ).color(color).next()
                    .vertex(minX, minY, minZ).color(color).next();
            renderer.draw();

            if (minY != maxY)
            {
                color = colorProvider.apply(Direction.UP).withAlpha(alphaProvider.apply(Direction.UP));
                modifyRenderer(Direction.UP);

                renderer.lineStrip(lineWidth, VertexFormats.POSITION_COLOR);
                renderer.vertex(minX, maxY, maxZ).color(color).next()
                        .vertex(maxX, maxY, maxZ).color(color).next()
                        .vertex(maxX, maxY, minZ).color(color).next()
                        .vertex(minX, maxY, minZ).color(color).next()
                        .vertex(minX, maxY, maxZ).color(color).next();
                renderer.draw();
            }
        }

        if (minX != maxX && minY != maxY)
        {
            color = colorProvider.apply(Direction.SOUTH).withAlpha(alphaProvider.apply(Direction.SOUTH));
            modifyRenderer(Direction.SOUTH);

            renderer.lineStrip(lineWidth, VertexFormats.POSITION_COLOR);
            renderer.vertex(maxX, minY, maxZ).color(color).next()
                    .vertex(maxX, maxY, maxZ).color(color).next()
                    .vertex(minX, maxY, maxZ).color(color).next()
                    .vertex(minX, minY, maxZ).color(color).next()
                    .vertex(maxX, minY, maxZ).color(color).next();
            renderer.draw();

            if (minZ != maxZ)
            {
                color = colorProvider.apply(Direction.NORTH).withAlpha(alphaProvider.apply(Direction.NORTH));
                modifyRenderer(Direction.NORTH);

                renderer.lineStrip(lineWidth, VertexFormats.POSITION_COLOR);
                renderer.vertex(minX, minY, minZ).color(color).next()
                        .vertex(minX, maxY, minZ).color(color).next()
                        .vertex(maxX, maxY, minZ).color(color).next()
                        .vertex(maxX, minY, minZ).color(color).next()
                        .vertex(minX, minY, minZ).color(color).next();
                renderer.draw();
            }
        }
        if (minY != maxY && minZ != maxZ)
        {
            color = colorProvider.apply(Direction.WEST).withAlpha(alphaProvider.apply(Direction.WEST));
            modifyRenderer(Direction.WEST);

            renderer.lineStrip(lineWidth, VertexFormats.POSITION_COLOR);
            renderer.vertex(minX, minY, minZ).color(color).next()
                    .vertex(minX, minY, maxZ).color(color).next()
                    .vertex(minX, maxY, maxZ).color(color).next()
                    .vertex(minX, maxY, minZ).color(color).next()
                    .vertex(minX, minY, minZ).color(color).next();
            renderer.draw();

            if (minX != maxX)
            {
                color = colorProvider.apply(Direction.EAST).withAlpha(alphaProvider.apply(Direction.EAST));
                modifyRenderer(Direction.EAST);

                renderer.lineStrip(lineWidth, VertexFormats.POSITION_COLOR);
                renderer.vertex(maxX, maxY, minZ).color(color).next()
                        .vertex(maxX, maxY, maxZ).color(color).next()
                        .vertex(maxX, minY, maxZ).color(color).next()
                        .vertex(maxX, minY, minZ).color(color).next()
                        .vertex(maxX, maxY, minZ).color(color).next();
                renderer.draw();
            }
        }
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
    //endregion
}
