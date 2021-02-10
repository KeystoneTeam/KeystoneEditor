package keystone.core.modules.selection.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.core.renderer.client.renderers.AbstractRenderer;
import keystone.core.renderer.client.renderers.OffsetBox;
import keystone.core.modules.mouse.MouseModule;
import keystone.core.modules.selection.SelectedFace;
import keystone.core.modules.selection.boxes.SelectionBoundingBox;

import java.awt.*;

public class SelectionBoxRenderer extends AbstractRenderer<SelectionBoundingBox>
{
    @Override
    public void render(MatrixStack stack, SelectionBoundingBox box)
    {
        OffsetBox bb = new OffsetBox(box.getMinCoords(), box.getMaxCoords());
        SelectedFace selectedFace = Keystone.getModule(MouseModule.class).getSelectedFace();

        renderCuboid(bb, direction -> Color.white, direction ->
        {
            if (selectedFace != null && selectedFace.getBox().equals(box) && selectedFace.getFaceDirection() == direction) return 128;
            else return 32;
        }, true, false);

        if (box.getMinCoords() != box.getMaxCoords())
        {
            OffsetBox min = new OffsetBox(box.getCorner1(), box.getCorner1()).nudge();
            renderCuboid(min, Color.yellow, true, false);

            OffsetBox max = new OffsetBox(box.getCorner2(), box.getCorner2()).nudge();
            renderCuboid(max, Color.blue, true, false);
        }
    }
}
