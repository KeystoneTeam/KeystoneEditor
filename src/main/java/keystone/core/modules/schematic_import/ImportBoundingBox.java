package keystone.core.modules.schematic_import;

import keystone.api.Keystone;
import keystone.core.client.Player;
import keystone.core.math.RayTracing;
import keystone.core.modules.ghost_blocks.GhostBlocksModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.history.IHistoryEntry;
import keystone.core.modules.selection.SelectedFace;
import keystone.core.modules.world.WorldModifierModules;
import keystone.core.renderer.blocks.world.GhostBlocksWorld;
import keystone.core.renderer.shapes.SelectableBoundingBox;
import keystone.core.schematic.KeystoneSchematic;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.Map;

public class ImportBoundingBox extends SelectableBoundingBox
{
    private GhostBlocksModule ghostBlocksModule;
    private KeystoneSchematic schematic;
    private GhostBlocksWorld ghostBlocks;

    private BlockRotation rotation;
    private BlockMirror mirror;
    private int scale;

    private Vec3i dragOffset;
    private int dragLockX;
    private int dragLockY;
    private int dragLockZ;

    private ImportBoundingBox(Vec3i corner1, Vec3i corner2, KeystoneSchematic schematic, BlockRotation rotation, BlockMirror mirror, int scale)
    {
        super(corner1, corner2);
        this.schematic = schematic;
        this.rotation = rotation;
        this.mirror = mirror;
        this.scale = scale;

        this.ghostBlocksModule = Keystone.getModule(GhostBlocksModule.class);
        this.ghostBlocks = ghostBlocksModule.createWorldFromSchematic(schematic, rotation, mirror, scale);
        this.ghostBlocks.getRenderer().offset = Vec3d.of(getMin());

        refreshMinMax();
        updateBounds();
    }
    public static ImportBoundingBox create(Vec3i min, KeystoneSchematic contents, BlockRotation rotation, BlockMirror mirror, int scale)
    {
        return new ImportBoundingBox(min, min.add(contents.getSize().add(-1, -1, -1)), contents, rotation, mirror, scale);
    }
    public static ImportBoundingBox create(Vec3i minCoords, KeystoneSchematic contents)
    {
        return create(minCoords, contents, BlockRotation.NONE, BlockMirror.NONE, 1);
    }

    public KeystoneSchematic getSchematic() { return schematic; }
    public GhostBlocksWorld getGhostBlocks() { return ghostBlocks; }
    public BlockRotation getRotation() { return rotation; }
    public BlockMirror getMirror() { return mirror; }
    public int getScale() { return scale; }

    public void cycleRotate()
    {
        setRotation(this.rotation.rotate(BlockRotation.CLOCKWISE_90));
    }
    public void cycleMirror()
    {
        switch (this.mirror)
        {
            case NONE:
                setMirror(BlockMirror.LEFT_RIGHT);
                return;
            case LEFT_RIGHT:
                setMirror(BlockMirror.FRONT_BACK);
                return;
            case FRONT_BACK:
                setMirror(BlockMirror.NONE);
                return;
        }
    }
    public void setRotation(BlockRotation rotation)
    {
        this.rotation = rotation;
        this.ghostBlocks.setOrientation(rotation, mirror);
        updateBounds();
    }
    public void setMirror(BlockMirror mirror)
    {
        this.mirror = mirror;
        this.ghostBlocks.setOrientation(rotation, mirror);
        updateBounds();
    }
    public void setOrientation(BlockRotation rotation, BlockMirror mirror)
    {
        this.rotation = rotation;
        this.mirror = mirror;
        this.ghostBlocks.setOrientation(rotation, mirror);
        updateBounds();
    }
    public void setScale(int scale)
    {
        scale = Math.max(1, scale);
        this.scale = scale;
        ghostBlocksModule.updateWorldFromSchematic(ghostBlocks, schematic, scale);
        updateBounds();
    }

    @Override
    public boolean isEnabled() { return true; }

    @Override
    public void startDrag(SelectedFace face)
    {
        dragOffset = face.getRelativeSelectedBlock();
        dragLockX = Integer.MAX_VALUE;
        dragLockY = Integer.MAX_VALUE;
        dragLockZ = Integer.MAX_VALUE;

        switch (face.getFaceDirection().getAxis())
        {
            case X: dragLockX = face.getBox().getMin().getX(); break;
            case Y: dragLockY = face.getBox().getMin().getY(); break;
            case Z: dragLockZ = face.getBox().getMin().getZ(); break;
        }

        HistoryModule historyModule = Keystone.getModule(HistoryModule.class);
        ImportModule importModule = Keystone.getModule(ImportModule.class);
        IHistoryEntry historyEntry = importModule.makeHistoryEntry();
        if (historyEntry != null)
        {
            historyModule.tryBeginHistoryEntry();
            historyModule.pushToEntry(historyEntry);
            historyModule.tryEndHistoryEntry();
        }
    }
    @Override
    public void drag(SelectedFace face)
    {
        Vec3d pointOnPlane = RayTracing.rayPlaneIntersection(Player.getEyePosition(), Player.getLookDirection(), face.getBox().getMin(), face.getBox().getMax(), face.getFaceDirection());
        if (pointOnPlane == null) return;

        double x = pointOnPlane.x;
        double y = pointOnPlane.y;
        double z = pointOnPlane.z;
        Vec3i size = face.getBox().getSize();

        switch (face.getFaceDirection())
        {
            case EAST:
                x -= size.getX() - 1;
            case WEST:
                y -= dragOffset.getY();
                z -= dragOffset.getZ();
                break;
            case UP:
                y -= size.getY() - 1;
            case DOWN:
                x -= dragOffset.getX();
                z -= dragOffset.getZ();
                break;
            case SOUTH:
                z -= size.getZ() - 1;
            case NORTH:
                x -= dragOffset.getX();
                y -= dragOffset.getY();
                break;
        }

        if (dragLockX != Integer.MAX_VALUE) x = dragLockX;
        if (dragLockY != Integer.MAX_VALUE) y = dragLockY;
        if (dragLockZ != Integer.MAX_VALUE) z = dragLockZ;
        face.getBox().move(new Vec3i(x, y, z));
    }

    @Override
    public void nudgeBox(Direction direction, int amount)
    {
        if (amount < 0) amount = getAxisSize(direction.getAxis());

        super.nudgeBox(direction, amount);
        ghostBlocks.getRenderer().offset = Vec3d.of(getMin());
    }
    @Override
    public void move(Vec3i newMin)
    {
        ghostBlocks.getRenderer().offset = Vec3d.of(newMin);
        super.move(newMin);
    }

    public void place(Map<Identifier, Boolean> extensionsToPlace, boolean copyAir)
    {
        schematic.place(new WorldModifierModules(), new BlockPos(getMin()), rotation, mirror, scale, extensionsToPlace, copyAir);
    }

    private void updateBounds()
    {
        Vec3i min = getMin();
        Vec3i size = schematic.getSize();
        if (rotation == BlockRotation.CLOCKWISE_90 || rotation == BlockRotation.COUNTERCLOCKWISE_90)
        {
            setMax(new Vec3i(min.getX() + size.getZ() * scale - 1, min.getY() + size.getY() * scale - 1, min.getZ() + size.getX() * scale - 1));
        }
        else
        {
            setMax(new Vec3i(min.getX() + size.getX() * scale - 1, min.getY() + size.getY() * scale - 1, min.getZ() + size.getZ() * scale - 1));
        }
    }
}
