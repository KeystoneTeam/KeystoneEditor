package keystone.core.utils;

import keystone.api.Keystone;
import keystone.api.wrappers.entities.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.util.math.BlockPos;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NBTSerializer
{
    //region Files
    public static boolean serialize(String path, NbtCompound nbt)
    {
        return serialize(new File(path), nbt);
    }
    public static boolean serialize(File file, NbtCompound nbt)
    {
        Path path = file.toPath();
        Path parent = path.getParent();

        if (parent == null) return false;
        else
        {
            // Create parent directories if necessary
            try
            {
                Files.createDirectories(Files.exists(parent) ? parent.toRealPath() : parent);
            }
            catch (IOException ioexception)
            {
                Keystone.LOGGER.error("Failed to create parent directory: {}", parent);
                return false;
            }

            // Serialize nbt
            try (OutputStream outputStream = new FileOutputStream(file))
            {
                NbtIo.writeCompressed(nbt, outputStream);
                return true;
            }
            catch (Throwable e)
            {
                e.printStackTrace();
                return false;
            }
        }
    }

    public static NbtCompound deserialize(String path)
    {
        return deserialize(new File(path));
    }
    public static NbtCompound deserialize(File file)
    {
        Path path = file.toPath();
        if (!Files.exists(path)) return new NbtCompound();

        try (InputStream inputStream = new FileInputStream(file))
        {
            NbtCompound nbt = NbtIo.readCompressed(inputStream, NbtSizeTracker.ofUnlimitedBytes());
            if (nbt == null) nbt = new NbtCompound();
            return nbt;
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            return new NbtCompound();
        }
    }
    //endregion
    //region Entity Maps
    public static NbtCompound serializeEntities(Map<UUID, Entity> entities)
    {
        NbtCompound nbt = new NbtCompound();
        for (Map.Entry<UUID, Entity> entry : entities.entrySet()) nbt.put(entry.getKey().toString(), entry.getValue().serialize());
        return nbt;
    }
    public static Map<UUID, Entity> deserializeEntities(NbtCompound nbt)
    {
        Map<UUID, Entity> entities = new HashMap<>();
        for (String key : nbt.getKeys())
        {
            Entity entity = Entity.deserialize(nbt.getCompound(key));
            entities.put(UUID.fromString(key), entity);
        }
        return entities;
    }
    //endregion
    //region Tile Entity Maps
    public static NbtList serializeTileEntities(Map<BlockPos, NbtCompound> tileEntities)
    {
        NbtList listNBT = new NbtList();
        for (Map.Entry<BlockPos, NbtCompound> entry : tileEntities.entrySet())
        {
            NbtCompound entityNBT = new NbtCompound();
            entityNBT.putIntArray("pos", new int[] { entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ() });
            entityNBT.put("nbt", entry.getValue());
            listNBT.add(entityNBT);
        }
        return listNBT;
    }
    public static Map<BlockPos, NbtCompound> deserializeTileEntities(NbtList tileEntitiesNBT)
    {
        Map<BlockPos, NbtCompound> ret = new HashMap<>();
        for (int i = 0; i < tileEntitiesNBT.size(); i++)
        {
            NbtCompound entityNBT = tileEntitiesNBT.getCompound(i);
            int[] pos = entityNBT.getIntArray("pos");
            ret.put(new BlockPos(pos[0], pos[1], pos[2]), entityNBT.getCompound("nbt"));
        }
        return ret;
    }
    //endregion
}
