package keystone.modules.selection.renderers;

import keystone.core.renderer.client.Player;
import keystone.core.renderer.client.renderers.AbstractRenderer;
import keystone.core.renderer.client.renderers.OffsetBox;
import keystone.core.renderer.client.renderers.RenderHelper;
import keystone.modules.selection.boxes.HighlightBoundingBox;

import java.awt.*;

public class HighlightBoxRenderer extends AbstractRenderer<HighlightBoundingBox>
{
    @Override
    public void render(HighlightBoundingBox box)
    {
        RenderHelper.enableDepthTest();

        OffsetBox bb = new OffsetBox(Player.getHighlightedBlock(), Player.getHighlightedBlock()).nudge();
        renderOutlinedCuboid(bb, Color.yellow);
    }
}
