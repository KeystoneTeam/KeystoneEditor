package keystone.core.modules.filter.remapper.descriptors;

import keystone.core.modules.filter.remapper.FilterRemapper;
import keystone.core.modules.filter.remapper.enums.RemappingDirection;
import keystone.core.modules.filter.remapper.interfaces.IRemappable;
import keystone.core.modules.filter.remapper.mappings.MappingTree;

import java.util.Optional;

public class ClassDescriptor implements IRemappable<ClassDescriptor>
{
    private final String descriptor;
    
    private ClassDescriptor(String descriptor) { this.descriptor = descriptor; }
    
    public static ClassDescriptor of(String descriptor) { return new ClassDescriptor(descriptor); }
    public static ClassDescriptor fromName(String qualifiedName) { return new ClassDescriptor(qualifiedName.replace('.', '/')); }
    
    //region Getters
    public String getDescriptor() { return this.descriptor; }
    public String getQualifiedName() { return this.descriptor.replace('/', '.').replace('$', '.'); }
    public String getSimpleName() { return this.descriptor.substring(descriptor.lastIndexOf('/') + 1).replace('$', '.'); }
    //endregion
    //region Object Overrides
    @Override
    public int hashCode()
    {
        return descriptor.hashCode();
    }
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null) return false;
        
        if (getClass() == o.getClass())
        {
            ClassDescriptor that = (ClassDescriptor) o;
            return descriptor.equals(that.descriptor);
        }
        else if (o.getClass() == String.class)
        {
            String that = (String) o;
            return descriptor.equals(that);
        }
        else return false;
    }
    @Override
    public String toString()
    {
        return descriptor;
    }
    //endregion
    
    @Override
    public ClassDescriptor remap(RemappingDirection direction, MappingTree mappings)
    {
        Optional<String> mapping = mappings.lookup(direction, this);
        return mapping.map(ClassDescriptor::new).orElse(this);
    }
    
    public Optional<Class<?>> asClass()
    {
        try { return Optional.of(FilterRemapper.REMAPPING_CLASS_LOADER.loadClass(getQualifiedName())); }
        catch (ClassNotFoundException ignored) { return Optional.empty(); }
    }
}
