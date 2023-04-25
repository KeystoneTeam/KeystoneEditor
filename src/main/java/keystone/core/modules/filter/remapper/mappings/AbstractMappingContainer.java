package keystone.core.modules.filter.remapper.mappings;

import keystone.core.modules.filter.remapper.descriptors.ClassDescriptor;
import keystone.core.modules.filter.remapper.enums.MappingType;
import keystone.core.modules.filter.remapper.descriptors.MethodDescriptor;
import keystone.core.modules.filter.remapper.enums.RemappingDirection;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public abstract class AbstractMappingContainer
{
    private final Map<RemappingDirection, Map<MappingType, Map<String, Mapping>>> mappings = Map.of
    (
            RemappingDirection.OBFUSCATING, new HashMap<>(),
            RemappingDirection.DEOBFUSCATING, new HashMap<>()
    );
    
    //region Lookups
    public Optional<String> lookup(RemappingDirection direction, ClassDescriptor descriptor) { return simplifyMapping(direction, lookupMapping(direction, descriptor)); }
    public Optional<String> lookup(RemappingDirection direction, MethodDescriptor descriptor) { return simplifyMapping(direction, lookupMapping(direction, descriptor)); }
    
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
        Optional<Mapping> declaringClass = lookupMapping(direction, descriptor.getDeclaringClass());
        if (declaringClass.isPresent()) return declaringClass.get().lookupMapping(direction, MappingType.METHOD, descriptor.getNamedDescriptor());
        else return Optional.empty();
    }
    //endregion
    //region Add Mappings
    public void putMapping(Mapping mapping)
    {
        Map<String, Mapping> map = mappings.get(RemappingDirection.OBFUSCATING).computeIfAbsent(mapping.getType(), e -> new TreeMap<>());
        map.put(mapping.getDeobfuscated(), mapping);
    
        map = mappings.get(RemappingDirection.DEOBFUSCATING).computeIfAbsent(mapping.getType(), e -> new TreeMap<>());
        map.put(mapping.getObfuscated(), mapping);
    }
    //endregion
    //region Private Helpers
    Optional<Mapping> lookupMapping(RemappingDirection direction, MappingType type, String key)
    {
        Map<String, Mapping> map = mappings.get(direction).get(type);
        if (map != null)
        {
            Mapping mapping = map.get(key);
            if (mapping != null) return Optional.of(mapping);
        }
        return Optional.empty();
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
