package keystone.core.serialization;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class IntegerVariableSerializer implements VariableSerializer<Integer>
{
    @Override
    public void write(String name, Integer value, NbtCompound nbt)
    {
        nbt.putInt(name, value);
    }

    @Override
    public Integer read(String name, NbtCompound nbt)
    {
        if (nbt.contains(name, NbtElement.INT_TYPE)) return nbt.getInt(name);
        else return null;
    }
}
