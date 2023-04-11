package keystone.core.renderer;

import keystone.core.renderer.overlay.ComplexOverlayRenderer;
import keystone.core.renderer.overlay.FillOverlayRenderer;
import keystone.core.renderer.overlay.WireframeOverlayRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

import java.util.HashMap;
import java.util.Map;

public final class ShapeRenderers
{
    private static final Map<RendererProperties, ShapeRenderer> renderers = new HashMap<>();
    
    private static WorldRenderContext context;
    
    public static ShapeRenderer getOrCreate(RendererProperties properties) { return renderers.computeIfAbsent(properties, ShapeRenderer::new); }
    public static void beginRender(WorldRenderContext context)
    {
        ShapeRenderers.context = context;
        for (ShapeRenderer renderer : renderers.values()) renderer.begin();
    }
    public static void endRender() { for (ShapeRenderer renderer : renderers.values()) renderer.end(); }
    
    public static ComplexOverlayRenderer createComplexOverlay(RendererProperties fillProperties, RendererProperties wireframeProperties)
    {
        return new ComplexOverlayRenderer(ComplexOverlayRenderer.DrawMode.FILL, Map.of
        (
                ComplexOverlayRenderer.DrawMode.FILL, new FillOverlayRenderer(fillProperties),
                ComplexOverlayRenderer.DrawMode.WIREFRAME, new WireframeOverlayRenderer(wireframeProperties)
        ));
    }
    
    public static WorldRenderContext getContext()
    {
        return context;
    }
}
