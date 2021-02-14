package keystone.core.modules.selection.providers;

import keystone.core.KeystoneGlobalState;
import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.common.models.DimensionId;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.modules.selection.boxes.SelectionBoundingBox;

public class SelectionBoxProvider implements IBoundingBoxProvider<SelectionBoundingBox>
{
    private SelectionModule module;

    public SelectionBoxProvider(SelectionModule module)
    {
        this.module = module;
    }

    @Override
    public boolean canProvide(DimensionId dimensionId)
    {
        return !KeystoneGlobalState.HideSelectionBoxes;
    }
    @Override
    public Iterable<SelectionBoundingBox> get(DimensionId dimensionId)
    {
        return module.getSelectionBoundingBoxes();
    }
}
