package keystone.core.schematic.formats;

import keystone.api.Keystone;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.blocks.BlockType;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.entities.Entity;
import keystone.api.wrappers.nbt.NBTCompound;
import keystone.core.mixins.PalettedBlockInfoListInvoker;
import keystone.core.mixins.StructureTemplateAccessor;
import keystone.core.registries.BlockTypeRegistry;
import keystone.core.schematic.KeystoneSchematic;
import keystone.core.schematic.extensions.ISchematicExtension;
import keystone.core.utils.EntityUtils;
import keystone.core.utils.NBTSerializer;
import keystone.core.utils.WorldRegistries;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.io.File;
import java.util.*;

public class KeystoneSchematicFormat implements ISchematicFormat
{
    private static final String[] FILE_EXTENSIONS = new String[] { "nbt", "kschem" };
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

        // Structure
        StructureTemplate template = new StructureTemplate();
        StructureTemplateAccessor accessor = (StructureTemplateAccessor) template;
        
        // Blocks
        List<StructureTemplate.StructureBlockInfo> blockList = new ArrayList<>();
        schematic.forEachBlock((pos, block) -> blockList.add(new StructureTemplate.StructureBlockInfo(pos, block.blockType().getMinecraftBlock(), block.tileEntity().getMinecraftNBT())));
        accessor.getBlockLists().add(PalettedBlockInfoListInvoker.invokeConstructor(blockList));
        
        // Entities
        schematic.forEachEntity(entity ->
        {
            Vec3d pos = entity.pos().getMinecraftVec3d();
            BlockPos blockPos = BlockPos.ofFloored(pos.x, pos.y, pos.z);
            accessor.getEntities().add(new StructureTemplate.StructureEntityInfo(pos, blockPos, entity.data().getMinecraftNBT()));
        });
        
        // Write Structure
        template.writeNbt(nbt);
        
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

        return nbt;
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
        int version = NbtHelper.getDataVersion(nbt, 500);
        nbt = DataFixTypes.STRUCTURE.update(MinecraftClient.getInstance().getDataFixer(), nbt, version);
    
        // Load Structure
        StructureTemplate template = new StructureTemplate();
        StructureTemplateAccessor accessor = (StructureTemplateAccessor) template;
        template.readNbt(WorldRegistries.blockLookup(), nbt);
    
        // Copy Data
        Vec3i size = template.getSize();
        Block[] blocks = new Block[size.getX() * size.getY() * size.getZ()];
        Entity[] entities = new Entity[accessor.getEntities().size()];
        for (StructureTemplate.StructureBlockInfo blockInfo : accessor.getBlockLists().get(0).getAll()) blocks[index(size, blockInfo.pos())] = new Block(blockInfo.state(), blockInfo.nbt());
        for (int i = 0; i < entities.length; i++)
        {
            StructureTemplate.StructureEntityInfo entityInfo = accessor.getEntities().get(i);
            entities[i] = new Entity(accessor.getEntities().get(i).nbt, false).position(entityInfo.pos.x, entityInfo.pos.y, entityInfo.pos.z);
        }

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
    
    private static int index(Vec3i size, Vec3i pos)
    {
        return pos.getZ() + pos.getY() * size.getZ() + pos.getX() * size.getZ() * size.getY();
    }
    //endregion
}
