package keystone.core.serialization;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class StringVariableSerializer implements VariableSerializer<String>
{
    @Override
    public void write(String name, String value, NbtCompound nbt)
    {
        nbt.putString(name, value);
    }

    @Override
    public String read(String name, NbtCompound nbt)
    {
        if (nbt.contains(name, NbtElement.STRING_TYPE)) return nbt.getString(name);
        else return null;
    }
}
