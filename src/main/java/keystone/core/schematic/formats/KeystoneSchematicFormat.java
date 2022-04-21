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
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.datafixer.Schemas;
import net.minecraft.nbt.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.io.File;
import java.util.*;

public class KeystoneSchematicFormat implements ISchematicFormat
{
    private static final String[] FILE_EXTENSIONS = new String[] { "nbt", "kschem" };
    private static Map<Identifier, ISchematicExtension> dataExtensions = new HashMap<>();

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
    public static Map<Identifier, ISchematicExtension> createExtensions(World world, BoundingBox bounds)
    {
        Map<Identifier, ISchematicExtension> ret = new HashMap<>();
        for (Map.Entry<Identifier, ISchematicExtension> entry : dataExtensions.entrySet())
        {
            ISchematicExtension extension = entry.getValue().create(world, bounds);
            if (extension != null) ret.put(entry.getKey(), extension);
        }
        return Collections.unmodifiableMap(ret);
    }

    //region Saving
    public static NbtCompound saveSchematic(KeystoneSchematic schematic)
    {
        NbtCompound nbt = new NbtCompound();

        // Size
        NbtList sizeNBT = new NbtList();
        sizeNBT.add(0, NbtInt.of(schematic.getSize().getX()));
        sizeNBT.add(1, NbtInt.of(schematic.getSize().getY()));
        sizeNBT.add(2, NbtInt.of(schematic.getSize().getZ()));
        nbt.put("size", sizeNBT);

        // Palette
        List<BlockState> palette = generatePalette(schematic);
        NbtList paletteNBT = new NbtList();
        for (int i = 0; i < palette.size(); i++)
        {
            BlockState entry = palette.get(i);
            NbtCompound entryNBT = NbtHelper.fromBlockState(entry);
            paletteNBT.add(i, entryNBT);
        }
        nbt.put("palette", paletteNBT);

        // Blocks
        {
            NbtList blocksNBT = new NbtList();
            schematic.forEachBlock((pos, block) ->
            {
                NbtCompound blockNBT = new NbtCompound();

                NbtList positionNBT = new NbtList();
                positionNBT.add(0, NbtInt.of(pos.getX()));
                positionNBT.add(1, NbtInt.of(pos.getY()));
                positionNBT.add(2, NbtInt.of(pos.getZ()));

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
            NbtList entitiesNBT = new NbtList();
            int i = 0;
            schematic.forEachEntity(entity ->
            {
                NbtCompound entityNBT = new NbtCompound();
                NbtList positionNBT = new NbtList();
                NbtList blockPositionNBT = new NbtList();
                positionNBT.add(NbtDouble.of(entity.x()));
                positionNBT.add(NbtDouble.of(entity.y()));
                positionNBT.add(NbtDouble.of(entity.z()));
                blockPositionNBT.add(NbtInt.of((int)entity.x()));
                blockPositionNBT.add(NbtInt.of((int)entity.y()));
                blockPositionNBT.add(NbtInt.of((int)entity.z()));

                entityNBT.put("pos", positionNBT);
                entityNBT.put("blockPos", blockPositionNBT);

                NbtCompound entityDataNBT = entity.getMinecraftEntity().writeNbt(new NbtCompound());
                entityDataNBT.remove(net.minecraft.entity.Entity.UUID_KEY);
                entityNBT.put("nbt", entityDataNBT);
                entitiesNBT.add(entityNBT);
            });
            nbt.put("entities", entitiesNBT);
        }

        // Extensions
        List<Identifier> ids = new ArrayList<>(dataExtensions.keySet());
        ids.sort(Identifier::compareTo);
        NbtCompound extensionsNBT = new NbtCompound();
        for (Identifier id : ids)
        {
            ISchematicExtension extension = schematic.getExtension(id);
            if (extension == null) continue;
            NbtCompound namespaceNBT = extensionsNBT.contains(id.getNamespace(), NbtElement.COMPOUND_TYPE) ? extensionsNBT.getCompound(id.getNamespace()) : new NbtCompound();

            NbtCompound extensionNBT = new NbtCompound();
            extension.serialize(schematic, extensionNBT);
            namespaceNBT.put(id.getPath(), extensionNBT);
            extensionsNBT.put(id.getNamespace(), namespaceNBT);
        }
        nbt.put("extensions", extensionsNBT);

        nbt.putInt("DataVersion", SharedConstants.getGameVersion().getSaveVersion().getId());
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
    public KeystoneSchematic deserialize(NbtCompound nbt)
    {
        if (nbt.isEmpty()) return null;
        nbt = NbtHelper.update(Schemas.getFixer(), DataFixTypes.STRUCTURE, nbt, nbt.getInt("DataVersion"));

        // Size
        NbtList sizeNBT = nbt.getList("size", NbtElement.INT_TYPE);
        Vec3i size = new Vec3i(sizeNBT.getInt(0), sizeNBT.getInt(1), sizeNBT.getInt(2));

        // Palette and Blocks
        BlockType[] palette = loadPalette(nbt.getList("palette", NbtElement.COMPOUND_TYPE));
        NbtList blocksNBT = nbt.getList("blocks", NbtElement.COMPOUND_TYPE);
        Block[] blocks = new Block[size.getX() * size.getY() * size.getZ()];
        loadBlocks(size, blocks, blocksNBT, palette);

        // Entities
        NbtList entitiesNBT = nbt.getList("entities", NbtElement.COMPOUND_TYPE);
        Entity[] entities = new Entity[entitiesNBT.size()];
        loadEntities(entities, entitiesNBT);

        // Extensions
        Map<Identifier, ISchematicExtension> extensions = new HashMap<>();
        NbtCompound extensionsNBT = nbt.getCompound("extensions");
        for (String namespace : extensionsNBT.getKeys())
        {
            NbtCompound namespaceNBT = extensionsNBT.getCompound(namespace);
            for (String path : namespaceNBT.getKeys())
            {
                Identifier id = new Identifier(namespace, path);
                if (!dataExtensions.containsKey(id)) continue;

                ISchematicExtension extension = dataExtensions.get(id).deserialize(size, blocks, entities, namespaceNBT.getCompound(path));
                extensions.put(id, extension);
            }
        }

        return new KeystoneSchematic(size, blocks, entities, extensions);
    }

    private BlockType[] loadPalette(NbtList paletteNBT)
    {
        BlockType[] palette = new BlockType[paletteNBT.size()];
        for (int i = 0; i < palette.length; i++)
        {
            NbtCompound entry = paletteNBT.getCompound(i);
            BlockState blockState = NbtHelper.toBlockState(entry);
            palette[i] = BlockTypeRegistry.fromMinecraftBlock(blockState);
        }
        return palette;
    }
    private void loadBlocks(Vec3i size, Block[] blocks, NbtList blocksNBT, BlockType[] palette)
    {
        for (int i = 0; i < blocksNBT.size(); i++)
        {
            NbtCompound blockNBT = blocksNBT.getCompound(i);
            BlockType blockType = palette[blockNBT.getInt("state")];
            Block block;
            if (blockNBT.contains("nbt")) block = new Block(blockType, new NBTCompound(blockNBT.getCompound("nbt")));
            else block = new Block(blockType);

            NbtList posNBT = blockNBT.getList("pos", NbtElement.INT_TYPE);
            blocks[index(size, new int[] { posNBT.getInt(0), posNBT.getInt(1), posNBT.getInt(2) })] = block;
        }
    }
    private void loadEntities(Entity[] entities, NbtList entitiesNBT)
    {
        for (int i = 0; i < entitiesNBT.size(); i++)
        {
            NbtCompound entityNBT = entitiesNBT.getCompound(i);
            NbtList posNBT = entityNBT.getList("pos", NbtElement.DOUBLE_TYPE);
            NbtCompound nbt = entityNBT.getCompound("nbt");
            entities[i] = new Entity(nbt, false).position(posNBT.getDouble(0), posNBT.getDouble(1), posNBT.getDouble(2));
        }
    }

    private static int index(Vec3i size, int[] pos)
    {
        return pos[2] + pos[1] * size.getZ() + pos[0] * size.getZ() * size.getY();
    }
    //endregion
}
