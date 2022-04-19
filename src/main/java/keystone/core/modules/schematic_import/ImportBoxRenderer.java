package keystone.core.modules.schematic_import;

import keystone.api.Keystone;
import keystone.core.client.Camera;
import keystone.core.modules.mouse.MouseModule;
import keystone.core.modules.selection.SelectedFace;
import keystone.core.renderer.Color4f;
import keystone.core.renderer.OverlayRenderer;
import keystone.core.renderer.RendererFactory;
import keystone.core.renderer.color.ColorProviderFactory;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.Vec3d;

public class ImportBoxRenderer
{
    private final OverlayRenderer edgeRenderer;
    private final OverlayRenderer fillRenderer;
    private final MouseModule mouse;

    public ImportBoxRenderer()
    {
        this.edgeRenderer = RendererFactory.createWorldspaceOverlay().ignoreDepth().wireframe().build();
        this.fillRenderer = RendererFactory.createWorldspaceOverlay().build();
        this.mouse = Keystone.getModule(MouseModule.class);
    }

    public void render(WorldRenderContext context, ImportBoundingBox box)
    {
        SelectedFace selectedFace = mouse.getSelectedFace();
        if (selectedFace != null && selectedFace.getBox().equals(box) && selectedFace.isDraggingFace())
        {
            double centerX = (int)Camera.getX();
            double centerY = (int)Camera.getY();
            double centerZ = (int)Camera.getZ();
            switch (selectedFace.getFaceDirection())
            {
                case EAST:
                case WEST:
                    centerX = box.getCenter().getX();
                    break;
                case UP:
                case DOWN:
                    centerY = box.getCenter().getY();
                    break;
                case SOUTH:
                case NORTH:
                    centerZ = box.getCenter().getZ();
                    break;
            }

            fillRenderer.drawPlane(new Vec3d(centerX, centerY, centerZ), selectedFace.getFaceDirection(), 1.0, ColorProviderFactory.staticColor(Color4f.white.withAlpha(0.25f)), 0.5f, 5.0f);
        }

        ColorProviderFactory.ColorProviderBuilder fillProvider = ColorProviderFactory.colorProvider(direction -> box.isSelectable() ? Color4f.green : new Color4f(0.75f, 1.0f, 0.75f, 1.0f));
        fillRenderer.drawCuboid(box.getRenderingBox(), fillProvider.withAlphaProvider(direction ->
                {
                    if (selectedFace != null && selectedFace.getBox().equals(box)) return 0.25f;
                    else return 0.125f;
                }));
        edgeRenderer.drawCuboid(box.getRenderingBox(), fillProvider);
    }
}
