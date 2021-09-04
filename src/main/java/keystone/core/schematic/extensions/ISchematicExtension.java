package keystone.core.schematic.extensions;

import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.entities.Entity;
import keystone.core.modules.world.WorldModifierModules;
import keystone.core.renderer.blocks.world.GhostBlocksWorld;
import keystone.core.schematic.KeystoneSchematic;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;

public interface ISchematicExtension
{
    ISchematicExtension create(World world, BoundingBox bounds);
    ResourceLocation id();
    void serialize(KeystoneSchematic schematic, CompoundNBT nbt);
    ISchematicExtension deserialize(Vector3i size, Block[] blocks, Entity[] entities, CompoundNBT nbt);

    default boolean canPlace() { return true; }
    default boolean placeByDefault() { return false; }
    default void place(KeystoneSchematic schematic, GhostBlocksWorld ghostWorld) {}
    default void place(KeystoneSchematic schematic, WorldModifierModules worldModifiers, BlockPos anchor, Rotation rotation, Mirror mirror, int scale) {}
}
