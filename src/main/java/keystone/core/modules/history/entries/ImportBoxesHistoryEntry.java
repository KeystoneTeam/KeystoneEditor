package keystone.core.modules.history.entries;

import keystone.api.Keystone;
import keystone.core.modules.history.IHistoryEntry;
import keystone.core.modules.schematic_import.ImportBoundingBox;
import keystone.core.modules.schematic_import.ImportModule;
import keystone.core.schematic.KeystoneSchematic;
import keystone.core.schematic.SchematicLoader;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.List;

public class ImportBoxesHistoryEntry implements IHistoryEntry
{
    public static class ImportBoxDescription
    {
        public final Vec3i minCoords;
        public final KeystoneSchematic schematic;
        public final BlockRotation rotation;
        public final BlockMirror mirror;
        public final int scale;

        public ImportBoxDescription(ImportBoundingBox box)
        {
            this.minCoords = box.getCorner1();
            this.schematic = box.getSchematic();
            this.rotation = box.getRotation();
            this.mirror = box.getMirror();
            this.scale = box.getScale();
        }
        public ImportBoxDescription(NbtCompound nbt)
        {
            int[] anchorNBT = nbt.getIntArray("anchor");
            minCoords = new Vec3i(anchorNBT[0], anchorNBT[1], anchorNBT[2]);
            schematic = SchematicLoader.deserializeSchematic(nbt.getCompound("schematic"));
            rotation = BlockRotation.valueOf(nbt.getString("rotation"));
            mirror = BlockMirror.valueOf(nbt.getString("mirror"));
            scale = nbt.getInt("scale");
        }

        public ImportBoundingBox createImportBox()
        {
            ImportBoundingBox box = ImportBoundingBox.create(this.minCoords, this.schematic);
            box.setOrientation(rotation, mirror);
            box.setScale(scale);
            return box;
        }

        public NbtCompound serialize()
        {
            NbtCompound nbt = new NbtCompound();
            nbt.putIntArray("anchor", new int[] { minCoords.getX(), minCoords.getY(), minCoords.getZ() });
            nbt.put("schematic", SchematicLoader.serializeSchematic(schematic));
            nbt.putString("rotation", rotation.name());
            nbt.putString("mirror", mirror.name());
            nbt.putInt("scale", scale);
            return nbt;
        }
    }

    private List<ImportBoxDescription> boxes;

    public ImportBoxesHistoryEntry(NbtCompound nbt)
    {
        deserialize(nbt);
    }
    public ImportBoxesHistoryEntry(List<ImportBoundingBox> importBoxes)
    {
        boxes = new ArrayList<>(importBoxes.size());
        for (int i = 0; i < importBoxes.size(); i++) boxes.add(new ImportBoxDescription(importBoxes.get(i)));
    }

    @Override
    public void apply()
    {
        List<ImportBoundingBox> pasteBoxes = new ArrayList<>(boxes.size());
        for (ImportBoxDescription description : boxes) pasteBoxes.add(description.createImportBox());
        Keystone.getModule(ImportModule.class).setImportBoxes(pasteBoxes);
    }
    @Override
    public String id()
    {
        return "import_boxes";
    }
    @Override
    public void serialize(NbtCompound nbt)
    {
        NbtList bufferNBT = new NbtList();
        for (ImportBoxDescription box : boxes) bufferNBT.add(box.serialize());
        nbt.put("boxes", bufferNBT);
    }
    @Override
    public void deserialize(NbtCompound nbt)
    {
        NbtList bufferNBT = nbt.getList("boxes", NbtElement.COMPOUND_TYPE);
        boxes = new ArrayList<>(bufferNBT.size());
        for (int i = 0; i < bufferNBT.size(); i++) boxes.add(new ImportBoxDescription(bufferNBT.getCompound(i)));
    }
}
