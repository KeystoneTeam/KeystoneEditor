package keystone.core.modules.selection.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.core.gui.screens.selection.SelectionNudgeScreen;
import keystone.core.modules.mouse.MouseModule;
import keystone.core.modules.selection.SelectedFace;
import keystone.core.modules.selection.boxes.SelectionBoundingBox;
import keystone.core.renderer.client.renderers.AbstractRenderer;
import keystone.core.renderer.client.renderers.OffsetBox;
import net.minecraft.util.Direction;

import java.awt.*;
import java.util.function.Function;

public class SelectionBoxRenderer extends AbstractRenderer<SelectionBoundingBox>
{
    private static final Color yellow = new Color(0xFFFFC0);
    private static final Color blue = new Color(0xC0C0FF);

    @Override
    public void render(MatrixStack stack, SelectionBoundingBox box)
    {
        OffsetBox bb = new OffsetBox(box.getMinCoords(), box.getMaxCoords());
        SelectedFace selectedFace = Keystone.getModule(MouseModule.class).getSelectedFace();

        boolean selectedForNudge = box.equals(SelectionNudgeScreen.getSelectionToNudge());
        Function<Direction, Color> colorProvider = direction ->
        {
            if (selectedForNudge) return box.isFaceCorner1(direction) ? blue : yellow;
            else return Color.white;
        };
        Function<Direction, Integer> alphaProvider = direction ->
        {
            if (selectedFace != null && selectedFace.getBox().equals(box) && selectedFace.getFaceDirection() == direction) return 128;
            else return 32;
        };

        // Render Box Cuboid
        renderGrid(bb.getMin(), bb.getSize(), 1.0, colorProvider, direction ->
        {
            if (selectedFace != null && selectedFace.getBox().equals(box) && selectedFace.getFaceDirection() == direction) return 255;
            else return 64;
        }, false, false);
        renderCuboid(bb, colorProvider, alphaProvider, true, false);

        // Render Corners If Selected
        if (box.getMinCoords() != box.getMaxCoords() && selectedForNudge)
        {
            OffsetBox min = new OffsetBox(box.getCorner1(), box.getCorner1()).nudge();
            renderCuboid(min, Color.blue, true, true);

            OffsetBox max = new OffsetBox(box.getCorner2(), box.getCorner2()).nudge();
            renderCuboid(max, Color.yellow, true, true);
        }
    }
}
