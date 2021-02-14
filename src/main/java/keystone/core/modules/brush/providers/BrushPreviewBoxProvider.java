package keystone.core.modules.brush.providers;

import keystone.api.Keystone;
import keystone.core.modules.brush.BrushModule;
import keystone.core.modules.brush.boxes.BrushPreviewBox;
import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.common.models.DimensionId;

import java.util.ArrayList;
import java.util.List;

public class BrushPreviewBoxProvider implements IBoundingBoxProvider<BrushPreviewBox>
{
    private BrushModule brushModule;
    private List<BrushPreviewBox> box;

    @Override
    public boolean canProvide(DimensionId dimensionId)
    {
        if (brushModule == null)
        {
            brushModule = Keystone.getModule(BrushModule.class);
            if (brushModule != null)
            {
                box = new ArrayList<>();
                box.add(new BrushPreviewBox(brushModule));
            }
        }

        return Keystone.isActive() && brushModule != null && brushModule.isEnabled();
    }

    @Override
    public Iterable<BrushPreviewBox> get(DimensionId dimensionId)
    {
        return box;
    }
}
