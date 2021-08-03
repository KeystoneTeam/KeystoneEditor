package keystone.core.modules.history.entries;

import keystone.api.Keystone;
import keystone.core.modules.history.IHistoryEntry;
import keystone.core.modules.schematic_import.ImportModule;
import keystone.core.modules.schematic_import.boxes.ImportBoundingBox;
import keystone.core.renderer.common.models.Coords;
import keystone.core.schematic.KeystoneSchematic;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

import java.util.ArrayList;
import java.util.List;

public class ImportBoxesHistoryEntry implements IHistoryEntry
{
    public static class ImportBoxDescription
    {
        public final Coords minCoords;
        public final KeystoneSchematic schematic;
        public final Rotation rotation;
        public final Mirror mirror;
        public final int scale;

        public ImportBoxDescription(ImportBoundingBox box)
        {
            this.minCoords = box.getCorner1();
            this.schematic = box.getSchematic();
            this.rotation = box.getRotation();
            this.mirror = box.getMirror();
            this.scale = box.getScale();
        }

        public ImportBoundingBox createImportBox()
        {
            ImportBoundingBox box = ImportBoundingBox.create(this.minCoords, this.schematic);
            box.setOrientation(rotation, mirror);
            box.setScale(scale);
            return box;
        }
    }

    private List<ImportBoxDescription> buffer;
    private List<ImportBoxDescription> restore;

    public ImportBoxesHistoryEntry(List<ImportBoundingBox> importBoxes)
    {
        buffer = new ArrayList<>(importBoxes.size());
        for (int i = 0; i < importBoxes.size(); i++) buffer.add(new ImportBoxDescription(importBoxes.get(i)));
    }

    @Override
    public void undo()
    {
        List<ImportBoundingBox> pasteBoxes = new ArrayList<>(buffer.size());
        for (ImportBoxDescription description : buffer) pasteBoxes.add(description.createImportBox());
        List<ImportBoundingBox> oldBoxes = Keystone.getModule(ImportModule.class).restoreImportBoxes(pasteBoxes);
        restore = new ArrayList<>(oldBoxes.size());
        for (ImportBoundingBox oldBox : oldBoxes) restore.add(new ImportBoxDescription(oldBox));
    }
    @Override
    public void redo()
    {
        List<ImportBoundingBox> pasteBoxes = new ArrayList<>(restore.size());
        for (ImportBoxDescription description : restore) pasteBoxes.add(description.createImportBox());
        Keystone.getModule(ImportModule.class).restoreImportBoxes(pasteBoxes);
    }
}
