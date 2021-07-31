package keystone.core.schematic.extensions;

import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.core.modules.blocks.BlocksModule;
import keystone.core.modules.entities.EntitiesModule;
import keystone.core.renderer.blocks.world.GhostBlocksWorld;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ISchematicExtension
{
    ISchematicExtension create(World world, BoundingBox bounds);
    ResourceLocation id();
    void serialize(CompoundNBT nbt);
    ISchematicExtension deserialize(CompoundNBT nbt);

    default void place(GhostBlocksWorld ghostWorld) {}
    default void place(BlockPos anchor, BlocksModule blocksModule, EntitiesModule entitiesModule, Rotation rotation, Mirror mirror, int scale) {}
}
