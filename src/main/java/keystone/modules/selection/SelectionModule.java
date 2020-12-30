package keystone.modules.selection;

import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.common.models.Coords;
import keystone.modules.IKeystoneModule;
import keystone.modules.selection.boxes.SelectionBoundingBox;
import keystone.modules.selection.providers.HighlightBoxProvider;
import keystone.modules.selection.providers.SelectionBoxProvider;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashSet;
import java.util.Set;

public class SelectionModule implements IKeystoneModule
{
    private Set<SelectionBoundingBox> selectionBoxes;
    private IBoundingBoxProvider[] renderProviders;

    public SelectionModule()
    {
        MinecraftForge.EVENT_BUS.register(this);

        selectionBoxes = new HashSet<>();
        renderProviders = new IBoundingBoxProvider[]
        {
                new SelectionBoxProvider(this),
                new HighlightBoxProvider()
        };

        selectionBoxes.add(SelectionBoundingBox.from(new Coords(-1, 0, -1), new Coords(0, 2, 2)));
    }

    public Set<SelectionBoundingBox> getSelectionBoxes()
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
