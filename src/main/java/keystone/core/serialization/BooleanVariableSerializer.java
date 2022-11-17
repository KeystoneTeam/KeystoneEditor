package keystone.core.serialization;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class BooleanVariableSerializer implements VariableSerializer<Boolean>
{
    @Override
    public void write(String name, Boolean value, NbtCompound nbt)
    {
        nbt.putBoolean(name, value);
    }

    @Override
    public Boolean read(String name, NbtCompound nbt)
    {
        if (nbt.contains(name, NbtElement.BYTE_TYPE)) return nbt.getBoolean(name);
        else return null;
    }
}
