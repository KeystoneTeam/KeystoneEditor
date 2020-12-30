package keystone.modules.selection;

import keystone.core.renderer.client.renderers.AbstractRenderer;
import keystone.core.renderer.client.renderers.OffsetBox;

import java.awt.*;

public class SelectionBoxRenderer extends AbstractRenderer<SelectionBox>
{
    @Override
    public void render(SelectionBox box)
    {
        OffsetBox bb = new OffsetBox(box.getMinCoords(), box.getMaxCoords());
        renderCuboid(bb, Color.white, false);

        if (box.getMinCoords() != box.getMaxCoords())
        {
            OffsetBox min = new OffsetBox(box.getMinCoords(), box.getMinCoords());
            min.nudge();
            renderCuboid(min, Color.blue, true);

            OffsetBox max = new OffsetBox(box.getMaxCoords(), box.getMaxCoords());
            max.nudge();
            renderCuboid(max, Color.yellow, true);
        }
    }
}
