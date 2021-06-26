package keystone.core.utils;

import keystone.api.Keystone;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class NBTSerializer
{
    public static boolean serialize(String path, CompoundNBT nbt)
    {
        return serialize(new File(path), nbt);
    }
    public static boolean serialize(File file, CompoundNBT nbt)
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
                CompressedStreamTools.writeCompressed(nbt, outputStream);
                return true;
            }
            catch (Throwable e)
            {
                e.printStackTrace();
                return false;
            }
        }
    }

    public static CompoundNBT deserialize(String path)
    {
        return deserialize(new File(path));
    }
    public static CompoundNBT deserialize(File file)
    {
        Path path = file.toPath();
        if (!Files.exists(path)) return new CompoundNBT();

        try (InputStream inputStream = new FileInputStream(file))
        {
            CompoundNBT nbt = CompressedStreamTools.readCompressed(inputStream);
            if (nbt == null) nbt = new CompoundNBT();
            return nbt;
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            return new CompoundNBT();
        }
    }
}
