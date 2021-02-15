package keystone.core.modules.brush.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.core.modules.brush.BrushShape;
import keystone.core.modules.brush.boxes.BrushPreviewBox;
import keystone.core.renderer.client.Player;
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
        OffsetPoint center = new OffsetPoint(Player.getHighlightedBlock());
        center = center.offset(brushSize[0] % 2 == 1 ? 0.5: 0, brushSize[1] % 2 == 1 ? 0.5: 0, brushSize[2] % 2 == 1 ? 0.5: 0);

        BrushShape shape = boundingBox.getBrushModule().getBrushShape();
        double xRadius = brushSize[0] * 0.5;
        double yRadius = brushSize[1] * 0.5;
        double zRadius = brushSize[2] * 0.5;

        if (shape == BrushShape.ROUND)
        {
            boolean cull = !shape.isPositionInShape(Player.getEyePosition(), center.getPoint(), brushSize[0], brushSize[1], brushSize[2]);
            renderSphere(center, xRadius, yRadius, zRadius, color, 128, false, false, cull);
        }
        else if (shape == BrushShape.DIAMOND)
        {
            boolean cull = !shape.isPositionInShape(Player.getEyePosition(), center.getPoint(), brushSize[0], brushSize[1], brushSize[2]);
            renderDiamond(center, xRadius, yRadius, zRadius, color, 128, false, false, cull);
        }
        else if (shape == BrushShape.SQUARE)
        {
            OffsetBox bb = new OffsetBox(center.offset(-xRadius, -yRadius, -zRadius), center.offset(xRadius, yRadius, zRadius));
            renderCuboid(bb, color, false, false);
        }
    }
}
