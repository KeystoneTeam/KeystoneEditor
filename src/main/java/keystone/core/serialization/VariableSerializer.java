package keystone.core.serialization;

import net.minecraft.nbt.NbtCompound;

public interface VariableSerializer<T>
{
    void write(String name, T value, NbtCompound nbt);
    T read(String name, NbtCompound nbt);

    default void writeCasted(String name, Object value, NbtCompound nbt)
    {
        write(name, (T)value, nbt);
    }
}