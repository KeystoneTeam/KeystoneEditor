package keystone.core.schematic.extensions;

import keystone.api.Keystone;
import keystone.api.enums.RetrievalMode;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.entities.Entity;
import keystone.core.math.BlockPosMath;
import keystone.core.modules.WorldModifierModules;
import keystone.core.modules.blocks.BlocksModule;
import keystone.core.schematic.KeystoneSchematic;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

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
            if (blocks.getBlock(x, y, z, RetrievalMode.LAST_SWAPPED).getMinecraftBlock().is(Blocks.STRUCTURE_VOID)) structureVoidsList.add(new BlockPos(x, y, z));
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
    public ResourceLocation id()
    {
        return new ResourceLocation("keystone:structure_voids");
    }

    @Override
    public void serialize(KeystoneSchematic schematic, CompoundNBT nbt)
    {
        ListNBT list = new ListNBT();
        for (BlockPos pos : structureVoids)
        {
            ListNBT posNBT = new ListNBT();
            posNBT.add(IntNBT.valueOf(pos.getX()));
            posNBT.add(IntNBT.valueOf(pos.getY()));
            posNBT.add(IntNBT.valueOf(pos.getZ()));
            list.add(posNBT);
        }
        nbt.put("positions", list);
    }

    @Override
    public ISchematicExtension deserialize(Vector3i size, Block[] blocks, Entity[] entities, CompoundNBT nbt)
    {
        List<BlockPos> structureVoidsList = new ArrayList<>();

        ListNBT list = nbt.getList("positions", Constants.NBT.TAG_LIST);
        for (int i = 0; i < list.size(); i++)
        {
            ListNBT posNBT = list.getList(i);
            structureVoidsList.add(new BlockPos(posNBT.getInt(0), posNBT.getInt(1), posNBT.getInt(2)));
        }

        StructureVoidsExtension extension = new StructureVoidsExtension();
        extension.structureVoids = new BlockPos[structureVoidsList.size()];
        structureVoidsList.toArray(extension.structureVoids);
        return extension;
    }

    @Override
    public void place(KeystoneSchematic schematic, WorldModifierModules worldModifiers, BlockPos anchor, Rotation rotation, Mirror mirror, int scale)
    {
        Block structureVoid = new Block(Blocks.STRUCTURE_VOID.defaultBlockState());
        for (BlockPos pos : structureVoids)
        {
            BlockPos oriented = BlockPosMath.getOrientedBlockPos(pos, schematic.getSize(), rotation, mirror, scale);
            worldModifiers.blocks.setBlock(oriented.getX(), oriented.getY(), oriented.getZ(), structureVoid);
        }
    }
}
