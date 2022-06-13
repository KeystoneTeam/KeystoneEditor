package keystone.core.modules.history.entries;

import keystone.api.Keystone;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.coordinates.Vector3i;
import keystone.core.gui.overlays.schematics.CloneScreen;
import keystone.core.modules.history.IHistoryEntry;
import keystone.core.modules.schematic_import.ImportModule;
import keystone.core.schematic.KeystoneSchematic;
import keystone.core.schematic.SchematicLoader;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;

import java.util.HashMap;
import java.util.Map;

public class CloneScreenHistoryEntry implements IHistoryEntry
{
    private BoundingBox boundingBox;
    private KeystoneSchematic schematic;
    private Vec3i anchor;
    private BlockRotation rotation;
    private BlockMirror mirror;
    private Vector3i offset = new Vector3i(0, 0, 0);
    private int repeat = 1;
    private int scale = 1;
    private Map<Identifier, Boolean> extensionsToPlace;
    private boolean copyAir;
    private boolean closeScreen;

    public CloneScreenHistoryEntry(NbtCompound nbt) { deserialize(nbt); }
    public CloneScreenHistoryEntry(boolean closeScreen)
    {
        this.closeScreen = closeScreen;
    }
    public CloneScreenHistoryEntry(BoundingBox boundingBox, KeystoneSchematic schematic, Vec3i anchor, BlockRotation rotation, BlockMirror mirror, Vector3i offset, int repeat, int scale, Map<Identifier, Boolean> extensionsToPlace, boolean copyAir)
    {
        this.boundingBox = boundingBox;
        this.schematic = schematic;
        this.anchor = anchor;
        this.rotation = rotation;
        this.mirror = mirror;
        this.offset = offset;
        this.repeat = repeat;
        this.scale = scale;
        this.extensionsToPlace = new HashMap<>(extensionsToPlace.size());
        this.extensionsToPlace.putAll(extensionsToPlace);
        this.copyAir = copyAir;
    }

    @Override
    public void apply()
    {
        if (closeScreen)
        {
            CloneScreen.closeInstance();
            Keystone.getModule(ImportModule.class).clearImportBoxes(true, false);
        }
        else CloneScreen.restoreValues(boundingBox, schematic, anchor, rotation, mirror, offset, repeat, scale, extensionsToPlace, copyAir);
    }

    @Override
    public String id()
    {
        return "clone_screen";
    }

    @Override
    public void serialize(NbtCompound nbt)
    {
        if (closeScreen)
        {
            nbt.putBoolean("closeScreen", true);
        }
        else
        {
            nbt.putIntArray("boundingBox", new int[] { (int)boundingBox.minX, (int)boundingBox.minY, (int)boundingBox.minZ, (int)boundingBox.maxX, (int)boundingBox.maxY, (int)boundingBox.maxZ });
            nbt.put("schematic", SchematicLoader.serializeSchematic(schematic));
            nbt.putIntArray("anchor", new int[] { anchor.getX(), anchor.getY(), anchor.getZ() });
            nbt.putString("rotation", rotation.name());
            nbt.putString("mirror", mirror.name());
            nbt.putIntArray("offset", new int[] { offset.x, offset.y, offset.z });
            nbt.putInt("repeat", repeat);
            nbt.putInt("scale", scale);
            nbt.putBoolean("copyAir", copyAir);

            NbtCompound extensionsNBT = new NbtCompound();
            for (Map.Entry<Identifier, Boolean> entry : extensionsToPlace.entrySet()) extensionsNBT.putBoolean(entry.getKey().toString(), entry.getValue());
            nbt.put("extensions", extensionsNBT);
        }
    }

    @Override
    public void deserialize(NbtCompound nbt)
    {
        if (nbt.contains("closeScreen"))
        {
            this.closeScreen = true;
        }
        else
        {
            int[] boundingBox = nbt.getIntArray("boundingBox");
            this.boundingBox = new BoundingBox(boundingBox[0], boundingBox[1], boundingBox[2], boundingBox[3], boundingBox[4], boundingBox[5]);
            this.schematic = SchematicLoader.deserializeSchematic(nbt.getCompound("schematic"));
            int[] anchor = nbt.getIntArray("anchor");
            this.anchor = new Vec3i(anchor[0], anchor[1], anchor[2]);
            this.rotation = BlockRotation.valueOf(nbt.getString("rotation"));
            this.mirror = BlockMirror.valueOf(nbt.getString("mirror"));
            int[] offset =  nbt.getIntArray("offset");
            this.offset = new Vector3i(offset[0], offset[1], offset[2]);
            this.repeat = nbt.getInt("repeat");
            this.scale = nbt.getInt("scale");
            this.copyAir = nbt.getBoolean("copyAir");

            NbtCompound extensionsNBT = nbt.getCompound("extensions");
            this.extensionsToPlace = new HashMap<>(extensionsNBT.getKeys().size());
            for (String key : extensionsNBT.getKeys()) extensionsToPlace.put(new Identifier(key), extensionsNBT.getBoolean(key));
        }
    }
}
