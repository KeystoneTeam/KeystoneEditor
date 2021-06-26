package keystone.core.modules.schematic_import.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.core.modules.mouse.MouseModule;
import keystone.core.modules.schematic_import.boxes.ImportBoundingBox;
import keystone.core.modules.selection.SelectedFace;
import keystone.core.renderer.client.Camera;
import keystone.core.renderer.client.renderers.AbstractRenderer;
import keystone.core.renderer.client.renderers.OffsetBox;
import keystone.core.renderer.client.renderers.OffsetPoint;
import net.minecraft.util.math.vector.Vector3d;

import java.awt.*;

public class ImportBoxRenderer extends AbstractRenderer<ImportBoundingBox>
{
    @Override
    public void render(MatrixStack stack, ImportBoundingBox box)
    {
        OffsetBox bb = new OffsetBox(box.getMinCoords(), box.getMaxCoords());
        SelectedFace selectedFace = Keystone.getModule(MouseModule.class).getSelectedFace();

        if (selectedFace != null && selectedFace.getBox().equals(box) && selectedFace.isDraggingFace())
        {
            double centerX = (int)Camera.getX();
            double centerY = (int)Camera.getY();
            double centerZ = (int)Camera.getZ();
            switch (selectedFace.getFaceDirection())
            {
                case EAST:
                case WEST:
                    centerX = bb.getCenter().getPoint().getX();
                    break;
                case UP:
                case DOWN:
                    centerY = bb.getCenter().getPoint().getY();
                    break;
                case SOUTH:
                case NORTH:
                    centerZ = bb.getCenter().getPoint().getZ();
                    break;
            }

            renderPlane(new OffsetPoint(centerX, centerY, centerZ), selectedFace.getFaceDirection(), 1.0, direction -> Color.white, direction -> 64, false);
        }

        renderCuboid(bb, direction -> Color.green, direction ->
        {
            if (selectedFace != null && selectedFace.getBox().equals(box)) return 64;
            return 32;
        }, true, false);
    }
}
