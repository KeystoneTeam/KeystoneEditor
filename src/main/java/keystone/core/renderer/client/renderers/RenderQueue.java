package keystone.core.renderer.client.renderers;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RenderQueue
{
    private static final Queue<RenderAction> queue = new ConcurrentLinkedQueue<>();

    public static void deferRendering(RenderAction action)
    {
        queue.add(action);
    }

    public static void renderDeferred()
    {
        while (!queue.isEmpty())
        {
            queue.poll().render();
            RenderHelper.polygonModeFill();
        }
    }

    @FunctionalInterface
    public interface RenderAction
    {
        void render();
    }
}
