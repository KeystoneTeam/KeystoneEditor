package keystone.core.modules.filter.remapper.descriptors;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import keystone.core.modules.filter.remapper.enums.RemappingDirection;
import keystone.core.modules.filter.remapper.interfaces.IRemappable;
import keystone.core.modules.filter.remapper.mappings.MappingTree;

import java.util.Objects;
import java.util.Optional;

public class MethodDescriptor implements IRemappable<MethodDescriptor>
{
    private final ClassDescriptor declaringClass;
    private final String name;
    private final String descriptor;
    private final boolean isStatic;
    
    private MethodDescriptor(ClassDescriptor declaringClass, String name, String descriptor, boolean isStatic, MappingTree mappings)
    {
        this.declaringClass = declaringClass;
        this.name = name;
        this.descriptor = remapDescriptorParameters(descriptor, mappings);
        this.isStatic = isStatic;
    }
    
    public static MethodDescriptor fromMethodCall(MethodCallExpr methodCall, MappingTree mappings)
    {
        ResolvedMethodDeclaration resolved = methodCall.resolve();
        return fromMethodCall(methodCall, resolved, mappings);
    }
    public static MethodDescriptor fromMethodCall(MethodCallExpr methodCall, ResolvedMethodDeclaration declaration, MappingTree mappings)
    {
        ClassDescriptor declaringClass = ClassDescriptor.fromName(declaration.declaringType().getQualifiedName());
        String name = methodCall.getNameAsString();
        String descriptor = declaration.toDescriptor();
        return new MethodDescriptor(declaringClass, name, descriptor, declaration.isStatic(), mappings);
    }
    
    public ClassDescriptor getDeclaringClass() { return this.declaringClass; }
    public String getName() { return this.name; }
    public String getDescriptor() { return this.descriptor; }
    public String getNamedDescriptor() { return this.name + this.descriptor; }
    public boolean isStatic() { return this.isStatic; }
    
    private String remapDescriptorParameters(String descriptor, MappingTree mappings)
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
    
    @Override
    public MethodDescriptor remap(RemappingDirection direction, MappingTree mappings)
    {
        Optional<String> methodMapping = mappings.lookup(direction, this);
        return methodMapping.map(mapped ->
        {
            String mappedName = mapped.substring(0, mapped.indexOf('('));
            String mappedDescriptor = mapped.substring(mapped.indexOf('('));
            return new MethodDescriptor(this.declaringClass.remap(direction, mappings), mappedName, mappedDescriptor, this.isStatic, mappings);
        }).orElse(this);
    }
}
