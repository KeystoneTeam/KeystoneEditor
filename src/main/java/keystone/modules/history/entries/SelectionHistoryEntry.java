package keystone.modules.history.entries;

import keystone.api.Keystone;
import keystone.modules.history.IHistoryEntry;
import keystone.modules.selection.SelectionModule;
import keystone.modules.selection.boxes.SelectionBoundingBox;

import java.util.ArrayList;
import java.util.List;

public class SelectionHistoryEntry implements IHistoryEntry
{
    private List<SelectionBoundingBox> buffer;
    private List<SelectionBoundingBox> restore;

    public SelectionHistoryEntry(List<SelectionBoundingBox> selectionBoxes, boolean includeLastBox)
    {
        buffer = new ArrayList<>(selectionBoxes.size());
        for (int i = 0; i < selectionBoxes.size(); i++) if (i < selectionBoxes.size() - 1 || includeLastBox) buffer.add(selectionBoxes.get(i).clone());
    }

    @Override
    public void undo()
    {
        restore = Keystone.getModule(SelectionModule.class).restoreSelectionBoxes(buffer);
    }
    @Override
    public void redo()
    {
        Keystone.getModule(SelectionModule.class).restoreSelectionBoxes(restore);
    }
    @Override
    public boolean addToUnsavedChanges()
    {
        return false;
    }
}
