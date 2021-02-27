package keystone.core.modules.clipboard.boxes;

import keystone.api.Keystone;
import keystone.core.modules.selection.SelectedFace;
import keystone.core.renderer.blocks.GhostBlockRenderer;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.common.BoundingBoxType;
import keystone.core.renderer.common.models.Coords;
import keystone.core.renderer.common.models.SelectableBoundingBox;
import keystone.core.schematic.KeystoneSchematic;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class PasteBoundingBox extends SelectableBoundingBox
{
    private KeystoneSchematic schematic;
    private GhostBlockRenderer ghostBlocks;

    private PasteBoundingBox(Coords corner1, Coords corner2, KeystoneSchematic schematic)
    {
        super(corner1, corner2, BoundingBoxType.get("paste_box"));
        this.schematic = schematic;

        this.ghostBlocks = new GhostBlockRenderer();
        schematic.forEachBlock((pos, block) -> ghostBlocks.setBlock(pos, block.getMinecraftBlock()));
    }
    public static PasteBoundingBox create(Coords minCoords, KeystoneSchematic contents)
    {
        return new PasteBoundingBox(minCoords, minCoords.add(Vector3d.copy(contents.getSize()).add(-1, -1, -1)), contents);
    }
    public PasteBoundingBox clone()
    {
        return create(getMinCoords(), this.schematic.clone());
    }

    public KeystoneSchematic getSchematic() { return schematic; }
    public GhostBlockRenderer getGhostBlocks() { return ghostBlocks; }

    @Override
    public boolean isEnabled() { return true; /* return Keystone.getModule(ClipboardModule.class).isEnabled(); */ }
    @Override
    public void drag(SelectedFace face)
    {
        Vector3d lookPoint = Player.getEyePosition().add(Player.getLookDirection().scale(face.getDistance()));
        face.getBox().move(new Coords(lookPoint).sub(face.getRelativeSelectedBlock()));
    }

    public void paste(World world)
    {
        schematic.forEachBlock((pos, block) ->
        {
            BlockPos offset = pos.add(getMinCoords().getX(), getMinCoords().getY(), getMinCoords().getZ());
            Keystone.setBlock(offset.getX(), offset.getY(), offset.getZ(), block);
        });
    }
}
