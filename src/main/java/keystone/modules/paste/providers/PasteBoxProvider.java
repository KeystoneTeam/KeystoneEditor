package keystone.modules.paste.providers;

import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.common.models.DimensionId;
import keystone.modules.paste.CloneModule;
import keystone.modules.paste.boxes.PasteBoundingBox;

public class PasteBoxProvider implements IBoundingBoxProvider<PasteBoundingBox>
{
    private CloneModule cloneModule;

    public PasteBoxProvider(CloneModule cloneModule)
    {
        this.cloneModule = cloneModule;
    }

    @Override
    public Iterable<PasteBoundingBox> get(DimensionId dimensionId)
    {
        return cloneModule.getPasteBoxes();
    }
}
