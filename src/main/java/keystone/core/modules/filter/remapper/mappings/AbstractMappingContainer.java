package keystone.core.modules.filter.remapper.mappings;

import keystone.api.Keystone;
import keystone.core.modules.filter.remapper.descriptors.ClassDescriptor;
import keystone.core.modules.filter.remapper.descriptors.MethodDescriptor;
import keystone.core.modules.filter.remapper.enums.MappingType;
import keystone.core.modules.filter.remapper.enums.RemappingDirection;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Consumer;

public abstract class AbstractMappingContainer
{
    private AbstractMappingContainer parent;
    private final Map<RemappingDirection, Map<MappingType, Map<String, Mapping>>> mappings = Map.of
    (
            RemappingDirection.OBFUSCATING, new HashMap<>(),
            RemappingDirection.DEOBFUSCATING, new HashMap<>()
    );
    
    public void setParent(AbstractMappingContainer parent) { this.parent = parent; }
    public AbstractMappingContainer getParent() { return this.parent; }
    
    public void forEachMapping(Consumer<Mapping> consumer, boolean recursive)
    {
        mappings.get(RemappingDirection.OBFUSCATING).values().forEach(typeMappings -> typeMappings.values().forEach(mapping -> { consumer.accept(mapping); if (recursive) mapping.forEachMapping(consumer, true); }));
    }
    public void forEachMapping(MappingType type, Consumer<Mapping> consumer, boolean recursive)
    {
        forEachMapping(mapping -> { if (mapping.getType() == type) consumer.accept(mapping); }, recursive);
    }
    
    //region Lookups
    // Class Names
    public Optional<String> lookup(RemappingDirection direction, ClassDescriptor descriptor) { return simplifyMapping(direction, lookupMapping(direction, descriptor)); }
    public Optional<String> lookup(RemappingDirection direction, MethodDescriptor descriptor) { return simplifyMapping(direction, lookupMapping(direction, descriptor)); }
    
    // Methods
    public Optional<Mapping> lookupMapping(RemappingDirection direction, ClassDescriptor descriptor)
    {
        String[] tokens = descriptor.getDescriptor().split("\\$");
        Optional<Mapping> mapping = lookupMapping(direction, MappingType.CLASS, tokens[0]);
        
        if (tokens.length > 1)
        {
            for (int i = 1; i < tokens.length && mapping.isPresent(); i++)
            {
                mapping = mapping.get().lookupMapping(direction, MappingType.CLASS, tokens[i]);
            }
        }
        
        return mapping;
    }
    public Optional<Mapping> lookupMapping(RemappingDirection direction, MethodDescriptor descriptor)
    {
        Optional<Class<?>> clazz = descriptor.getDeclaringClass().asClass();
        if (clazz.isPresent()) return searchForMethodMapping(clazz.get(), direction, descriptor);
        else return Optional.empty();
    }
    
    // Raw
    public Optional<Mapping> lookupMapping(RemappingDirection direction, MappingType type, String key)
    {
        Map<String, Mapping> map = mappings.get(direction).get(type);
        if (map != null)
        {
            Mapping mapping = map.get(key);
            if (mapping != null) return Optional.of(mapping);
        }
        return Optional.empty();
    }
    public Optional<String> lookup(RemappingDirection direction, MappingType type, String key) { return simplifyMapping(direction, lookupMapping(direction, type, key)); }
    //endregion
    //region Add Mappings
    public void putMapping(Mapping mapping)
    {
        mapping.setParent(this);
        
        Map<String, Mapping> map = mappings.get(RemappingDirection.OBFUSCATING).computeIfAbsent(mapping.getType(), e -> new TreeMap<>());
        if (map.containsKey(mapping.getDeobfuscated())) Keystone.LOGGER.info("Deobfuscation map already contains " + mapping.getType().name() + " mapping '" + mapping.getDeobfuscated() + "'!");
        map.put(mapping.getDeobfuscated(), mapping);
    
        map = mappings.get(RemappingDirection.DEOBFUSCATING).computeIfAbsent(mapping.getType(), e -> new TreeMap<>());
        if (map.containsKey(mapping.getObfuscated())) Keystone.LOGGER.info("Obfuscation map already contains " + mapping.getType().name() + " mapping '" + mapping.getObfuscated() + "'!");
        map.put(mapping.getObfuscated(), mapping);
    }
    //endregion
    //region Private Helpers
    private Optional<Mapping> searchForMethodMapping(Class<?> clazz, RemappingDirection direction, MethodDescriptor descriptor)
    {
        // The ClassDescriptor for the type that is being used to invoke the method
        // This is NOT necessarily the class that the method is mapped in, as the
        // method might @Override a method in a parent class
        ClassDescriptor classDescriptor = ClassDescriptor.fromName(clazz.getName());
        Optional<Mapping> classMapping = lookupMapping(direction, classDescriptor);
    
        // Check the current class for a mapping
        if (classMapping.isPresent())
        {
            // Try to get a mapping for this method in the current level of
            // the inheritance hierarchy
            Mapping mapping = classMapping.get();
            Optional<Mapping> methodMapping = mapping.lookupMapping(direction, MappingType.METHOD, descriptor.getNamedDescriptor());
        
            // If a valid mapping is present, return it
            if (methodMapping.isPresent()) return methodMapping;
        }
        
        // Search this class's interfaces for a valid mapping
        for (Class<?> interfaceClass : clazz.getInterfaces())
        {
            Optional<Mapping> interfaceSearch = searchForMethodMapping(interfaceClass, direction, descriptor);
            if (interfaceSearch.isPresent()) return interfaceSearch;
        }
    
        // Try to go one level up in the inheritance hierarchy. If we are at
        // the top level, break out of the loop
        Optional<Class<?>> currentClass = classDescriptor.asClass();
        if (currentClass.isEmpty()) return Optional.empty();
        else
        {
            // Get the parent class of the current level in the inheritance hierarchy
            Class<?> parentClass = currentClass.get().getSuperclass();
        
            // If there is no parent class, return empty. Otherwise, search the parent class
            if (parentClass == null) return Optional.empty();
            else return searchForMethodMapping(parentClass, direction, descriptor);
        }
    }
    
    private Optional<String> simplifyMapping(RemappingDirection direction, Optional<Mapping> mapping)
    {
        return mapping.flatMap(value -> switch (direction)
        {
            case OBFUSCATING -> Optional.of(value.getObfuscated());
            case DEOBFUSCATING -> Optional.of(value.getDeobfuscated());
        });
    }
    //endregion
}
