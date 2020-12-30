package keystone.core.renderer.client.renderers;

import keystone.core.renderer.common.models.BoundingBoxCuboid;

import java.awt.*;

public class CuboidRenderer extends AbstractRenderer<BoundingBoxCuboid>
{
    @Override
    public void render(BoundingBoxCuboid boundingBox)
    {
        OffsetBox bb = new OffsetBox(boundingBox.getMinCoords(), boundingBox.getMaxCoords());
        renderCuboid(bb, Color.white, false);
    }
}
