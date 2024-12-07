package keystone.core.schematic.extensions;

import keystone.api.wrappers.blocks.BlockType;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.entities.Entity;
import keystone.api.wrappers.nbt.NBTCompound;
import keystone.core.modules.world.WorldModifierModules;
import keystone.core.renderer.blocks.legacy.world.GhostBlocksWorld;
import keystone.core.schematic.KeystoneSchematic;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.Map;

public interface ISchematicExtension
{
    ISchematicExtension create(World world, BoundingBox bounds);
    Identifier id();
    void serialize(KeystoneSchematic schematic, NbtCompound nbt);
    ISchematicExtension deserialize(Vec3i size, BlockType[] blocks, Map<BlockPos, NBTCompound> tileEntities, Entity[] entities, NbtCompound nbt);

    default boolean canPlace() { return true; }
    default boolean placeByDefault() { return false; }
    default void place(KeystoneSchematic schematic, GhostBlocksWorld ghostWorld) {}
    default void place(KeystoneSchematic schematic, WorldModifierModules worldModifiers, BlockPos anchor, BlockRotation rotation, BlockMirror mirror, int scale) {}
}
