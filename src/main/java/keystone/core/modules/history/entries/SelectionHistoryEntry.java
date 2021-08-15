package keystone.core.modules.history.entries;

import keystone.api.Keystone;
import keystone.core.modules.history.IHistoryEntry;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.modules.selection.boxes.SelectionBoundingBox;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class SelectionHistoryEntry implements IHistoryEntry
{
    private List<SelectionBoundingBox> buffer;
    private List<SelectionBoundingBox> restore;

    public SelectionHistoryEntry(CompoundNBT nbt)
    {
        deserialize(nbt);
    }
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

    @Override
    public String id() { return "selection_boxes"; }

    @Override
    public void serialize(CompoundNBT nbt)
    {
        List<Integer> bufferNBT = new ArrayList<>(buffer.size() * 6);
        for (SelectionBoundingBox box : buffer)
        {
            bufferNBT.add(box.getCorner1().getX());
            bufferNBT.add(box.getCorner1().getY());
            bufferNBT.add(box.getCorner1().getZ());
            bufferNBT.add(box.getCorner2().getX());
            bufferNBT.add(box.getCorner2().getY());
            bufferNBT.add(box.getCorner2().getZ());
        }
        nbt.putIntArray("buffer", bufferNBT);

        if (restore != null)
        {
            List<Integer> restoreNBT = new ArrayList<>(restore.size() * 6);
            for (SelectionBoundingBox box : restore)
            {
                restoreNBT.add(box.getCorner1().getX());
                restoreNBT.add(box.getCorner1().getY());
                restoreNBT.add(box.getCorner1().getZ());
                restoreNBT.add(box.getCorner2().getX());
                restoreNBT.add(box.getCorner2().getY());
                restoreNBT.add(box.getCorner2().getZ());
            }
            nbt.putIntArray("restore", restoreNBT);
        }
    }
    @Override
    public void deserialize(CompoundNBT nbt)
    {
        int[] bufferNBT = nbt.getIntArray("buffer");
        buffer = new ArrayList<>(bufferNBT.length / 6);
        for (int i = 0; i < bufferNBT.length; i += 6)
        {
            buffer.add(SelectionBoundingBox.create(bufferNBT[i], bufferNBT[i + 1], bufferNBT[i + 2], bufferNBT[i + 3], bufferNBT[i + 4], bufferNBT[i + 5]));
        }

        if (nbt.contains("restore", Constants.NBT.TAG_INT_ARRAY))
        {
            int[] restoreNBT = nbt.getIntArray("restore");
            restore = new ArrayList<>(restoreNBT.length / 6);
            for (int i = 0; i < restoreNBT.length; i += 6)
            {
                restore.add(SelectionBoundingBox.create(restoreNBT[i], restoreNBT[i + 1], restoreNBT[i + 2], restoreNBT[i + 3], restoreNBT[i + 4], restoreNBT[i + 5]));
            }
        }
    }
}
