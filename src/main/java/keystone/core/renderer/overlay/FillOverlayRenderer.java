package keystone.core.renderer.overlay;

import keystone.api.Keystone;
import keystone.core.client.Camera;
import keystone.core.renderer.*;
import keystone.core.renderer.interfaces.IAlphaProvider;
import keystone.core.renderer.interfaces.IColorProvider;
import keystone.core.renderer.shapes.SphereMesh;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class FillOverlayRenderer implements IOverlayRenderer
{
    private static final Vec3d[] UNIT_SPHERE_VERTICES = new SphereMesh(50).getVertices();

    private final ShapeRenderer quadRenderer;
    private final ShapeRenderer triangleRenderer;

    public FillOverlayRenderer(RendererProperties properties)
    {
        this.quadRenderer = ShapeRenderers.getOrCreate(properties.copy(VertexFormat.DrawMode.QUADS));
        this.triangleRenderer = ShapeRenderers.getOrCreate(properties.copy(VertexFormat.DrawMode.TRIANGLES));
    }
    
    //region Draw Calls
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
        
        if (minX != maxX && minZ != maxZ)
        {
            color = colorProvider.apply(Direction.DOWN).withAlpha(alphaProvider.apply(Direction.DOWN));
            quadRenderer.vertex(minX, minY, minZ).color(color).next()
                    .vertex(maxX, minY, minZ).color(color).next()
                    .vertex(maxX, minY, maxZ).color(color).next()
                    .vertex(minX, minY, maxZ).color(color).next();

            if (minY != maxY)
            {
                color = colorProvider.apply(Direction.UP).withAlpha(alphaProvider.apply(Direction.UP));
                quadRenderer.vertex(minX, maxY, maxZ).color(color).next()
                        .vertex(maxX, maxY, maxZ).color(color).next()
                        .vertex(maxX, maxY, minZ).color(color).next()
                        .vertex(minX, maxY, minZ).color(color).next();
            }
        }

        if (minX != maxX && minY != maxY)
        {
            color = colorProvider.apply(Direction.SOUTH).withAlpha(alphaProvider.apply(Direction.SOUTH));
            quadRenderer.vertex(maxX, minY, maxZ).color(color).next()
                    .vertex(maxX, maxY, maxZ).color(color).next()
                    .vertex(minX, maxY, maxZ).color(color).next()
                    .vertex(minX, minY, maxZ).color(color).next();

            if (minZ != maxZ)
            {
                color = colorProvider.apply(Direction.NORTH).withAlpha(alphaProvider.apply(Direction.NORTH));
                quadRenderer.vertex(minX, minY, minZ).color(color).next()
                        .vertex(minX, maxY, minZ).color(color).next()
                        .vertex(maxX, maxY, minZ).color(color).next()
                        .vertex(maxX, minY, minZ).color(color).next();
            }
        }
        if (minY != maxY && minZ != maxZ)
        {
            color = colorProvider.apply(Direction.WEST).withAlpha(alphaProvider.apply(Direction.WEST));
            quadRenderer.vertex(minX, minY, minZ).color(color).next()
                    .vertex(minX, minY, maxZ).color(color).next()
                    .vertex(minX, maxY, maxZ).color(color).next()
                    .vertex(minX, maxY, minZ).color(color).next();

            if (minX != maxX)
            {
                color = colorProvider.apply(Direction.EAST).withAlpha(alphaProvider.apply(Direction.EAST));
                quadRenderer.vertex(maxX, maxY, minZ).color(color).next()
                        .vertex(maxX, maxY, maxZ).color(color).next()
                        .vertex(maxX, minY, maxZ).color(color).next()
                        .vertex(maxX, minY, minZ).color(color).next();
            }
        }
    }
    @Override
    public void drawGrid(Vec3d min, Vec3i size, double scale, IColorProvider colorProvider, IAlphaProvider alphaProvider, boolean drawEdges)
    {
        Keystone.LOGGER.error("FillOverlayRenderer does not support drawGrid!");
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

        drawCuboid(new RenderBox(min, min.add(size.getX(), size.getY(), size.getZ())), colorProvider, alphaProvider);
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
        
        triangleRenderer.vertex(pY).color(color).next()
                .vertex(pX).color(color).next()
                .vertex(nZ).color(color).next();
        triangleRenderer.vertex(nY).color(color).next()
                .vertex(nZ).color(color).next()
                .vertex(pX).color(color).next();
    
        triangleRenderer.vertex(pY).color(color).next()
                .vertex(nZ).color(color).next()
                .vertex(nX).color(color).next();
        triangleRenderer.vertex(nY).color(color).next()
                .vertex(nX).color(color).next()
                .vertex(nZ).color(color).next();
    
        triangleRenderer.vertex(pY).color(color).next()
                .vertex(nX).color(color).next()
                .vertex(pZ).color(color).next();
        triangleRenderer.vertex(nY).color(color).next()
                .vertex(pZ).color(color).next()
                .vertex(nX).color(color).next();
    
        triangleRenderer.vertex(pY).color(color).next()
                .vertex(pZ).color(color).next()
                .vertex(pX).color(color).next();
        triangleRenderer.vertex(nY).color(color).next()
                .vertex(pX).color(color).next()
                .vertex(pZ).color(color).next();
    }
    @Override
    public void drawSphere(Vec3d center, double xRadius, double yRadius, double zRadius, Color4f color)
    {
        if (yRadius <= 0) yRadius = xRadius;
        if (zRadius <= 0) zRadius = xRadius;
        for (Vec3d vertex : UNIT_SPHERE_VERTICES) triangleRenderer.vertex(center.add(vertex.getX() * xRadius, vertex.getY() * yRadius, vertex.getZ() * zRadius)).color(color).next();
    }
    //endregion
}
