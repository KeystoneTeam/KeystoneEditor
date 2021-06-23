package keystone.core.modules.history.entries;

import keystone.api.Keystone;
import keystone.core.modules.history.IHistoryEntry;
import keystone.core.modules.clipboard.ClipboardModule;
import keystone.core.modules.clipboard.boxes.PasteBoundingBox;
import keystone.core.renderer.common.models.AbstractBoundingBox;
import keystone.core.renderer.common.models.Coords;
import keystone.core.schematic.KeystoneSchematic;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

import java.util.ArrayList;
import java.util.List;

public class PasteBoxHistoryEntry implements IHistoryEntry
{
    public class PasteBoxDescription
    {
        public final Coords minCoords;
        public final KeystoneSchematic schematic;
        public final Rotation rotation;
        public final Mirror mirror;
        public final int scale;

        public PasteBoxDescription(PasteBoundingBox box)
        {
            this.minCoords = box.getCorner1();
            this.schematic = box.getSchematic();
            this.rotation = box.getRotation();
            this.mirror = box.getMirror();
            this.scale = box.getScale();
        }

        public PasteBoundingBox createPasteBox()
        {
            PasteBoundingBox box = PasteBoundingBox.create(this.minCoords, this.schematic);
            box.setOrientation(rotation, mirror);
            box.setScale(scale);
            return box;
        }
    }

    private List<PasteBoxDescription> buffer;
    private List<PasteBoxDescription> restore;

    public PasteBoxHistoryEntry(List<PasteBoundingBox> pasteBoxes)
    {
        buffer = new ArrayList<>(pasteBoxes.size());
        for (int i = 0; i < pasteBoxes.size(); i++) buffer.add(new PasteBoxDescription(pasteBoxes.get(i)));
    }

    @Override
    public void undo()
    {
        List<PasteBoundingBox> pasteBoxes = new ArrayList<>(buffer.size());
        for (PasteBoxDescription description : buffer) pasteBoxes.add(description.createPasteBox());
        List<PasteBoundingBox> oldBoxes = Keystone.getModule(ClipboardModule.class).restorePasteBoxes(pasteBoxes);
        restore = new ArrayList<>(oldBoxes.size());
        for (PasteBoundingBox oldBox : oldBoxes) restore.add(new PasteBoxDescription(oldBox));
    }
    @Override
    public void redo()
    {
        List<PasteBoundingBox> pasteBoxes = new ArrayList<>(restore.size());
        for (PasteBoxDescription description : restore) pasteBoxes.add(description.createPasteBox());
        Keystone.getModule(ClipboardModule.class).restorePasteBoxes(pasteBoxes);
    }
}
