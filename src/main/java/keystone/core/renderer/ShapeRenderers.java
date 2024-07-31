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
    
    public static ShapeRenderer getOrCreate(RendererProperties properties) { return renderers.computeIfAbsent(properties, ShapeRenderer::new); }
    public static void begin(WorldRenderContext context)
    {
        for (ShapeRenderer renderer : renderers.values()) renderer.begin(context);
    }
    public static void draw() { for (ShapeRenderer renderer : renderers.values()) renderer.draw(); }
    
    public static ComplexOverlayRenderer createComplexOverlay(RendererProperties fillProperties, RendererProperties wireframeProperties)
    {
        return new ComplexOverlayRenderer(ComplexOverlayRenderer.DrawMode.FILL, Map.of
        (
                ComplexOverlayRenderer.DrawMode.FILL, new FillOverlayRenderer(fillProperties),
                ComplexOverlayRenderer.DrawMode.WIREFRAME, new WireframeOverlayRenderer(wireframeProperties)
        ));
    }
}
