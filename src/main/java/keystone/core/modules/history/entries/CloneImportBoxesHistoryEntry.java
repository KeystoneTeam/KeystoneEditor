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
import keystone.core.schematic.SchematicLoader;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.DoubleNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class CloneImportBoxesHistoryEntry implements IHistoryEntry
{
    private BoundingBox bounds;
    private KeystoneSchematic schematic;
    private Coords anchor;
    private Rotation rotation;
    private Mirror mirror;
    private Vector3i offset;
    private int repeat;
    private int scale;

    private Rotation restoreRotation;
    private Mirror restoreMirror;
    private Vector3i restoreOffset;
    private int restoreRepeat;
    private int restoreScale;
    private List<ImportBoxesHistoryEntry.ImportBoxDescription> buffer;

    public CloneImportBoxesHistoryEntry(CompoundNBT nbt)
    {
        deserialize(nbt);
    }
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

    @Override
    public String id()
    {
        return "clone_import_boxes";
    }
    @Override
    public void serialize(CompoundNBT nbt)
    {
        ListNBT boundsNBT = new ListNBT();
        boundsNBT.add(DoubleNBT.valueOf(bounds.minX));
        boundsNBT.add(DoubleNBT.valueOf(bounds.minY));
        boundsNBT.add(DoubleNBT.valueOf(bounds.minZ));
        boundsNBT.add(DoubleNBT.valueOf(bounds.maxX));
        boundsNBT.add(DoubleNBT.valueOf(bounds.maxY));
        boundsNBT.add(DoubleNBT.valueOf(bounds.maxZ));
        nbt.put("bounds", boundsNBT);

        nbt.put("schematic", SchematicLoader.serializeSchematic(schematic));

        nbt.putIntArray("orientation", new int[]
        {
                anchor.getX(), anchor.getY(), anchor.getZ(),
                offset.x, offset.y, offset.z,
                repeat, scale,
                restoreOffset.x, restoreOffset.y, restoreOffset.z,
                restoreRepeat, restoreScale
        });
        nbt.putString("rotation", rotation.name());
        nbt.putString("mirror", mirror.name());
        nbt.putString("restore_rotation", restoreRotation.name());
        nbt.putString("restore_mirror", restoreMirror.name());

        if (buffer != null)
        {
            ListNBT bufferNBT = new ListNBT();
            for (ImportBoxesHistoryEntry.ImportBoxDescription box : buffer) bufferNBT.add(box.serialize());
            nbt.put("buffer", bufferNBT);
        }
    }
    @Override
    public void deserialize(CompoundNBT nbt)
    {
        ListNBT boundsNBT = nbt.getList("bounds", Constants.NBT.TAG_DOUBLE);
        bounds = new BoundingBox(boundsNBT.getDouble(0), boundsNBT.getDouble(1), boundsNBT.getDouble(2), boundsNBT.getDouble(3), boundsNBT.getDouble(4), boundsNBT.getDouble(5));
        schematic = SchematicLoader.deserializeSchematic(nbt.getCompound("schematic"));

        int[] orientationNBT = nbt.getIntArray("orientation");
        anchor = new Coords(orientationNBT[0], orientationNBT[1], orientationNBT[2]);
        offset = new Vector3i(orientationNBT[3], orientationNBT[4], orientationNBT[5]);
        repeat = orientationNBT[6];
        scale = orientationNBT[7];
        restoreOffset = new Vector3i(orientationNBT[8], orientationNBT[9], orientationNBT[10]);
        restoreRepeat = orientationNBT[11];
        restoreScale = orientationNBT[12];
        rotation = Rotation.valueOf(nbt.getString("rotation"));
        mirror = Mirror.valueOf(nbt.getString("mirror"));
        restoreRotation = Rotation.valueOf("restore_rotation");
        restoreMirror = Mirror.valueOf("restore_mirror");

        if (nbt.contains("buffer"))
        {
            ListNBT bufferNBT = nbt.getList("buffer", Constants.NBT.TAG_COMPOUND);
            buffer = new ArrayList<>(bufferNBT.size());
            for (int i = 0; i < bufferNBT.size(); i++) buffer.add(new ImportBoxesHistoryEntry.ImportBoxDescription(bufferNBT.getCompound(i)));
        }
        else buffer = null;
    }
}
