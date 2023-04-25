package keystone.core.modules.filter.remapper.descriptors;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;

import java.util.Objects;

public class MethodDescriptor
{
    private final ClassDescriptor declaringClass;
    private final String name;
    private final String descriptor;
    
    private MethodDescriptor(ClassDescriptor declaringClass, String name, String descriptor)
    {
        this.declaringClass = declaringClass;
        this.name = name;
        this.descriptor = descriptor;
    }
    
    public static MethodDescriptor fromMethodCall(MethodCallExpr methodCall)
    {
        ResolvedMethodDeclaration resolved = methodCall.resolve();
        return fromMethodCall(methodCall, resolved);
    }
    public static MethodDescriptor fromMethodCall(MethodCallExpr methodCall, ResolvedMethodDeclaration declaration)
    {
        ClassDescriptor declaringClass = ClassDescriptor.fromName(declaration.declaringType().getQualifiedName());
        String name = methodCall.getNameAsString();
        String descriptor = declaration.toDescriptor();
        return new MethodDescriptor(declaringClass, name, descriptor);
    }
    
    public ClassDescriptor getDeclaringClass() { return this.declaringClass; }
    public String getName() { return this.name; }
    public String getDescriptor() { return this.descriptor; }
    public String getNamedDescriptor() { return this.name + this.descriptor; }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodDescriptor that = (MethodDescriptor) o;
        return declaringClass.equals(that.declaringClass) && name.equals(that.name) && descriptor.equals(that.descriptor);
    }
    @Override
    public int hashCode()
    {
        return Objects.hash(declaringClass, name, descriptor);
    }
    @Override
    public String toString()
    {
        return declaringClass.getDescriptor() + "[" + name + descriptor + "]";
    }
}
