package keystone.core.modules.selection;

import keystone.api.Keystone;
import keystone.core.gui.hotbar.KeystoneHotbar;
import keystone.core.gui.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.overlays.selection.SelectionNudgeScreen;
import keystone.core.modules.mouse.MouseModule;
import keystone.core.renderer.Color4f;
import keystone.core.renderer.RenderBox;
import keystone.core.renderer.RendererFactory;
import keystone.core.renderer.interfaces.IColorProvider;
import keystone.core.renderer.overlay.ComplexOverlayRenderer;
import keystone.core.renderer.overlay.WireframeOverlayRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class SelectionBoxRenderer
{
    private static final Color4f lightYellow = new Color4f(new Color(0xFFFFC0));
    private static final Color4f lightBlue =   new Color4f(new Color(0xC0C0FF));

    private final ComplexOverlayRenderer renderer;
    private final WireframeOverlayRenderer gridRenderer;
    private final MouseModule mouse;

    public SelectionBoxRenderer()
    {
        this.renderer = RendererFactory.createComplexOverlay(
                RendererFactory.createPolygonOverlay().buildFill(),
                RendererFactory.createWireframeOverlay().ignoreDepth().buildWireframe()
        );
        this.gridRenderer = RendererFactory.createWireframeOverlay().buildWireframe();
        this.mouse = Keystone.getModule(MouseModule.class);
    }

    public void render(WorldRenderContext context, SelectionBoundingBox box)
    {
        SelectedFace selectedFace = this.mouse.getSelectedFace();
        boolean selectedForNudge = box.equals(SelectionNudgeScreen.getSelectionToNudge()) && KeystoneHotbar.getSelectedSlot() == KeystoneHotbarSlot.SELECTION;

        IColorProvider boxColor = direction ->
        {
            if (selectedForNudge) return box.isFaceCorner1(direction) ? lightBlue : lightYellow;
            else return Color4f.white;
        };

        // Render Box Cuboid
        renderer.drawMode(ComplexOverlayRenderer.DrawMode.FILL);
        renderer.drawCuboid(box.getRenderingBox(), boxColor, direction ->
        {
            if (selectedFace != null && selectedFace.getBox().equals(box) && selectedFace.getFaceDirection() == direction) return 0.5f;
            else return 0.25f;
        });
        renderer.drawMode(ComplexOverlayRenderer.DrawMode.WIREFRAME);
        renderer.drawCuboid(box.getRenderingBox(), boxColor, direction ->
        {
            if (selectedFace != null && selectedFace.getBox().equals(box) && selectedFace.getFaceDirection() == direction) return 0.75f;
            else return 0.5f;
        });

        // Render Box Grid
        gridRenderer.lineWidth(1.0f).drawGrid(Vec3d.of(box.getMin()), box.getSize(), 1.0, boxColor, direction ->
        {
            if (selectedFace != null && selectedFace.getBox().equals(box) && selectedFace.getFaceDirection() == direction) return 0.75f;
            else return 0.25f;
        }, false);
        gridRenderer.revertLineWidth();

        // Render Corners If Selected
        if (box.getMin() != box.getMax() && selectedForNudge)
        {
            RenderBox corner1 = new RenderBox(box.getCorner1(), box.getCorner1());
            RenderBox corner2 = new RenderBox(box.getCorner2(), box.getCorner2());

            renderer.drawMode(ComplexOverlayRenderer.DrawMode.FILL);
            renderer.drawCuboid(corner1, Color4f.blue.withAlpha(0.25f));
            renderer.drawCuboid(corner2, Color4f.yellow.withAlpha(0.25f));

            renderer.drawMode(ComplexOverlayRenderer.DrawMode.WIREFRAME);
            renderer.drawCuboid(corner1, Color4f.blue);
            renderer.drawCuboid(corner2, Color4f.yellow);
        }
    }
}
