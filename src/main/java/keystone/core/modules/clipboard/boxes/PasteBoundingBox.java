package keystone.core.modules.clipboard.boxes;

import keystone.api.Keystone;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.blocks.BlocksModule;
import keystone.core.modules.clipboard.ClipboardModule;
import keystone.core.modules.ghost_blocks.GhostBlocksModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.history.entries.PasteBoxHistoryEntry;
import keystone.core.modules.selection.SelectedFace;
import keystone.core.renderer.blocks.world.GhostBlocksWorld;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.common.BoundingBoxType;
import keystone.core.renderer.common.models.Coords;
import keystone.core.renderer.common.models.SelectableBoundingBox;
import keystone.core.schematic.KeystoneSchematic;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class PasteBoundingBox extends SelectableBoundingBox
{
    private KeystoneSchematic schematic;
    private GhostBlocksWorld ghostBlocks;

    private Rotation rotation;
    private Mirror mirror;
    private int scale;

    private PasteBoundingBox(Coords corner1, Coords corner2, KeystoneSchematic schematic, GhostBlocksWorld ghostBlocks)
    {
        super(corner1, corner2, BoundingBoxType.get("paste_box"));
        this.schematic = schematic;
        this.rotation = Rotation.NONE;
        this.mirror = Mirror.NONE;
        this.scale = 1;

        if (ghostBlocks == null) this.ghostBlocks = Keystone.getModule(GhostBlocksModule.class).createWorldFromSchematic(schematic);
        else this.ghostBlocks = ghostBlocks;
        this.ghostBlocks.getRenderer().offset = getMinCoords().toVector3d();

        refreshMinMax();
    }
    public static PasteBoundingBox create(Coords minCoords, KeystoneSchematic contents, GhostBlocksWorld ghostBlocks)
    {
        return new PasteBoundingBox(minCoords, minCoords.add(Vector3d.copy(contents.getSize()).add(-1, -1, -1)), contents, ghostBlocks);
    }
    public PasteBoundingBox clone()
    {
        return create(getMinCoords(), this.schematic.clone(), this.ghostBlocks);
    }

    public KeystoneSchematic getSchematic() { return schematic; }
    public GhostBlocksWorld getGhostBlocks() { return ghostBlocks; }
    public Rotation getRotation() { return rotation; }
    public Mirror getMirror() { return mirror; }
    public int getScale() { return scale; }

    @Override
    public Coords getMaxCoords()
    {
        Coords min = super.getMinCoords();
        Coords max = super.getMaxCoords();
        Coords delta = max.sub(min);
        if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90)
        {
            max = new Coords(min.getX() + delta.getZ(), min.getY() + delta.getY(), min.getZ() + delta.getX());
        }
        return max;
    }

    public void cycleRotate()
    {
        this.rotation = this.rotation.add(Rotation.CLOCKWISE_90);
        this.ghostBlocks.setOrientation(rotation, mirror);
    }
    public void cycleMirror()
    {
        switch (this.mirror)
        {
            case NONE:
                this.mirror = Mirror.LEFT_RIGHT;
                break;
            case LEFT_RIGHT:
                this.mirror = Mirror.FRONT_BACK;
                break;
            case FRONT_BACK:
                this.mirror = Mirror.NONE;
                break;
        }

        this.ghostBlocks.setOrientation(rotation, mirror);
    }
    public void setScale(int scale)
    {
        this.scale = scale;
    }

    @Override
    public boolean isEnabled() { return true; /* return Keystone.getModule(ClipboardModule.class).isEnabled(); */ }

    @Override
    public void startDrag(SelectedFace face)
    {
        HistoryModule historyModule = Keystone.getModule(HistoryModule.class);
        historyModule.beginHistoryEntry();
        historyModule.pushToEntry(new PasteBoxHistoryEntry(Keystone.getModule(ClipboardModule.class).getPasteBoxes()));
        historyModule.endHistoryEntry();
    }

    @Override
    public void drag(SelectedFace face)
    {
        Vector3d lookPoint = Player.getEyePosition().add(Player.getLookDirection().scale(face.getDistance()));
        face.getBox().move(new Coords(lookPoint).sub(face.getRelativeSelectedBlock()));
    }
    @Override
    public void move(Coords newMin)
    {
        ghostBlocks.getRenderer().offset = newMin.toVector3d();
        super.move(newMin);
    }

    public void paste()
    {
        BlocksModule blocksModule = Keystone.getModule(BlocksModule.class);
        schematic.place(getMinCoords().toBlockPos(), blocksModule, rotation, mirror, 1);
    }
}
