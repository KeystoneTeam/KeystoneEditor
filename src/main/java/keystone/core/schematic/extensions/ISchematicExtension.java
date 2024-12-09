package keystone.core.schematic.extensions;

import keystone.api.wrappers.entities.Entity;
import keystone.core.modules.world.WorldModifierModules;
import keystone.core.renderer.blocks.legacy.world.GhostBlocksWorld;
import keystone.core.schematic.KeystoneSchematic;
import keystone.core.utils.PalettedArray;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.Map;

public interface ISchematicExtension
{
    ISchematicExtension create(World world, BlockBox box);
    Identifier id();
    NbtCompound serialize(KeystoneSchematic schematic);
    ISchematicExtension deserialize(Vec3i size, PalettedArray<BlockState> blocks, Map<BlockPos, NbtCompound> tileEntities, Entity[] entities, NbtCompound nbt);

    default boolean canPlace() { return true; }
    default boolean placeByDefault() { return false; }
    default void place(KeystoneSchematic schematic, GhostBlocksWorld ghostWorld) {}
    default void place(KeystoneSchematic schematic, WorldModifierModules worldModifiers, BlockPos anchor, BlockRotation rotation, BlockMirror mirror, int scale) {}
}
