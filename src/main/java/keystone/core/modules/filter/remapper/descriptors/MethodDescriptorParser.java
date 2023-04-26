package keystone.core.modules.filter.remapper.descriptors;

import keystone.core.KeystoneMod;
import keystone.core.modules.filter.remapper.RemappingClassLoader;
import keystone.core.modules.filter.remapper.mappings.MappingTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MethodDescriptorParser
{
    private static int index;
    private static int end;
    private static String descriptor;
    private static RemappingClassLoader classLoader;
    
    public static String buildDescriptor(Class<?>[] parameterTypes)
    {
        StringBuilder descriptorBuilder = new StringBuilder("(");
        for (Class<?> type : parameterTypes) appendType(type, descriptorBuilder);
        descriptorBuilder.append(")");
        return descriptorBuilder.toString();
    }
    private static void appendType(Class<?> type, StringBuilder builder)
    {
        if (type.isArray())
        {
            builder.append("[");
            appendType(type.componentType(), builder);
        }
        else
        {
            if (type.equals(byte.class)) builder.append('B');
            else if (type.equals(char.class)) builder.append('C');
            else if (type.equals(double.class)) builder.append('D');
            else if (type.equals(float.class)) builder.append('F');
            else if (type.equals(int.class)) builder.append('I');
            else if (type.equals(long.class)) builder.append('J');
            else if (type.equals(short.class)) builder.append('S');
            else if (type.equals(boolean.class)) builder.append('Z');
            else
            {
                builder.append('L');
                builder.append(type.getName().replace('.', '/'));
                builder.append(';');
            }
        }
    }
    
    public static Optional<Class<?>[]> parseDescriptor(String descriptor, MappingTree mappings)
    {
        if (classLoader == null && mappings != null) classLoader = new RemappingClassLoader(mappings, KeystoneMod.class.getClassLoader());
        MethodDescriptorParser.descriptor = descriptor;
        end = descriptor.lastIndexOf(')');
        
        try
        {
            List<Class<?>> parameterTypes = new ArrayList<>();
            for (index = descriptor.indexOf('(') + 1; index < end; index++) parameterTypes.add(readType());
            return Optional.of(parameterTypes.toArray(Class<?>[]::new));
        }
        catch (Throwable ignore) { return Optional.empty(); }
    }
    
    private static Class<?> readType() throws ClassNotFoundException
    {
        char c = descriptor.charAt(index);
        return switch (c)
        {
            case 'B' -> byte.class;
            case 'C' -> char.class;
            case 'D' -> double.class;
            case 'F' -> float.class;
            case 'I' -> int.class;
            case 'J' -> long.class;
            case 'S' -> short.class;
            case 'Z' -> boolean.class;
            case 'L' -> readClass();
            case '[' ->
            {
                index++;
                Class<?> elementType = readType();
                yield elementType.arrayType();
            }
            default -> null;
        };
    }
    private static Class<?> readClass() throws ClassNotFoundException
    {
        index++;
        StringBuilder builder = new StringBuilder();
        for (; index < end; index++)
        {
            char c = descriptor.charAt(index);
            if (c == ';') break;
            else if (c == '/') builder.append('.');
            else builder.append(c);
        }
        
        try
        {
            return (classLoader != null ? classLoader : KeystoneMod.class.getClassLoader()).loadClass(builder.toString());
        }
        catch (Exception e)
        {
            throw e;
        }
    }
}
