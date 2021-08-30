package keystone.core.schematic.formats;

import keystone.api.Keystone;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.blocks.BlockType;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.entities.Entity;
import keystone.api.wrappers.nbt.NBTCompound;
import keystone.core.registries.BlockTypeRegistry;
import keystone.core.schematic.KeystoneSchematic;
import keystone.core.schematic.extensions.ISchematicExtension;
import keystone.core.utils.NBTSerializer;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.datafix.DefaultTypeReferences;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.io.File;
import java.util.*;

public class KeystoneSchematicFormat implements ISchematicFormat
{
    private static final String[] FILE_EXTENSIONS = new String[] { "nbt", "kschem" };
    private static Map<ResourceLocation, ISchematicExtension> dataExtensions = new HashMap<>();

    @Override
    public String[] getFileExtensions()
    {
        return FILE_EXTENSIONS;
    }

    public static void registerExtension(ISchematicExtension extension)
    {
        if (dataExtensions.containsKey(extension.id()))
        {
            Keystone.LOGGER.error("Trying to register Schematic Extension under already used id '" + extension.id().toString() + "'!");
            return;
        }
        dataExtensions.put(extension.id(), extension);
    }
    public static Map<ResourceLocation, ISchematicExtension> createExtensions(World world, BoundingBox bounds)
    {
        Map<ResourceLocation, ISchematicExtension> ret = new HashMap<>();
        for (Map.Entry<ResourceLocation, ISchematicExtension> entry : dataExtensions.entrySet())
        {
            ISchematicExtension extension = entry.getValue().create(world, bounds);
            if (extension != null) ret.put(entry.getKey(), extension);
        }
        return Collections.unmodifiableMap(ret);
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
            schematic.forEachBlock((pos, block) ->
            {
                CompoundNBT blockNBT = new CompoundNBT();

                ListNBT positionNBT = new ListNBT();
                positionNBT.add(0, IntNBT.valueOf(pos.getX()));
                positionNBT.add(1, IntNBT.valueOf(pos.getY()));
                positionNBT.add(2, IntNBT.valueOf(pos.getZ()));

                blockNBT.put("pos", positionNBT);
                blockNBT.putInt("state", palette.indexOf(block.blockType().getMinecraftBlock()));

                NBTCompound tileEntityNBT = block.tileEntity();
                if (tileEntityNBT != null)
                {
                    tileEntityNBT.remove("x");
                    tileEntityNBT.remove("y");
                    tileEntityNBT.remove("z");
                    blockNBT.put("nbt", tileEntityNBT.getMinecraftNBT());
                }

                blocksNBT.add(blockNBT);
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

        // Extensions
        List<ResourceLocation> ids = new ArrayList<>(dataExtensions.keySet());
        ids.sort(ResourceLocation::compareNamespaced);
        CompoundNBT extensionsNBT = new CompoundNBT();
        for (ResourceLocation id : ids)
        {
            ISchematicExtension extension = schematic.getExtension(id);
            if (extension == null) continue;
            CompoundNBT namespaceNBT = extensionsNBT.contains(id.getNamespace(), Constants.NBT.TAG_COMPOUND) ? extensionsNBT.getCompound(id.getNamespace()) : new CompoundNBT();

            CompoundNBT extensionNBT = new CompoundNBT();
            extension.serialize(schematic, extensionNBT);
            namespaceNBT.put(id.getPath(), extensionNBT);
            extensionsNBT.put(id.getNamespace(), namespaceNBT);
        }
        nbt.put("extensions", extensionsNBT);

        nbt.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        return nbt;
    }
    private static List<BlockState> generatePalette(KeystoneSchematic schematic)
    {
        List<BlockState> palette = new ArrayList<>();
        schematic.forEachBlock((pos, block) ->
        {
            BlockState paletteEntry = block.blockType().getMinecraftBlock();
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
        return deserialize(NBTSerializer.deserialize(file));
    }
    public KeystoneSchematic deserialize(CompoundNBT nbt)
    {
        if (nbt.isEmpty()) return null;
        nbt = NBTUtil.update(Minecraft.getInstance().getFixerUpper(), DefaultTypeReferences.STRUCTURE, nbt, nbt.getInt("DataVersion"));

        // Size
        ListNBT sizeNBT = nbt.getList("size", Constants.NBT.TAG_INT);
        Vector3i size = new Vector3i(sizeNBT.getInt(0), sizeNBT.getInt(1), sizeNBT.getInt(2));

        // Palette and Blocks
        BlockType[] palette = loadPalette(nbt.getList("palette", Constants.NBT.TAG_COMPOUND));
        ListNBT blocksNBT = nbt.getList("blocks", Constants.NBT.TAG_COMPOUND);
        Block[] blocks = new Block[size.getX() * size.getY() * size.getZ()];
        loadBlocks(size, blocks, blocksNBT, palette);

        // Entities
        ListNBT entitiesNBT = nbt.getList("entities", Constants.NBT.TAG_COMPOUND);
        Entity[] entities = new Entity[entitiesNBT.size()];
        loadEntities(entities, entitiesNBT);

        // Extensions
        Map<ResourceLocation, ISchematicExtension> extensions = new HashMap<>();
        CompoundNBT extensionsNBT = nbt.getCompound("extensions");
        for (String namespace : extensionsNBT.getAllKeys())
        {
            CompoundNBT namespaceNBT = extensionsNBT.getCompound(namespace);
            for (String path : namespaceNBT.getAllKeys())
            {
                ResourceLocation id = new ResourceLocation(namespace, path);
                if (!dataExtensions.containsKey(id)) continue;

                ISchematicExtension extension = dataExtensions.get(id).deserialize(size, blocks, entities, namespaceNBT.getCompound(path));
                extensions.put(id, extension);
            }
        }

        return new KeystoneSchematic(size, blocks, entities, extensions);
    }

    private BlockType[] loadPalette(ListNBT paletteNBT)
    {
        BlockType[] palette = new BlockType[paletteNBT.size()];
        for (int i = 0; i < palette.length; i++)
        {
            CompoundNBT entry = paletteNBT.getCompound(i);
            BlockState blockState = NBTUtil.readBlockState(entry);
            palette[i] = BlockTypeRegistry.fromMinecraftBlock(blockState);
        }
        return palette;
    }
    private void loadBlocks(Vector3i size, Block[] blocks, ListNBT blocksNBT, BlockType[] palette)
    {
        for (int i = 0; i < blocksNBT.size(); i++)
        {
            CompoundNBT blockNBT = blocksNBT.getCompound(i);
            BlockType blockType = palette[blockNBT.getInt("state")];
            Block block;
            if (blockNBT.contains("nbt")) block = new Block(blockType, new NBTCompound(blockNBT.getCompound("nbt")));
            else block = new Block(blockType);

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
