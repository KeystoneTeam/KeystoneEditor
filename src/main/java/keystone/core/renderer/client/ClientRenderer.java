package keystone.core.renderer.client;

import keystone.core.KeystoneMod;
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
        return active && KeystoneMod.KeystoneActive;
    }
    public static void setActive(boolean active)
    {
        ClientRenderer.active = active;
    }
    public static void toggleActive()
    {
        active = !active;
        if (!active) return;

        Player.setActiveY();
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

    public static void render(DimensionId dimensionId)
    {
        if (!active || !KeystoneMod.KeystoneActive) return;

        RenderHelper.beforeRender();
        getBoundingBoxes(dimensionId).forEach(key ->
        {
            AbstractRenderer renderer = boundingBoxRendererMap.get(key.getClass());
            if (renderer != null) renderer.render(key);
        });
        RenderHelper.afterRender();
    }
    public static void renderDeferred()
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
                for (AbstractBoundingBox boundingBox : provider.get(dimensionId))
                {
                    if (isWithinRenderDistance(boundingBox))
                    {
                        boundingBoxes.accept(boundingBox);
                    }
                }
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
