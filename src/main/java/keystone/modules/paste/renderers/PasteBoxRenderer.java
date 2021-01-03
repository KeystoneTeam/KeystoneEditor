package keystone.modules.paste.renderers;

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
    public void render(PasteBoundingBox box)
    {
        OffsetBox bb = new OffsetBox(box.getMinCoords(), box.getMaxCoords());
        SelectedFace selectedFace = Keystone.getModule(SelectionModule.class).getSelectedFace();

        renderCuboid(bb, direction -> Color.green, direction ->
        {
            if (selectedFace != null && selectedFace.getBox().equals(box)) return 64;
            return 32;
        }, true);
    }
}
