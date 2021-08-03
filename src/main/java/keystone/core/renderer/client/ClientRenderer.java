package keystone.core.renderer.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.core.renderer.client.interop.ClientInterop;
import keystone.core.renderer.client.models.Point;
import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.client.providers.ICachingProvider;
import keystone.core.renderer.client.renderers.AbstractRenderer;
import keystone.core.renderer.client.renderers.RenderHelper;
import keystone.core.renderer.client.renderers.RenderQueue;
import keystone.core.renderer.common.MathHelper;
import keystone.core.renderer.common.TypeHelper;
import keystone.core.renderer.common.models.AbstractBoundingBox;
import keystone.core.renderer.common.models.DimensionId;

import java.util.*;
import java.util.stream.Stream;

public class ClientRenderer
{
    private static final int CHUNK_SIZE = 16;
    private static final Map<Class<? extends AbstractBoundingBox>, AbstractRenderer> boundingBoxRendererMap = new HashMap<>();

    private static boolean active = true;
    private static final Set<IBoundingBoxProvider> providers = new HashSet<>();

    public static boolean getActive()
    {
        return active && Keystone.isActive();
    }
    public static void setActive(boolean active)
    {
        ClientRenderer.active = active;
    }
    public static void toggleActive()
    {
        active = !active;
        if (!active) return;
    }

    public static <T extends AbstractBoundingBox> void registerProvider(IBoundingBoxProvider<T> provider)
    {
        providers.add(provider);
    }

    public static <T extends AbstractBoundingBox> void registerRenderer(Class<? extends T> type, AbstractRenderer<T> renderer)
    {
        boundingBoxRendererMap.put(type, renderer);
    }

    private static boolean isWithinRenderDistance(AbstractBoundingBox boundingBox)
    {
        int renderDistanceBlocks = ClientInterop.getRenderDistanceChunks() * CHUNK_SIZE;
        int minX = MathHelper.floor(Player.getX() - renderDistanceBlocks);
        int maxX = MathHelper.floor(Player.getX() + renderDistanceBlocks);
        int minZ = MathHelper.floor(Player.getZ() - renderDistanceBlocks);
        int maxZ = MathHelper.floor(Player.getZ() + renderDistanceBlocks);

        return boundingBox.intersectsBounds(minX, minZ, maxX, maxZ);
    }

    public static void render(MatrixStack stack, float partialTicks, DimensionId dimensionId)
    {
        if (!active || !Keystone.isActive()) return;

        Keystone.forEachModule((module) -> module.preRender(stack, partialTicks, dimensionId));
        RenderHelper.beforeRender();

        RenderQueue.doRenderQueue();
        getBoundingBoxes(dimensionId).forEach(key ->
        {
            AbstractRenderer renderer = boundingBoxRendererMap.get(key.getClass());
            if (renderer != null) renderer.render(stack, key);
        });
        RenderHelper.afterRender();
    }
    public static void renderDeferred(float partialTicks)
    {
        RenderHelper.beforeRender();
        RenderHelper.polygonModeFill();
        RenderHelper.enableBlend();
        RenderQueue.renderDeferred();
        RenderHelper.disableBlend();
        RenderHelper.enablePolygonOffsetLine();
        RenderHelper.polygonOffsetMinusOne();
        RenderHelper.afterRender();
    }

    public static Stream<AbstractBoundingBox> getBoundingBoxes(DimensionId dimensionId)
    {
        Stream.Builder<AbstractBoundingBox> boundingBoxes = Stream.builder();
        for (IBoundingBoxProvider<?> provider : providers)
        {
            if (provider.canProvide(dimensionId))
            {
                provider.get(dimensionId).forEach(boundingBox ->
                {
                    if (isWithinRenderDistance(boundingBox))
                    {
                        boundingBoxes.accept(boundingBox);
                    }
                });
            }
        }

        Point point = Player.getPoint();
        return boundingBoxes.build()
                .sorted(Comparator
                        .comparingDouble((AbstractBoundingBox boundingBox) -> boundingBox.getDistance(point.getX(), point.getY(), point.getZ())).reversed());
    }
    public static void clear()
    {
        for (IBoundingBoxProvider<?> provider : providers)
        {
            TypeHelper.doIfType(provider, ICachingProvider.class, ICachingProvider::clearCache);
        }
    }
}
