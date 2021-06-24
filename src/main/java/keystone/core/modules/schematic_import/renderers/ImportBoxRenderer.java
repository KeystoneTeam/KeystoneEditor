package keystone.core.modules.schematic_import.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.core.modules.schematic_import.boxes.ImportBoundingBox;
import keystone.core.renderer.client.renderers.AbstractRenderer;
import keystone.core.renderer.client.renderers.OffsetBox;
import keystone.core.modules.mouse.MouseModule;
import keystone.core.modules.selection.SelectedFace;

import java.awt.*;

public class ImportBoxRenderer extends AbstractRenderer<ImportBoundingBox>
{
    @Override
    public void render(MatrixStack stack, ImportBoundingBox box)
    {
        OffsetBox bb = new OffsetBox(box.getMinCoords(), box.getMaxCoords());
        SelectedFace selectedFace = Keystone.getModule(MouseModule.class).getSelectedFace();

        renderCuboid(bb, direction -> Color.green, direction ->
        {
            if (selectedFace != null && selectedFace.getBox().equals(box)) return 64;
            return 32;
        }, true, false);
    }
}
