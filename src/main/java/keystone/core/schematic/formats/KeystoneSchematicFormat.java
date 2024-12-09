package keystone.core.schematic.formats;

import keystone.api.Keystone;
import keystone.api.wrappers.entities.Entity;
import keystone.core.mixins.common.PalettedBlockInfoListInvoker;
import keystone.core.mixins.common.StructureTemplateAccessor;
import keystone.core.schematic.KeystoneSchematic;
import keystone.core.schematic.extensions.ISchematicExtension;
import keystone.core.utils.NBTSerializer;
import keystone.core.utils.PalettedArray;
import keystone.core.utils.RegistryLookups;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class KeystoneSchematicFormat implements ISchematicFormat
{
    private static final String[] FILE_EXTENSIONS = new String[] { "nbt", "kschem" };
    
    private static final String SIZE_KEY = "Size";
    private static final String BLOCKS_KEY = "Blocks";
    private static final String TILE_ENTITIES_KEY = "TileEntities";
    private static final String ENTITIES_KEY = "Entities";
    private static final String EXTENSIONS_KEY = "Extensions";
    
    private static final Map<Identifier, ISchematicExtension> dataExtensions = new HashMap<>();

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
    public static Map<Identifier, ISchematicExtension> createExtensions(World world, BlockBox bounds)
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
        nbt.putIntArray(SIZE_KEY, new int[] { schematic.getSize().getX(), schematic.getSize().getY(), schematic.getSize().getZ() });
        
        // Blocks
        nbt.put(BLOCKS_KEY, schematic.getBlocks().serialize(NbtHelper::fromBlockState));
        
        // Tile Entities
        NbtList tileEntities = NBTSerializer.serializeTileEntities(schematic.getTileEntities());
        nbt.put(TILE_ENTITIES_KEY, tileEntities);
        
        // Entities
        NbtList entities = new NbtList();
        schematic.forEachEntity(entity ->
        {
            NbtCompound entityNbt = new NbtCompound();
            NbtList posNbt = new NbtList();
            posNbt.add(NbtDouble.of(entity.pos().x));
            posNbt.add(NbtDouble.of(entity.pos().y));
            posNbt.add(NbtDouble.of(entity.pos().z));
            entityNbt.put("Pos", posNbt);
            entityNbt.put("Data", entity.data().getMinecraftNBT());
            entities.add(entityNbt);
        });
        nbt.put(ENTITIES_KEY, entities);
        
        // Extensions
        NbtList extensionsNbt = new NbtList();
        schematic.forEachExtension(extension ->
        {
            NbtCompound extensionNbt = new NbtCompound();
            extensionNbt.putString("ID", extension.id().toString());
            
            NbtCompound data = extension.serialize(schematic);
            if (data != null) extensionNbt.put("Data", data);
        });
        nbt.put(EXTENSIONS_KEY, extensionsNbt);
        
        return nbt;
    }
    
    @Override
    public void writeFile(Path path, KeystoneSchematic schematic) throws IOException
    {
        NbtIo.write(saveSchematic(schematic), path);
    }
    //endregion
    //region Loading
    @Override
    public KeystoneSchematic readFile(Path path) throws Exception
    {
        return deserialize(NbtIo.read(path));
    }
    public KeystoneSchematic deserialize(NbtCompound nbt)
    {
        if (nbt.isEmpty()) return null;
        
        // Size
        int[] sizeArray = nbt.getIntArray(SIZE_KEY);
        Vec3i size = new Vec3i(sizeArray[0], sizeArray[1], sizeArray[2]);
        
        // Blocks
        RegistryEntryLookup<Block> blockLookup = RegistryLookups.registryLookup(RegistryKeys.BLOCK);
        PalettedArray<BlockState> blocks = new PalettedArray<>(nbt.getCompound(BLOCKS_KEY), paletteEntry -> NbtHelper.toBlockState(blockLookup, (NbtCompound) paletteEntry));
        
        // Tile Entities
        NbtList tileEntitiesNbt = nbt.getList(TILE_ENTITIES_KEY, NbtElement.COMPOUND_TYPE);
        Map<BlockPos, NbtCompound> tileEntities = NBTSerializer.deserializeTileEntities(tileEntitiesNbt);
        
        // Entities
        NbtList entitiesNbt = nbt.getList(ENTITIES_KEY, NbtElement.COMPOUND_TYPE);
        Entity[] entities = new Entity[entitiesNbt.size()];
        for (int i = 0; i < entities.length; i++)
        {
            NbtCompound entityNbt = entitiesNbt.getCompound(i);
            NbtList posNbt = entityNbt.getList("Pos", NbtElement.DOUBLE_TYPE);
            entities[i] = new Entity(entityNbt.getCompound("Data"), false).position(posNbt.getDouble(0), posNbt.getDouble(1), posNbt.getDouble(2));
        }
        
        // Extensions
        NbtList extensionsNBT = nbt.getList(EXTENSIONS_KEY, NbtElement.COMPOUND_TYPE);
        Map<Identifier, ISchematicExtension> extensions = new HashMap<>();
        for (NbtElement extensionNBT : extensionsNBT)
        {
            NbtCompound extensionNbt = (NbtCompound) extensionNBT;
            Identifier id = Identifier.of(extensionNbt.getString("ID"));
            if (!dataExtensions.containsKey(id)) continue;
            
            ISchematicExtension extension = dataExtensions.get(id).deserialize(size, blocks, tileEntities, entities, extensionNbt.getCompound("Data"));
            extensions.put(id, extension);
        }
        
        // Create Schematic
        return new KeystoneSchematic(size, blocks, tileEntities, entities, new HashMap<>());
    }
    
    private static int index(Vec3i size, Vec3i pos)
    {
        return pos.getZ() + pos.getY() * size.getZ() + pos.getX() * size.getZ() * size.getY();
    }
    //endregion
}
