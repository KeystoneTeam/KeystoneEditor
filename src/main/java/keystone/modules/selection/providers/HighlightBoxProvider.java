package keystone.modules.selection.providers;

import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.common.models.DimensionId;
import keystone.modules.selection.boxes.HighlightBoundingBox;

import java.util.HashSet;
import java.util.Set;

public class HighlightBoxProvider implements IBoundingBoxProvider<HighlightBoundingBox>
{
    private Set<HighlightBoundingBox> box = new HashSet<>();
    public HighlightBoxProvider()
    {
        box.add(new HighlightBoundingBox());
    }

    @Override
    public Iterable<HighlightBoundingBox> get(DimensionId dimensionId)
    {
        return box;
    }
}
