package keystone.core.schematic.formats;

import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.entities.Entity;
import keystone.core.schematic.KeystoneSchematic;
import keystone.core.utils.NBTSerializer;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.*;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.datafix.DefaultTypeReferences;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.common.util.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KeystoneSchematicFormat implements ISchematicFormat
{
    private static final String[] EXTENSIONS = new String[] { "nbt", "kschem" };

    @Override
    public String[] getFileExtensions()
    {
        return EXTENSIONS;
    }

    //region Saving
    public static CompoundNBT saveSchematic(KeystoneSchematic schematic)
    {
        CompoundNBT nbt = new CompoundNBT();

        // Size
        ListNBT sizeNBT = new ListNBT();
        sizeNBT.add(0, IntNBT.valueOf(schematic.getSize().getX()));
        sizeNBT.add(1, IntNBT.valueOf(schematic.getSize().getY()));
        sizeNBT.add(2, IntNBT.valueOf(schematic.getSize().getZ()));
        nbt.put("size", sizeNBT);

        // Palette
        List<BlockState> palette = generatePalette(schematic);
        ListNBT paletteNBT = new ListNBT();
        for (int i = 0; i < palette.size(); i++)
        {
            BlockState entry = palette.get(i);
            CompoundNBT entryNBT = NBTUtil.writeBlockState(entry);
            paletteNBT.add(i, entryNBT);
        }
        nbt.put("palette", paletteNBT);

        // Blocks
        {
            ListNBT blocksNBT = new ListNBT();
            int i = 0;
            schematic.forEachBlock((pos, block) ->
            {
                CompoundNBT blockNBT = new CompoundNBT();

                ListNBT positionNBT = new ListNBT();
                positionNBT.add(0, IntNBT.valueOf(pos.getX()));
                positionNBT.add(1, IntNBT.valueOf(pos.getY()));
                positionNBT.add(2, IntNBT.valueOf(pos.getZ()));

                blockNBT.put("pos", positionNBT);
                blockNBT.putInt("state", palette.indexOf(block.getMinecraftBlock()));

                CompoundNBT tileEntityNBT = block.getTileEntityData();
                if (tileEntityNBT != null && !tileEntityNBT.isEmpty())
                {
                    tileEntityNBT.remove("x");
                    tileEntityNBT.remove("y");
                    tileEntityNBT.remove("z");
                    blockNBT.put("nbt", tileEntityNBT);
                }

                blocksNBT.add(index(schematic.getSize(), new int[]{pos.getX(), pos.getY(), pos.getZ()}), blockNBT);
            });
            nbt.put("blocks", blocksNBT);
        }

        // Entities
        if (schematic.getEntityCount() > 0)
        {
            ListNBT entitiesNBT = new ListNBT();
            int i = 0;
            schematic.forEachEntity(entity ->
            {
                CompoundNBT entityNBT = new CompoundNBT();
                ListNBT positionNBT = new ListNBT();
                ListNBT blockPositionNBT = new ListNBT();
                positionNBT.add(DoubleNBT.valueOf(entity.x()));
                positionNBT.add(DoubleNBT.valueOf(entity.y()));
                positionNBT.add(DoubleNBT.valueOf(entity.z()));
                blockPositionNBT.add(IntNBT.valueOf((int)entity.x()));
                blockPositionNBT.add(IntNBT.valueOf((int)entity.y()));
                blockPositionNBT.add(IntNBT.valueOf((int)entity.z()));

                entityNBT.put("pos", positionNBT);
                entityNBT.put("blockPos", blockPositionNBT);
                entityNBT.put("nbt", entity.getMinecraftEntityData());
                entitiesNBT.add(entityNBT);
            });
            nbt.put("entities", entitiesNBT);
        }

        nbt.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        return nbt;
    }
    private static List<BlockState> generatePalette(KeystoneSchematic schematic)
    {
        List<BlockState> palette = new ArrayList<>();
        schematic.forEachBlock((pos, block) ->
        {
            BlockState paletteEntry = block.getMinecraftBlock();
            if (!palette.contains(paletteEntry)) palette.add(paletteEntry);
        });
        palette.sort(Comparator.comparing(BlockState::toString));
        return palette;
    }
    //endregion
    //region Loading
    @Override
    public KeystoneSchematic loadFile(File file)
    {
        CompoundNBT nbt = NBTSerializer.deserialize(file);
        if (nbt.isEmpty()) return null;
        nbt = NBTUtil.update(Minecraft.getInstance().getFixerUpper(), DefaultTypeReferences.STRUCTURE, nbt, nbt.getInt("DataVersion"));

        // Size
        ListNBT sizeNBT = nbt.getList("size", Constants.NBT.TAG_INT);
        Vector3i size = new Vector3i(sizeNBT.getInt(0), sizeNBT.getInt(1), sizeNBT.getInt(2));

        // Palette and Blocks
        Block[] palette = loadPalette(nbt.getList("palette", Constants.NBT.TAG_COMPOUND));
        ListNBT blocksNBT = nbt.getList("blocks", Constants.NBT.TAG_COMPOUND);
        Block[] blocks = new Block[blocksNBT.size()];
        loadBlocks(size, blocks, blocksNBT, palette);

        // Entities
        ListNBT entitiesNBT = nbt.getList("entities", Constants.NBT.TAG_COMPOUND);
        Entity[] entities = new Entity[entitiesNBT.size()];
        loadEntities(entities, entitiesNBT);

        return new KeystoneSchematic(size, blocks, entities);
    }

    private Block[] loadPalette(ListNBT paletteNBT)
    {
        Block[] palette = new Block[paletteNBT.size()];
        for (int i = 0; i < palette.length; i++)
        {
            CompoundNBT entry = paletteNBT.getCompound(i);
            BlockState blockState = NBTUtil.readBlockState(entry);
            palette[i] = new Block(blockState);
        }
        return palette;
    }
    private void loadBlocks(Vector3i size, Block[] blocks, ListNBT blocksNBT, Block[] palette)
    {
        for (int i = 0; i < blocksNBT.size(); i++)
        {
            CompoundNBT blockNBT = blocksNBT.getCompound(i);
            Block block = palette[blockNBT.getInt("state")].clone();
            if (blockNBT.contains("nbt")) block.setTileEntity(blockNBT.getCompound("nbt"));

            ListNBT posNBT = blockNBT.getList("pos", Constants.NBT.TAG_INT);
            blocks[index(size, new int[] { posNBT.getInt(0), posNBT.getInt(1), posNBT.getInt(2) })] = block;
        }
    }
    private void loadEntities(Entity[] entities, ListNBT entitiesNBT)
    {
        for (int i = 0; i < entitiesNBT.size(); i++)
        {
            CompoundNBT entityNBT = entitiesNBT.getCompound(i);
            ListNBT posNBT = entityNBT.getList("pos", Constants.NBT.TAG_DOUBLE);
            CompoundNBT nbt = entityNBT.getCompound("nbt");
            entities[i] = new Entity(nbt, false).position(posNBT.getDouble(0), posNBT.getDouble(1), posNBT.getDouble(2));
        }
    }

    private static int index(Vector3i size, int[] pos)
    {
        return pos[2] + pos[1] * size.getZ() + pos[0] * size.getZ() * size.getY();
    }
    //endregion
}
