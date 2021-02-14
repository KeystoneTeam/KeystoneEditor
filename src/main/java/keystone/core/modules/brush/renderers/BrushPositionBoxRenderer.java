package keystone.core.modules.brush.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.core.modules.brush.boxes.BrushPositionBox;
import keystone.core.renderer.client.renderers.AbstractRenderer;
import keystone.core.renderer.client.renderers.OffsetBox;

import java.awt.*;

public class BrushPositionBoxRenderer extends AbstractRenderer<BrushPositionBox>
{
    @Override
    public void render(MatrixStack stack, BrushPositionBox boundingBox)
    {
        OffsetBox bb = new OffsetBox(boundingBox.getPosition(), boundingBox.getPosition());
        renderOutlinedCuboid(bb, Color.yellow, true);
    }
}
