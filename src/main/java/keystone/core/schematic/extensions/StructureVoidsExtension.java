package keystone.core.schematic.extensions;

import keystone.api.wrappers.entities.Entity;
import keystone.core.math.BlockPosMath;
import keystone.core.modules.world.WorldModifierModules;
import keystone.core.schematic.KeystoneSchematic;
import keystone.core.utils.PalettedArray;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class StructureVoidsExtension implements ISchematicExtension
{
    @Override
    public StructureVoidsExtension create(World world, BlockBox bounds)
    {
        AtomicBoolean hasStructureVoids = new AtomicBoolean(false);
        bounds.forEachVertex(pos -> { if (world.getBlockState(pos).getBlock().equals(Blocks.STRUCTURE_VOID)) hasStructureVoids.set(true); });
        return hasStructureVoids.get() ? new StructureVoidsExtension() : null;
    }

    @Override
    public Identifier id()
    {
        return Identifier.of("keystone:structure_voids");
    }

    @Override
    public NbtCompound serialize(KeystoneSchematic schematic)
    {
        return null;
    }

    @Override
    public ISchematicExtension deserialize(Vec3i size, PalettedArray<BlockState> blocks, Map<BlockPos, NbtCompound> tileEntities, Entity[] entities, NbtCompound nbt)
    {
        for (BlockState block : blocks) if (block.getBlock().equals(Blocks.STRUCTURE_VOID)) return new StructureVoidsExtension();
        return null;
    }

    @Override
    public void place(KeystoneSchematic schematic, WorldModifierModules worldModifiers, BlockPos anchor, BlockRotation rotation, BlockMirror mirror, int scale)
    {
        schematic.forEachBlock((pos, state, nbt) ->
        {
            if (state.getBlock().equals(Blocks.STRUCTURE_VOID))
            {
                BlockPos oriented = BlockPosMath.getOrientedBlockPos(pos, schematic.getSize(), rotation, mirror, scale).add(anchor);
                worldModifiers.blocks.setBlockType(oriented.getX(), oriented.getY(), oriented.getZ(), state);
            }
        });
    }
}
