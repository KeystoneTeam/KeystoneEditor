package keystone.core.modules.selection.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.client.renderers.AbstractRenderer;
import keystone.core.renderer.client.renderers.OffsetBox;
import keystone.core.renderer.client.renderers.RenderHelper;
import keystone.core.modules.selection.boxes.HighlightBoundingBox;

import java.awt.*;

public class HighlightBoxRenderer extends AbstractRenderer<HighlightBoundingBox>
{
    @Override
    public void render(MatrixStack stack, HighlightBoundingBox box)
    {
        RenderHelper.enableDepthTest();
        OffsetBox bb = new OffsetBox(Player.getHighlightedBlock(), Player.getHighlightedBlock()).nudge();
        renderCuboid(bb, Color.yellow, true, false);
    }
}
