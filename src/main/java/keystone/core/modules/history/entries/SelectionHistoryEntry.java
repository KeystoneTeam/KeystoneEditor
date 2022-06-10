package keystone.core.modules.history.entries;

import keystone.api.Keystone;
import keystone.core.gui.screens.selection.SelectionNudgeScreen;
import keystone.core.modules.history.IHistoryEntry;
import keystone.core.modules.selection.SelectionBoundingBox;
import keystone.core.modules.selection.SelectionModule;
import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;
import java.util.List;

public class SelectionHistoryEntry implements IHistoryEntry
{
    private final SelectionModule selectionModule = Keystone.getModule(SelectionModule.class);
    private List<SelectionBoundingBox> boxes;
    private int selectedBox;

    public SelectionHistoryEntry(NbtCompound nbt)
    {
        deserialize(nbt);
    }
    public SelectionHistoryEntry(List<SelectionBoundingBox> selectionBoxes)
    {
        boxes = new ArrayList<>(selectionBoxes.size());
        for (SelectionBoundingBox selectionBox : selectionBoxes) boxes.add(selectionBox.clone());
        selectedBox = SelectionNudgeScreen.getSelectionIndex();
    }

    @Override
    public void apply()
    {
        selectionModule.setSelectionBoxes(boxes, false);
        SelectionNudgeScreen.setSelectedIndex(selectedBox);
    }
    @Override
    public boolean addToUnsavedChanges()
    {
        return false;
    }

    @Override
    public String id() { return "selection_boxes"; }

    @Override
    public void serialize(NbtCompound nbt)
    {
        List<Integer> bufferNBT = new ArrayList<>(boxes.size() * 6);
        for (SelectionBoundingBox box : boxes)
        {
            bufferNBT.add(box.getCorner1().getX());
            bufferNBT.add(box.getCorner1().getY());
            bufferNBT.add(box.getCorner1().getZ());
            bufferNBT.add(box.getCorner2().getX());
            bufferNBT.add(box.getCorner2().getY());
            bufferNBT.add(box.getCorner2().getZ());
        }
        nbt.putIntArray("boxes", bufferNBT);
        nbt.putInt("selectedBox", selectedBox);
    }
    @Override
    public void deserialize(NbtCompound nbt)
    {
        int[] bufferNBT = nbt.getIntArray("boxes");
        boxes = new ArrayList<>(bufferNBT.length / 6);
        for (int i = 0; i < bufferNBT.length; i += 6)
        {
            boxes.add(SelectionBoundingBox.create(bufferNBT[i], bufferNBT[i + 1], bufferNBT[i + 2], bufferNBT[i + 3], bufferNBT[i + 4], bufferNBT[i + 5]));
        }
        selectedBox = nbt.getInt("selectedBox");
    }
}
