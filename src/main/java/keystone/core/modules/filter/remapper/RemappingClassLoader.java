package keystone.core.modules.filter.remapper;

import keystone.core.modules.filter.remapper.mappings.Mapping;
import keystone.core.modules.filter.remapper.mappings.MappingTree;
import keystone.core.modules.filter.remapper.descriptors.ClassDescriptor;
import keystone.core.modules.filter.remapper.enums.RemappingDirection;

import java.util.Optional;

public class RemappingClassLoader extends ClassLoader
{
    private final MappingTree mappings;
    
    public RemappingClassLoader(MappingTree mappings, ClassLoader parent)
    {
        super("Remapped[" + parent.getName() + "]", parent);
        this.mappings = mappings;
    }
    
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException
    {
        try { return getParent().loadClass(name); }
        catch (ClassNotFoundException e)
        {
            // Get class descriptor to load
            ClassDescriptor descriptor = ClassDescriptor.fromName(name);
            
            // Search all mapping directions for the class descriptor
            Optional<Mapping> mapping;
            for (RemappingDirection direction : RemappingDirection.values())
            {
                mapping = mappings.lookupMapping(direction, descriptor);
                if (mapping.isPresent())
                {
                    descriptor = ClassDescriptor.fromMapping(mapping.get());
                    return getParent().loadClass(descriptor.getClassLoaderName());
                }
            }
            
            // If no mapping found, re-throw the exception
            throw e;
        }
    }
    
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
    {
        throw new ClassNotFoundException("RemappingClassLoader does not support loadClass(String, bool)!");
    }
    
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        throw new ClassNotFoundException("RemappingClassLoader does not support findClass!");
    }
}
