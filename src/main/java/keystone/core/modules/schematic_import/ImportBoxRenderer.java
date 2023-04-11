package keystone.core.modules.schematic_import;

import keystone.api.Keystone;
import keystone.core.client.Camera;
import keystone.core.modules.mouse.MouseModule;
import keystone.core.modules.selection.SelectedFace;
import keystone.core.renderer.Color4f;
import keystone.core.renderer.RendererProperties;
import keystone.core.renderer.ShapeRenderers;
import keystone.core.renderer.interfaces.IAlphaProvider;
import keystone.core.renderer.interfaces.IColorProvider;
import keystone.core.renderer.overlay.ComplexOverlayRenderer;
import keystone.core.renderer.overlay.WireframeOverlayRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.Vec3d;

public class ImportBoxRenderer
{
    private final ComplexOverlayRenderer renderer;
    private final WireframeOverlayRenderer planeGridRenderer;
    private final MouseModule mouse;

    public ImportBoxRenderer()
    {
        this.renderer = ShapeRenderers.createComplexOverlay(RendererProperties.createFill(), RendererProperties.createWireframe(2.0f).ignoreDepth());
        this.planeGridRenderer = new WireframeOverlayRenderer(RendererProperties.createWireframe(1.0f));
        this.mouse = Keystone.getModule(MouseModule.class);
    }

    public void render(WorldRenderContext context, ImportBoundingBox box)
    {
        renderer.drawMode(ComplexOverlayRenderer.DrawMode.FILL);

        SelectedFace selectedFace = mouse.getSelectedFace();
        if (selectedFace != null && selectedFace.getBox().equals(box) && selectedFace.isDraggingFace())
        {
            double centerX = (int)Camera.getX();
            double centerY = (int)Camera.getY();
            double centerZ = (int)Camera.getZ();
            switch (selectedFace.getFaceDirection())
            {
                case EAST, WEST -> centerX = box.getCenter().getX();
                case UP, DOWN -> centerY = box.getCenter().getY();
                case SOUTH, NORTH -> centerZ = box.getCenter().getZ();
            }

            renderer.drawMode(ComplexOverlayRenderer.DrawMode.FILL).drawPlane(new Vec3d(centerX, centerY, centerZ), selectedFace.getFaceDirection(), 1.0, Color4f.white.withAlpha(0.25f));
            planeGridRenderer.drawPlane(new Vec3d(centerX, centerY, centerZ), selectedFace.getFaceDirection(), 1.0, Color4f.white.withAlpha(0.5f));
        }

        IColorProvider colorProvider = direction -> box.isSelectable() ? Color4f.green : new Color4f(0.75f, 1.0f, 0.75f, 1.0f);
        IAlphaProvider fillAlphaProvider = direction ->
        {
            if (selectedFace != null && selectedFace.getBox().equals(box)) return 0.25f;
            else return 0.125f;
        };

        renderer.drawMode(ComplexOverlayRenderer.DrawMode.FILL).drawCuboid(box.getRenderingBox(), colorProvider, fillAlphaProvider);
        renderer.drawMode(ComplexOverlayRenderer.DrawMode.WIREFRAME).drawCuboid(box.getRenderingBox(), colorProvider, null);
    }
}
