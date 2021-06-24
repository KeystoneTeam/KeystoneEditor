package keystone.core.modules.schematic_import.boxes;

import keystone.api.Keystone;
import keystone.core.math.RayTracing;
import keystone.core.modules.blocks.BlocksModule;
import keystone.core.modules.ghost_blocks.GhostBlocksModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.history.entries.ImportBoxesHistoryEntry;
import keystone.core.modules.schematic_import.ImportModule;
import keystone.core.modules.selection.SelectedFace;
import keystone.core.renderer.blocks.world.GhostBlocksWorld;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.common.BoundingBoxType;
import keystone.core.renderer.common.models.Coords;
import keystone.core.renderer.common.models.SelectableBoundingBox;
import keystone.core.schematic.KeystoneSchematic;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

public class ImportBoundingBox extends SelectableBoundingBox
{
    private KeystoneSchematic schematic;
    private GhostBlocksWorld ghostBlocks;

    private Rotation rotation;
    private Mirror mirror;
    private int scale;

    private Vector3d dragOffset;

    private ImportBoundingBox(Coords corner1, Coords corner2, KeystoneSchematic schematic)
    {
        super(corner1, corner2, BoundingBoxType.get("paste_box"));
        this.schematic = schematic;
        this.rotation = Rotation.NONE;
        this.mirror = Mirror.NONE;
        this.scale = 1;

        this.ghostBlocks = Keystone.getModule(GhostBlocksModule.class).createWorldFromSchematic(schematic);
        this.ghostBlocks.getRenderer().offset = getMinCoords().toVector3d();

        refreshMinMax();
    }
    public static ImportBoundingBox create(Coords minCoords, KeystoneSchematic contents)
    {
        return new ImportBoundingBox(minCoords, minCoords.add(Vector3d.atLowerCornerOf(contents.getSize()).add(-1, -1, -1)), contents);
    }

    public KeystoneSchematic getSchematic() { return schematic; }
    public GhostBlocksWorld getGhostBlocks() { return ghostBlocks; }
    public Rotation getRotation() { return rotation; }
    public Mirror getMirror() { return mirror; }
    public int getScale() { return scale; }

    public void cycleRotate()
    {
        setRotation(this.rotation.getRotated(Rotation.CLOCKWISE_90));
    }
    public void cycleMirror()
    {
        switch (this.mirror)
        {
            case NONE:
                setMirror(Mirror.LEFT_RIGHT);
                return;
            case LEFT_RIGHT:
                setMirror(Mirror.FRONT_BACK);
                return;
            case FRONT_BACK:
                setMirror(Mirror.NONE);
                return;
        }
    }
    public void setRotation(Rotation rotation)
    {
        this.rotation = rotation;
        this.ghostBlocks.setOrientation(rotation, mirror);
        updateBounds();
    }
    public void setMirror(Mirror mirror)
    {
        this.mirror = mirror;
        this.ghostBlocks.setOrientation(rotation, mirror);
        updateBounds();
    }
    public void setOrientation(Rotation rotation, Mirror mirror)
    {
        this.rotation = rotation;
        this.mirror = mirror;
        this.ghostBlocks.setOrientation(rotation, mirror);
        updateBounds();
    }
    public void setScale(int scale)
    {
        this.scale = scale;
        updateBounds();
    }

    @Override
    public boolean isEnabled() { return true; /* return Keystone.getModule(ClipboardModule.class).isEnabled(); */ }

    @Override
    public void startDrag(SelectedFace face)
    {
        Vector3d pointOnPlane = RayTracing.rayPlaneIntersection(Player.getEyePosition(), Player.getLookDirection(), face.getBox().getMinCoords().toVector3d(), face.getFaceDirection());
        Vector3d relative = face.getRelativeSelectedBlock().toVector3d().add(getMinCoords().toVector3d());
        dragOffset = pointOnPlane.subtract(relative);

        HistoryModule historyModule = Keystone.getModule(HistoryModule.class);
        historyModule.beginHistoryEntry();
        historyModule.pushToEntry(new ImportBoxesHistoryEntry(Keystone.getModule(ImportModule.class).getImportBoxes()));
        historyModule.endHistoryEntry();
    }
    @Override
    public void drag(SelectedFace face)
    {
        Vector3d pointOnPlane = RayTracing.rayPlaneIntersection(Player.getEyePosition(), Player.getLookDirection(), face.getBox().getMinCoords().toVector3d(), face.getFaceDirection());
        Vector3d relative = dragOffset;
        Vector3d offset = Vector3d.ZERO;
        switch (face.getFaceDirection())
        {
            case EAST:
            case WEST:
                offset = new Vector3d(0, relative.y, relative.z);
                break;
            case NORTH:
            case SOUTH:
                offset = new Vector3d(relative.x, relative.y, 0);
                break;
            case UP:
            case DOWN:
                offset = new Vector3d(relative.x, 0, relative.z);
                break;
        }
        face.getBox().move(new Coords(pointOnPlane).sub(offset));
    }
    @Override
    public void move(Coords newMin)
    {
        ghostBlocks.getRenderer().offset = newMin.toVector3d();
        super.move(newMin);
    }

    public void place()
    {
        BlocksModule blocksModule = Keystone.getModule(BlocksModule.class);
        schematic.place(getMinCoords().toBlockPos(), blocksModule, rotation, mirror, 1);
    }

    private void updateBounds()
    {
        Coords min = getMinCoords();
        Vector3i size = schematic.getSize();
        if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90)
        {
            setMaxCoords(new Coords(min.getX() + size.getZ() * scale - 1, min.getY() + size.getY() * scale - 1, min.getZ() + size.getX() * scale - 1));
        }
        else
        {
            setMaxCoords(new Coords(min.getX() + size.getX() * scale - 1, min.getY() + size.getY() * scale - 1, min.getZ() + size.getZ() * scale - 1));
        }
    }
}
