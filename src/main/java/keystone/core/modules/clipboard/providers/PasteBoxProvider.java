package keystone.core.modules.clipboard.providers;

import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.common.models.DimensionId;
import keystone.core.modules.clipboard.ClipboardModule;
import keystone.core.modules.clipboard.boxes.PasteBoundingBox;

public class PasteBoxProvider implements IBoundingBoxProvider<PasteBoundingBox>
{
    private ClipboardModule clipboardModule;

    public PasteBoxProvider(ClipboardModule clipboardModule)
    {
        this.clipboardModule = clipboardModule;
    }

    @Override
    public Iterable<PasteBoundingBox> get(DimensionId dimensionId)
    {
        return clipboardModule.getPasteBoxes();
    }
}
