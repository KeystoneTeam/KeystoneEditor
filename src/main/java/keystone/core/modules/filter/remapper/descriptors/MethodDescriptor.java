package keystone.core.modules.filter.remapper.descriptors;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.types.ResolvedType;
import keystone.api.Keystone;
import keystone.core.modules.filter.remapper.enums.RemappingDirection;
import keystone.core.modules.filter.remapper.interfaces.IRemappable;
import keystone.core.modules.filter.remapper.mappings.MappingTree;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MethodDescriptor implements IRemappable<MethodDescriptor>
{
    private final ClassDescriptor declaringClass;
    private final String name;
    private final String descriptor;
    private final boolean isStatic;
    
    //region Creation
    private MethodDescriptor(ClassDescriptor declaringClass, String name, String descriptor, boolean isStatic)
    {
        this.declaringClass = declaringClass;
        this.name = name;
        this.descriptor = descriptor;
        this.isStatic = isStatic;
    }
    public static Optional<MethodDescriptor> fromMethodCall(MethodCallExpr methodCall, MappingTree mappings)
    {
        if (methodCall.getScope().isPresent())
        {
            ClassDescriptor declaringTypeDescriptor = ClassDescriptor.fromName(methodCall.getScope().get().calculateResolvedType().describe());
            Optional<Class<?>> declaringClass = declaringTypeDescriptor.asClass();
            if (declaringClass.isEmpty()) return Optional.empty();
            
            Optional<Class<?>[]> argumentTypes = MethodDescriptorParser.parseDescriptor(extractDescriptor(methodCall), mappings);
            if (argumentTypes.isEmpty()) return Optional.empty();
            
            List<MappingTree.MethodMappingInfo> possibleMethods = mappings.getPossibleMethodMappings(RemappingDirection.OBFUSCATING, methodCall.getNameAsString());
            MappingTree.MethodMappingInfo matchingMethod = null;
            for (MappingTree.MethodMappingInfo possibleMethod : possibleMethods)
            {
                if (possibleMethod.declaringClass().isAssignableFrom(declaringClass.get()) && possibleMethod.parameterTypes().length == argumentTypes.get().length)
                {
                    boolean goodDescriptor = true;
                    for (int i = 0; i < possibleMethod.parameterTypes().length; i++)
                    {
                        if (!possibleMethod.parameterTypes()[i].isAssignableFrom(argumentTypes.get()[i]))
                        {
                            goodDescriptor = false;
                            break;
                        }
                    }
                    
                    if (goodDescriptor)
                    {
                        if (matchingMethod == null) matchingMethod = possibleMethod;
                        else
                        {
                            String error = "Multiple matching methods found for method call '" + methodCall + "'!";
                            Keystone.LOGGER.error(error);
                            MinecraftClient.getInstance().player.sendMessage(Text.literal(error).styled(style -> style.withColor(Formatting.RED)));
                            return Optional.empty();
                        }
                    }
                }
            }
            
            if (matchingMethod != null)
            {
                String descriptor = remapDescriptorParameters(MethodDescriptorParser.buildDescriptor(matchingMethod.parameterTypes()), mappings);
                return Optional.of(new MethodDescriptor(declaringTypeDescriptor, methodCall.getNameAsString(), descriptor, Modifier.isStatic(matchingMethod.method().getModifiers())));
            }
            else return Optional.empty();
        }
        else return Optional.empty();
    }
    //endregion
    //region Creation Helpers
    private static String extractDescriptor(MethodCallExpr methodCall)
    {
        StringBuilder descriptor = new StringBuilder("(");
        for (Expression argumentNode : methodCall.getArguments())
        {
            String type = argumentNode.calculateResolvedType().describe();
            while (type.endsWith("[]"))
            {
                descriptor.append('[');
                type = type.substring(0, type.length() - 2);
            }
    
            switch (type)
            {
                case "byte" -> descriptor.append('B');
                case "char" -> descriptor.append('C');
                case "double" -> descriptor.append('D');
                case "float" -> descriptor.append('F');
                case "int" -> descriptor.append('I');
                case "long" -> descriptor.append('J');
                case "short" -> descriptor.append('S');
                case "boolean" -> descriptor.append('Z');
                default -> descriptor.append('L').append(type.replace('.', '/')).append(';');
            }
        }
        descriptor.append(")");
        
        return descriptor.toString();
    }
    private static String remapDescriptorParameters(String descriptor, MappingTree mappings)
    {
        StringBuilder remapped = new StringBuilder();
    
        for (int index = 0; index < descriptor.length(); index++)
        {
            char c = descriptor.charAt(index);
            if (c == 'L')
            {
                // Read class descriptor
                StringBuilder parameter = new StringBuilder();
                for (index++; index < descriptor.length(); index++)
                {
                    c = descriptor.charAt(index);
                    if (c == ';') break;
                    else parameter.append(c);
                }
            
                // Remap class descriptor
                ClassDescriptor remappedParameter = ClassDescriptor.of(parameter.toString()).remap(RemappingDirection.OBFUSCATING, mappings);
                remapped.append('L').append(remappedParameter.getDescriptor()).append(';');
            }
            else remapped.append(c);
        }
    
        return remapped.toString();
    }
    //endregion
    //region Getters
    public ClassDescriptor getDeclaringClass() { return this.declaringClass; }
    public String getName() { return this.name; }
    public String getDescriptor() { return this.descriptor; }
    public String getNamedDescriptor() { return this.name + this.descriptor; }
    public boolean isStatic() { return this.isStatic; }
    //endregion
    //region Object Overrides
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodDescriptor that = (MethodDescriptor) o;
        return declaringClass.equals(that.declaringClass) &&
                name.equals(that.name) &&
                descriptor.equals(that.descriptor) &&
                isStatic == that.isStatic;
    }
    @Override
    public int hashCode()
    {
        return Objects.hash(declaringClass, name, descriptor, isStatic);
    }
    @Override
    public String toString()
    {
        return declaringClass.getDescriptor() + "#" + name + descriptor ;
    }
    //endregion
    //region API
    @Override
    public MethodDescriptor remap(RemappingDirection direction, MappingTree mappings)
    {
        Optional<String> methodMapping = mappings.lookup(direction, this);
        return methodMapping.map(mapped ->
        {
            String mappedName = mapped.substring(0, mapped.indexOf('('));
            String mappedDescriptor = mapped.substring(mapped.indexOf('('));
            return new MethodDescriptor(this.declaringClass.remap(direction, mappings), mappedName, mappedDescriptor, isStatic);
        }).orElse(this);
    }
    //endregion
}
