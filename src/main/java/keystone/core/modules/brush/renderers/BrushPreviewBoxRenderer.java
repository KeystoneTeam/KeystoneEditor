package keystone.core.modules.brush.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.core.modules.brush.BrushShape;
import keystone.core.modules.brush.boxes.BrushPreviewBox;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.client.models.Point;
import keystone.core.renderer.client.renderers.AbstractRenderer;
import keystone.core.renderer.client.renderers.OffsetBox;
import keystone.core.renderer.client.renderers.OffsetPoint;

import java.awt.*;

public class BrushPreviewBoxRenderer extends AbstractRenderer<BrushPreviewBox>
{
    private static final Color color = Color.blue;

    @Override
    public void render(MatrixStack stack, BrushPreviewBox boundingBox)
    {
        int[] brushSize = boundingBox.getBrushModule().getBrushSize();
        Point center = new Point(Player.getHighlightedBlock());
        center = center.offset(brushSize[0] % 2 == 1 ? 0.5: 0, brushSize[1] % 2 == 1 ? 0.5: 0, brushSize[2] % 2 == 1 ? 0.5: 0);

        BrushShape shape = boundingBox.getBrushModule().getBrushShape();
        double xRadius = brushSize[0] * 0.5;
        double yRadius = brushSize[1] * 0.5;
        double zRadius = brushSize[2] * 0.5;

        if (shape == BrushShape.ROUND) renderSpheroid(color, center, xRadius, yRadius, zRadius);
        else if (shape == BrushShape.DIAMOND) renderDiamond(new OffsetPoint(center), xRadius, yRadius, zRadius, color, 128, false, false);
        else if (shape == BrushShape.SQUARE)
        {
            OffsetBox bb = new OffsetBox(new OffsetPoint(center).offset(-xRadius, -yRadius, -zRadius), new OffsetPoint(center).offset(xRadius, yRadius, zRadius));
            renderCuboid(bb, color, false, false);
        }
    }
}
