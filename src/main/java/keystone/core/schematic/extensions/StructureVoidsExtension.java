package keystone.core.schematic.extensions;

import keystone.api.Keystone;
import keystone.api.enums.RetrievalMode;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.blocks.BlockType;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.entities.Entity;
import keystone.core.math.BlockPosMath;
import keystone.core.modules.world.BlocksModule;
import keystone.core.modules.world.WorldModifierModules;
import keystone.core.registries.BlockTypeRegistry;
import keystone.core.schematic.KeystoneSchematic;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class StructureVoidsExtension implements ISchematicExtension
{
    private BlockPos[] structureVoids;

    @Override
    public StructureVoidsExtension create(World world, BoundingBox bounds)
    {
        BlocksModule blocks = Keystone.getModule(BlocksModule.class);

        List<BlockPos> structureVoidsList = new ArrayList<>();
        bounds.forEachCoordinate((x, y, z) ->
        {
            if (blocks.getBlockType(x, y, z, RetrievalMode.LAST_SWAPPED).getMinecraftBlock().isOf(Blocks.STRUCTURE_VOID))
            {
                structureVoidsList.add(BlockPos.ofFloored(x - bounds.minX, y - bounds.minY, z - bounds.minZ));
            }
        });

        if (structureVoidsList.size() > 0)
        {
            StructureVoidsExtension extension = new StructureVoidsExtension();
            extension.structureVoids = new BlockPos[structureVoidsList.size()];
            structureVoidsList.toArray(extension.structureVoids);
            return extension;
        }
        else return null;
    }

    @Override
    public Identifier id()
    {
        return new Identifier("keystone:structure_voids");
    }

    @Override
    public void serialize(KeystoneSchematic schematic, NbtCompound nbt)
    {
        NbtList list = new NbtList();
        for (BlockPos pos : structureVoids)
        {
            NbtIntArray posNBT = new NbtIntArray(new int[] { pos.getX(), pos.getY(), pos.getZ() });
            list.add(posNBT);
        }
        nbt.put("positions", list);
    }

    @Override
    public ISchematicExtension deserialize(Vec3i size, Block[] blocks, Entity[] entities, NbtCompound nbt)
    {
        List<BlockPos> structureVoidsList = new ArrayList<>();

        NbtList list = nbt.getList("positions", NbtElement.INT_ARRAY_TYPE);
        for (int i = 0; i < list.size(); i++)
        {
            int[] posNBT = list.getIntArray(i);
            structureVoidsList.add(new BlockPos(posNBT[0], posNBT[1], posNBT[2]));
        }

        StructureVoidsExtension extension = new StructureVoidsExtension();
        extension.structureVoids = new BlockPos[structureVoidsList.size()];
        structureVoidsList.toArray(extension.structureVoids);
        return extension;
    }

    @Override
    public void place(KeystoneSchematic schematic, WorldModifierModules worldModifiers, BlockPos anchor, BlockRotation rotation, BlockMirror mirror, int scale)
    {
        BlockType structureVoid = BlockTypeRegistry.fromMinecraftBlock(Blocks.STRUCTURE_VOID.getDefaultState());
        for (BlockPos pos : structureVoids)
        {
            BlockPos oriented = BlockPosMath.getOrientedBlockPos(pos, schematic.getSize(), rotation, mirror, scale).add(anchor);
            worldModifiers.blocks.setBlock(oriented.getX(), oriented.getY(), oriented.getZ(), structureVoid);
        }
    }
}
