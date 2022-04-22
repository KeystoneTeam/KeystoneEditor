package keystone.core.schematic.extensions;

import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.entities.Entity;
import keystone.core.modules.world.WorldModifierModules;
import keystone.core.renderer.blocks.world.GhostBlocksWorld;
import keystone.core.schematic.KeystoneSchematic;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public interface ISchematicExtension
{
    ISchematicExtension create(World world, BoundingBox bounds);
    Identifier id();
    void serialize(KeystoneSchematic schematic, NbtCompound nbt);
    ISchematicExtension deserialize(Vec3i size, Block[] blocks, Entity[] entities, NbtCompound nbt);

    default boolean canPlace() { return true; }
    default boolean placeByDefault() { return false; }
    default void place(KeystoneSchematic schematic, GhostBlocksWorld ghostWorld) {}
    default void place(KeystoneSchematic schematic, WorldModifierModules worldModifiers, BlockPos anchor, BlockRotation rotation, BlockMirror mirror, int scale) {}
}
