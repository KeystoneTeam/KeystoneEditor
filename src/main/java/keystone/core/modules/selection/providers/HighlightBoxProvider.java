package keystone.core.modules.selection.providers;

import keystone.api.Keystone;
import keystone.core.modules.mouse.MouseModule;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.modules.selection.boxes.HighlightBoundingBox;
import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.common.models.DimensionId;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class HighlightBoxProvider implements IBoundingBoxProvider<HighlightBoundingBox>
{
    private SelectionModule selectionModule;
    private MouseModule mouseModule;
    private Set<HighlightBoundingBox> box = Collections.synchronizedSet(new HashSet<>());

    public HighlightBoxProvider()
    {
        box.add(new HighlightBoundingBox());
    }

    @Override
    public boolean canProvide(DimensionId dimensionId)
    {
        if (selectionModule == null) selectionModule = Keystone.getModule(SelectionModule.class);
        if (mouseModule == null) mouseModule = Keystone.getModule(MouseModule.class);
        if (selectionModule == null || mouseModule == null) return false;

        return selectionModule.isEnabled() && !selectionModule.isCreatingSelection() && mouseModule.getSelectedFace() == null;
    }

    @Override
    public Iterable<HighlightBoundingBox> get(DimensionId dimensionId) { return box; }
}
