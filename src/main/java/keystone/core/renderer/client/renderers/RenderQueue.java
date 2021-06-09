package keystone.core.renderer.client.renderers;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RenderQueue
{
    private static final Queue<RenderAction> renderQueue = new ConcurrentLinkedQueue<>();
    private static final Queue<RenderAction> deferredQueue = new ConcurrentLinkedQueue<>();

    public static void render(RenderAction action)
    {
        renderQueue.add(action);
    }
    public static void deferRendering(RenderAction action)
    {
        deferredQueue.add(action);
    }

    public static void doRenderQueue()
    {
        while (!renderQueue.isEmpty())
        {
            renderQueue.poll().render();
            RenderHelper.polygonModeFill();
            RenderHelper.disableCull();
        }
    }
    public static void renderDeferred()
    {
        while (!deferredQueue.isEmpty())
        {
            deferredQueue.poll().render();
            RenderHelper.polygonModeFill();
            RenderHelper.disableCull();
        }
    }

    @FunctionalInterface
    public interface RenderAction
    {
        void render();
    }
}
