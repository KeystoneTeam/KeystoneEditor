package keystone.modules.selection;

import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.common.models.DimensionId;

public class SelectionBoxProvider implements IBoundingBoxProvider<SelectionBox>
{
    private SelectionModule module;

    public SelectionBoxProvider(SelectionModule module)
    {
        this.module = module;
    }

    @Override
    public Iterable<SelectionBox> get(DimensionId dimensionId)
    {
        return module.getSelectionBoxes();
    }
}
