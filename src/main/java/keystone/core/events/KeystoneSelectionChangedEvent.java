package keystone.core.events;

import keystone.core.modules.selection.boxes.SelectionBoundingBox;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public class KeystoneSelectionChangedEvent extends Event
{
    public final SelectionBoundingBox[] selections;
    public final boolean createdSelection;

    public KeystoneSelectionChangedEvent(List<SelectionBoundingBox> selections, boolean createdSelection)
    {
        this.selections = new SelectionBoundingBox[selections.size()];
        for (int i = 0; i < selections.size(); i++) this.selections[i] = selections.get(i);
        this.createdSelection = createdSelection;
    }
}
