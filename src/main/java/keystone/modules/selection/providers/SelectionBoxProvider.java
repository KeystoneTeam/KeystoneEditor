package keystone.modules.selection.providers;

import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.common.models.DimensionId;
import keystone.modules.selection.boxes.SelectionBoundingBox;
import keystone.modules.selection.SelectionModule;

public class SelectionBoxProvider implements IBoundingBoxProvider<SelectionBoundingBox>
{
    private SelectionModule module;

    public SelectionBoxProvider(SelectionModule module)
    {
        this.module = module;
    }

    @Override
    public Iterable<SelectionBoundingBox> get(DimensionId dimensionId)
    {
        return module.getSelectionBoundingBoxes();
    }
}
