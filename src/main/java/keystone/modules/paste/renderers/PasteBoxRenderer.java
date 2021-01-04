package keystone.modules.paste.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.core.renderer.client.renderers.AbstractRenderer;
import keystone.core.renderer.client.renderers.OffsetBox;
import keystone.modules.paste.boxes.PasteBoundingBox;
import keystone.modules.selection.SelectedFace;
import keystone.modules.selection.SelectionModule;

import java.awt.*;

public class PasteBoxRenderer extends AbstractRenderer<PasteBoundingBox>
{
    @Override
    public void render(MatrixStack stack, PasteBoundingBox box)
    {
        OffsetBox bb = new OffsetBox(box.getMinCoords(), box.getMaxCoords());
        SelectedFace selectedFace = Keystone.getModule(SelectionModule.class).getSelectedFace();

        box.getGhostBlocks().render(stack, box.getMinCoords());
        renderCuboid(bb, direction -> Color.green, direction ->
        {
            if (selectedFace != null && selectedFace.getBox().equals(box)) return 64;
            return 32;
        }, true);
    }
}
