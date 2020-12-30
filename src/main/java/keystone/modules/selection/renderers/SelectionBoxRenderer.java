package keystone.modules.selection.renderers;

import keystone.core.renderer.client.renderers.AbstractRenderer;
import keystone.core.renderer.client.renderers.OffsetBox;
import keystone.modules.selection.boxes.SelectionBoundingBox;

import java.awt.*;

public class SelectionBoxRenderer extends AbstractRenderer<SelectionBoundingBox>
{
    @Override
    public void render(SelectionBoundingBox box)
    {
        OffsetBox bb = new OffsetBox(box.getMinCoords(), box.getMaxCoords());
        renderCuboid(bb, Color.white, false);

        if (box.getMinCoords() != box.getMaxCoords())
        {
            OffsetBox min = new OffsetBox(box.getMinCoords(), box.getMinCoords()).nudge();
            renderCuboid(min, Color.blue, true);

            OffsetBox max = new OffsetBox(box.getMaxCoords(), box.getMaxCoords()).nudge();
            renderCuboid(max, Color.yellow, true);
        }
    }
}
