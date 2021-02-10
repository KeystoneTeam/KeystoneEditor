package keystone.core.modules.paste.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.core.renderer.client.renderers.AbstractRenderer;
import keystone.core.renderer.client.renderers.OffsetBox;
import keystone.core.modules.mouse.MouseModule;
import keystone.core.modules.paste.boxes.PasteBoundingBox;
import keystone.core.modules.selection.SelectedFace;

import java.awt.*;

public class PasteBoxRenderer extends AbstractRenderer<PasteBoundingBox>
{
    @Override
    public void render(MatrixStack stack, PasteBoundingBox box)
    {
        OffsetBox bb = new OffsetBox(box.getMinCoords(), box.getMaxCoords());
        SelectedFace selectedFace = Keystone.getModule(MouseModule.class).getSelectedFace();

        box.getGhostBlocks().render(stack, box.getMinCoords());
        renderCuboid(bb, direction -> Color.green, direction ->
        {
            if (selectedFace != null && selectedFace.getBox().equals(box)) return 64;
            return 32;
        }, true, false);
    }
}
