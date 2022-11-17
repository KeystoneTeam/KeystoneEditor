package keystone.core.serialization;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class FloatVariableSerializer implements VariableSerializer<Float>
{
    @Override
    public void write(String name, Float value, NbtCompound nbt)
    {
        nbt.putFloat(name, value);
    }

    @Override
    public Float read(String name, NbtCompound nbt)
    {
        if (nbt.contains(name, NbtElement.FLOAT_TYPE)) return nbt.getFloat(name);
        else return null;
    }
}
