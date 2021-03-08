package keystone.core.modules.history.entries;

import keystone.api.Keystone;
import keystone.core.modules.history.IHistoryEntry;
import keystone.core.modules.clipboard.ClipboardModule;
import keystone.core.modules.clipboard.boxes.PasteBoundingBox;

import java.util.ArrayList;
import java.util.List;

public class PasteBoxHistoryEntry implements IHistoryEntry
{
    private List<PasteBoundingBox> buffer;
    private List<PasteBoundingBox> restore;

    public PasteBoxHistoryEntry(List<PasteBoundingBox> pasteBoxes)
    {
        buffer = new ArrayList<>(pasteBoxes.size());
        for (int i = 0; i < pasteBoxes.size(); i++) buffer.add(pasteBoxes.get(i).clone());
    }

    @Override
    public void undo()
    {
        restore = Keystone.getModule(ClipboardModule.class).restorePasteBoxes(buffer);
    }
    @Override
    public void redo()
    {
        Keystone.getModule(ClipboardModule.class).restorePasteBoxes(restore);
    }
}
