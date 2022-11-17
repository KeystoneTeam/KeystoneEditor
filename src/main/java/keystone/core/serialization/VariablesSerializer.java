package keystone.core.serialization;

import keystone.api.variables.Variable;
import keystone.core.events.keystone.KeystoneRegistryEvents;
import net.minecraft.nbt.NbtCompound;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class VariablesSerializer
{
    private static final Map<Class<?>, VariableSerializer<?>> serializers = new HashMap<>();
    public static <T> void registerSerializer(Class<T> clazz, VariableSerializer<T> serializer)
    {
        serializers.put(clazz, serializer);
    }
    public static void registerDefaultSerializers()
    {
        registerSerializer(int.class, new IntegerVariableSerializer());
        registerSerializer(float.class, new FloatVariableSerializer());
        registerSerializer(boolean.class, new BooleanVariableSerializer());

        registerSerializer(Integer.class, new IntegerVariableSerializer());
        registerSerializer(Float.class, new FloatVariableSerializer());
        registerSerializer(Boolean.class, new BooleanVariableSerializer());

        registerSerializer(String.class, new StringVariableSerializer());
    }

    public static <T> NbtCompound write(Class<T> clazz, T instance)
    {
        NbtCompound nbt = new NbtCompound();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields)
        {
            Variable variable = field.getAnnotation(Variable.class);
            if (variable != null)
            {
                try
                {
                    VariableSerializer<?> serializer = serializers.get(field.getType());
                    if (serializer == null) throw new IllegalArgumentException("No variable serializer is registered for type " + field.getType().getSimpleName() + "!");
                    Object value = field.get(instance);
                    if (value != null) serializer.writeCasted(field.getName(), value, nbt);
                }
                catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return nbt;
    }
    public static <T> T read(Class<T> clazz, NbtCompound nbt, Supplier<T> constructor)
    {
        Field[] fields = clazz.getDeclaredFields();
        T instance = constructor.get();

        for (Field field : fields)
        {
            Variable variable = field.getAnnotation(Variable.class);
            if (variable != null)
            {
                try
                {
                    VariableSerializer<?> serializer = serializers.get(field.getType());
                    if (serializer == null) throw new IllegalArgumentException("No variable serializer is registered for type " + field.getType().getSimpleName() + "!");
                    Object value = serializer.read(field.getName(), nbt);
                    if (value != null) field.set(instance, value);
                }
                catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return instance;
    }
}
