package keystone.modules.selection;

import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.common.models.Coords;
import keystone.modules.IKeystoneModule;

import java.util.HashSet;
import java.util.Set;

public class SelectionModule implements IKeystoneModule
{
    private Set<SelectionBox> selectionBoxes;
    private IBoundingBoxProvider[] renderProviders;

    public SelectionModule()
    {
        selectionBoxes = new HashSet<>();
        renderProviders = new IBoundingBoxProvider[]
        {
            new SelectionBoxProvider(this)
        };

        selectionBoxes.add(SelectionBox.from(new Coords(-1, 0, -1), new Coords(0, 2, 2)));
    }

    public Set<SelectionBox> getSelectionBoxes()
    {
        return selectionBoxes;
    }

    @Override
    public IBoundingBoxProvider[] getBoundingBoxProviders()
    {
        return renderProviders;
    }

    @Override
    public void tick()
    {

    }
}
