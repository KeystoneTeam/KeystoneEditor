package keystone.core.modules.history.entries;

import keystone.api.Keystone;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.coordinates.Vector3i;
import keystone.core.gui.screens.schematics.CloneScreen;
import keystone.core.modules.history.IHistoryEntry;
import keystone.core.modules.schematic_import.ImportModule;
import keystone.core.modules.schematic_import.boxes.ImportBoundingBox;
import keystone.core.renderer.common.models.Coords;
import keystone.core.schematic.KeystoneSchematic;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

import java.util.ArrayList;
import java.util.List;

public class CloneImportBoxesHistoryEntry implements IHistoryEntry
{
    private final BoundingBox bounds;
    private final KeystoneSchematic schematic;
    private final Coords anchor;
    private final Rotation rotation;
    private final Mirror mirror;
    private final Vector3i offset;
    private final int repeat;
    private final int scale;

    private Rotation restoreRotation;
    private Mirror restoreMirror;
    private Vector3i restoreOffset;
    private int restoreRepeat;
    private int restoreScale;
    private List<ImportBoxesHistoryEntry.ImportBoxDescription> buffer;

    public CloneImportBoxesHistoryEntry(BoundingBox bounds, KeystoneSchematic schematic, Coords anchor, Rotation rotation, Mirror mirror, Vector3i offset, int repeat, int scale, boolean first)
    {
        this.bounds = bounds;
        this.schematic = schematic;
        this.anchor = anchor;
        this.rotation = rotation;
        this.mirror = mirror;
        this.offset = offset;
        this.repeat = repeat;
        this.scale = scale;

        if (first)
        {
            ImportModule importModule = Keystone.getModule(ImportModule.class);
            buffer = new ArrayList<>(importModule.getImportBoxes().size());
            for (ImportBoundingBox box : importModule.getImportBoxes()) buffer.add(new ImportBoxesHistoryEntry.ImportBoxDescription(box));
        }
        else
        {
            restoreRotation = CloneScreen.getRotation();
            restoreMirror = CloneScreen.getMirror();
            restoreOffset = CloneScreen.getOffset();
            restoreRepeat = CloneScreen.getRepeat();
            restoreScale = CloneScreen.getScale();
        }
    }

    @Override
    public void undo()
    {
        if (buffer != null)
        {
            List<ImportBoundingBox> pasteBoxes = new ArrayList<>(buffer.size());
            for (ImportBoxesHistoryEntry.ImportBoxDescription description : buffer) pasteBoxes.add(description.createImportBox());
            Keystone.getModule(ImportModule.class).restoreImportBoxes(pasteBoxes);
        }
        else
        {
            Keystone.getModule(ImportModule.class).restoreCloneImportBoxes(schematic, anchor, restoreRotation, restoreMirror, restoreOffset, restoreRepeat, restoreScale);
            CloneScreen.reopen(bounds, schematic, anchor, restoreRotation, restoreMirror, restoreOffset, restoreRepeat, restoreScale);
        }
    }
    @Override
    public void redo()
    {
        Keystone.getModule(ImportModule.class).restoreCloneImportBoxes(schematic, anchor, rotation, mirror, offset, repeat, scale);
        CloneScreen.reopen(bounds, schematic, anchor, rotation, mirror, offset, repeat, scale);
    }
}
