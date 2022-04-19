package keystone.core.modules.selection;

import keystone.api.Keystone;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.screens.selection.SelectionNudgeScreen;
import keystone.core.modules.mouse.MouseModule;
import keystone.core.renderer.Color4f;
import keystone.core.renderer.OverlayRenderer;
import keystone.core.renderer.RenderBox;
import keystone.core.renderer.RendererFactory;
import keystone.core.renderer.color.ColorProviderFactory;
import keystone.core.renderer.color.IColorProvider;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class SelectionBoxRenderer
{
    private static final Color4f lightYellow = new Color4f(new Color(0xFFFFC0));
    private static final Color4f lightBlue =   new Color4f(new Color(0xC0C0FF));

    private static final IColorProvider blue = ColorProviderFactory.staticColor(Color4f.blue);
    private static final IColorProvider yellow = ColorProviderFactory.staticColor(Color4f.yellow);

    private final OverlayRenderer edgeRenderer;
    private final OverlayRenderer fillRenderer;
    private final MouseModule mouse;

    public SelectionBoxRenderer()
    {
        this.edgeRenderer = RendererFactory.createWorldspaceOverlay().ignoreDepth().wireframe().build();
        this.fillRenderer = RendererFactory.createWorldspaceOverlay().build();
        this.mouse = Keystone.getModule(MouseModule.class);
    }

    public void render(WorldRenderContext context, SelectionBoundingBox box)
    {
        SelectedFace selectedFace = this.mouse.getSelectedFace();
        boolean selectedForNudge = box.equals(SelectionNudgeScreen.getSelectionToNudge()) && KeystoneHotbar.getSelectedSlot() == KeystoneHotbarSlot.SELECTION;

        ColorProviderFactory.ColorProviderBuilder colorProvider = ColorProviderFactory.colorProvider(direction ->
        {
            if (selectedForNudge) return box.isFaceCorner1(direction) ? lightBlue : lightYellow;
            else return Color4f.white;
        });
        ColorProviderFactory.ColorProviderBuilder fillColorProvider = colorProvider.withAlphaProvider(direction ->
        {
            if (selectedFace != null && selectedFace.getBox().equals(box) && selectedFace.getFaceDirection() == direction) return 0.5f;
            else return 0.125f;
        });

        // Render Box Cuboid
        fillRenderer.drawGrid(Vec3d.of(box.getMin()), box.getSize(), 1.0, colorProvider.withAlphaProvider(direction ->
        {
            if (selectedFace != null && selectedFace.getBox().equals(box) && selectedFace.getFaceDirection() == direction) return 1.0f;
            else return 0.25f;
        }), 5.0f, false);
        fillRenderer.drawCuboid(box.getRenderingBox(), fillColorProvider);
        edgeRenderer.drawCuboid(box.getRenderingBox(), fillColorProvider);

        // Render Corners If Selected
        if (box.getMin() != box.getMax() && selectedForNudge)
        {
            RenderBox corner1 = new RenderBox(box.getCorner1(), box.getCorner1()).nudge();
            RenderBox corner2 = new RenderBox(box.getCorner2(), box.getCorner2()).nudge();

            edgeRenderer.drawCuboid(corner1, blue);
            edgeRenderer.drawCuboid(corner2, yellow);
        }
    }
}
