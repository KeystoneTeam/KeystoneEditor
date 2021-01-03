package keystone.modules.paste.providers;

import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.common.models.DimensionId;
import keystone.modules.paste.PasteModule;
import keystone.modules.paste.boxes.PasteBoundingBox;

public class PasteBoxProvider implements IBoundingBoxProvider<PasteBoundingBox>
{
    private PasteModule pasteModule;

    public PasteBoxProvider(PasteModule pasteModule)
    {
        this.pasteModule = pasteModule;
    }

    @Override
    public Iterable<PasteBoundingBox> get(DimensionId dimensionId)
    {
        return pasteModule.getPasteBoxes();
    }
}
