package keystone.core.modules.brush.providers;

import keystone.api.Keystone;
import keystone.core.modules.brush.BrushModule;
import keystone.core.modules.brush.boxes.BrushPositionBox;
import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.common.models.DimensionId;

public class BrushPositionBoxProvider implements IBoundingBoxProvider<BrushPositionBox>
{
    private BrushModule brushModule;

    @Override
    public boolean canProvide(DimensionId dimensionId)
    {
        if (brushModule == null) brushModule = Keystone.getModule(BrushModule.class);

        return Keystone.isActive() && brushModule != null && brushModule.isEnabled();
    }

    @Override
    public Iterable<BrushPositionBox> get(DimensionId dimensionId)
    {
        return brushModule.getBrushPositionBoxes();
    }
}
