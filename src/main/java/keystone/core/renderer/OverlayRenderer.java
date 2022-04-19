package keystone.core.renderer;

import keystone.core.client.Camera;
import keystone.core.renderer.color.ColorProviderFactory;
import keystone.core.renderer.color.IColorProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class OverlayRenderer
{
    private final IRenderer renderer;

    public OverlayRenderer(IRenderer renderer)
    {
        this.renderer = renderer;
    }

    public IRenderer getRenderer() { return this.renderer; }
    public void modifyRenderer(Direction direction) {  }

    //region Helpers

    //endregion
    //region Draw Calls
    public void drawCuboid(RenderBox box, IColorProvider colorProvider)
    {
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
            color = colorProvider.apply(Direction.DOWN);
            modifyRenderer(Direction.DOWN);

            renderer.vertex(minX, minY, minZ).color(color).next()
                    .vertex(maxX, minY, minZ).color(color).next()
                    .vertex(maxX, minY, maxZ).color(color).next()
                    .vertex(minX, minY, maxZ).color(color).next();

            if (minY != maxY)
            {
                color = colorProvider.apply(Direction.UP);
                modifyRenderer(Direction.UP);

                renderer.vertex(minX, maxY, minZ).color(color).next()
                        .vertex(maxX, maxY, minZ).color(color).next()
                        .vertex(maxX, maxY, maxZ).color(color).next()
                        .vertex(minX, maxY, maxZ).color(color).next();
            }
        }

        if (minX != maxX && minY != maxY)
        {
            color = colorProvider.apply(Direction.SOUTH);
            modifyRenderer(Direction.SOUTH);

            renderer.vertex(minX, minY, maxZ).color(color).next()
                    .vertex(minX, maxY, maxZ).color(color).next()
                    .vertex(maxX, maxY, maxZ).color(color).next()
                    .vertex(maxX, minY, maxZ).color(color).next();

            if (minZ != maxZ)
            {
                color = colorProvider.apply(Direction.NORTH);
                modifyRenderer(Direction.NORTH);

                renderer.vertex(minX, minY, minZ).color(color).next()
                        .vertex(minX, maxY, minZ).color(color).next()
                        .vertex(maxX, maxY, minZ).color(color).next()
                        .vertex(maxX, minY, minZ).color(color).next();
            }
        }
        if (minY != maxY && minZ != maxZ)
        {
            color = colorProvider.apply(Direction.WEST);
            modifyRenderer(Direction.WEST);

            renderer.vertex(minX, minY, minZ).color(color).next()
                    .vertex(minX, minY, maxZ).color(color).next()
                    .vertex(minX, maxY, maxZ).color(color).next()
                    .vertex(minX, maxY, minZ).color(color).next();

            if (minX != maxX)
            {
                color = colorProvider.apply(Direction.EAST);
                modifyRenderer(Direction.EAST);

                renderer.vertex(maxX, minY, minZ).color(color).next()
                        .vertex(maxX, minY, maxZ).color(color).next()
                        .vertex(maxX, maxY, maxZ).color(color).next()
                        .vertex(maxX, maxY, minZ).color(color).next();
            }
        }
        renderer.draw();
    }
    
    public void drawGrid(Vec3d min, Vec3i size, double scale, IColorProvider colorProvider, float lineWidth, boolean drawEdges)
    {
        int sizeX = size.getX();
        int sizeY = size.getY();
        int sizeZ = size.getZ();
        
        Color4f color;
        renderer.lines(lineWidth, VertexFormats.POSITION_COLOR);

        //region -X Face
        color = colorProvider.apply(Direction.WEST);
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
        color = colorProvider.apply(Direction.EAST);
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
        color = colorProvider.apply(Direction.DOWN);
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
        color = colorProvider.apply(Direction.UP);
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
        color = colorProvider.apply(Direction.NORTH);
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
        color = colorProvider.apply(Direction.SOUTH);
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
    public void drawPlane(Vec3d center, Direction planeNormal, double gridScale, IColorProvider colorProvider, float lineAlpha, float lineWidth)
    {
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

        drawGrid(min, size, gridScale, ColorProviderFactory.colorProvider(colorProvider).withAlphaProvider(direction -> lineAlpha), lineWidth, true);
        drawCuboid(new RenderBox(min, min.add(size.getX(), size.getY(), size.getZ())).nudge(), colorProvider);
    }
    //endregion
}
