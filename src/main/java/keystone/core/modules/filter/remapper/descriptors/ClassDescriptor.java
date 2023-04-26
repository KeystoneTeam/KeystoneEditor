package keystone.core.modules.filter.remapper.descriptors;

import keystone.core.modules.filter.remapper.FilterRemapper;
import keystone.core.modules.filter.remapper.enums.MappingType;
import keystone.core.modules.filter.remapper.enums.RemappingDirection;
import keystone.core.modules.filter.remapper.interfaces.IRemappable;
import keystone.core.modules.filter.remapper.mappings.Mapping;
import keystone.core.modules.filter.remapper.mappings.MappingTree;

import java.util.Optional;

public class ClassDescriptor implements IRemappable<ClassDescriptor>
{
    private final String descriptor;
    
    private ClassDescriptor(String descriptor) { this.descriptor = descriptor; }
    
    public static ClassDescriptor of(String descriptor) { return new ClassDescriptor(descriptor); }
    public static ClassDescriptor fromName(String qualifiedName) { return new ClassDescriptor(qualifiedName.replace('.', '/')); }
    public static ClassDescriptor fromMapping(Mapping mapping)
    {
        StringBuilder descriptorBuilder = new StringBuilder();
        Mapping currentClassLevel = mapping;
        while (currentClassLevel != null)
        {
            String className = currentClassLevel.getNative();
            descriptorBuilder.insert(0, className + "$");
            if (currentClassLevel.getParent() instanceof Mapping parentMapping && parentMapping.getType() == MappingType.CLASS) currentClassLevel = parentMapping;
            else break;
        }
        return of(descriptorBuilder.substring(0, descriptorBuilder.length() - 1));
    }
    
    //region Getters
    public String getDescriptor() { return this.descriptor; }
    public String getQualifiedName() { return this.descriptor.replace('/', '.').replace('$', '.'); }
    public String getClassLoaderName() { return this.descriptor.replace('/', '.'); }
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
        try { return Optional.of(FilterRemapper.REMAPPING_CLASS_LOADER.loadClass(getClassLoaderName())); }
        catch (ClassNotFoundException ignored) { return Optional.empty(); }
    }
}
