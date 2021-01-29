package keystone.modules.history.entries;

import keystone.api.Keystone;
import keystone.api.SelectionBox;
import keystone.modules.history.HistoryModule;
import keystone.modules.history.IHistoryEntry;
import keystone.modules.paste.CloneModule;
import keystone.modules.paste.boxes.PasteBoundingBox;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class PasteHistoryEntry extends FillHistoryEntry
{
    private List<PasteBoundingBox> pastes;

    private static final SelectionBox[] getPasteSelections(World world, List<PasteBoundingBox> pastes)
    {
        SelectionBox[] selections = new SelectionBox[pastes.size()];
        for (int i = 0; i < selections.length; i++)
        {
            PasteBoundingBox paste = pastes.get(i);
            selections[i] = new SelectionBox(paste.getMinCoords(), paste.getMaxCoords(), world);
        }
        return selections;
    }
    public PasteHistoryEntry(World world, List<PasteBoundingBox> pastes)
    {
        super(world, getPasteSelections(world, pastes));

        this.pastes = new ArrayList<>(pastes.size());
        for (int i = 0; i < pastes.size(); i++) this.pastes.add(pastes.get(i).clone());
    }

    public void updateSelectionBuffers()
    {
        for (SelectionBox box : boxes) box.forEachBlock(pos -> box.setBlock(pos, world.getBlockState(pos)));
    }

    @Override
    public void onPushToHistory(HistoryModule history, boolean beforePush)
    {
        if (beforePush) history.pushToHistory(new PrePasteEvent(this));
    }
    @Override
    public void undo()
    {
        super.undo();
        Keystone.getModule(CloneModule.class).setPasteBoxes(this.pastes);
    }

    //region Pre
    class PrePasteEvent implements IHistoryEntry
    {
        private PasteHistoryEntry main;

        PrePasteEvent(PasteHistoryEntry main)
        {
            this.main = main;
        }

        @Override public boolean addToUnsavedChanges() { return false; }

        @Override
        public void undo()
        {
            Keystone.getModule(CloneModule.class).clearPasteBoxes();
        }
        @Override
        public void redo()
        {
            Keystone.getModule(CloneModule.class).setPasteBoxes(this.main.pastes);
        }
    }
    //endregion
}
