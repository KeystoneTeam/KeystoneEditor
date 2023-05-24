package keystone.core.modules.filter.remapper.resolution;

import keystone.api.Keystone;
import keystone.core.KeystoneMod;
import keystone.core.modules.filter.remapper.descriptors.ClassDescriptor;
import keystone.core.modules.filter.remapper.descriptors.MethodDescriptorUtils;
import keystone.core.modules.filter.remapper.enums.MappingType;
import keystone.core.modules.filter.remapper.enums.RemappingDirection;
import keystone.core.modules.filter.remapper.mappings.Mapping;
import keystone.core.modules.filter.remapper.mappings.MappingTree;

import java.lang.reflect.Method;
import java.util.*;

public class MethodResolver
{
    public record MethodMappingInfo(Class<?> declaringClass, Class<?>[] parameterTypes, Method method, Mapping mapping) {}
    
    private final MappingTree mappings;
    private final Map<RemappingDirection, Map<String, List<MethodMappingInfo>>> methodMappings = new HashMap<>();
    
    public MethodResolver(MappingTree mappings)
    {
        this.mappings = mappings;
        
        Keystone.LOGGER.info("Building MethodResolver for Mapping Tree " + mappings + "...");
        long startTime = System.currentTimeMillis();
        buildMethodMappings();
        long duration = System.currentTimeMillis() - startTime;
        Keystone.LOGGER.info("Finished building MappingResolver in " + duration + "ms.");
    }
    
    public List<MethodMappingInfo> getPossibleMethodMappings(RemappingDirection direction, String name)
    {
        return methodMappings.get(direction).get(name);
    }
    
    private void buildMethodMappings()
    {
        this.mappings.forEachMapping(MappingType.METHOD, mapping ->
        {
            try
            {
                // Build the full descriptor of this method's declaring class
                ClassDescriptor declaringClassDescriptor = ClassDescriptor.fromMapping((Mapping) mapping.getParent());
                Class<?> declaringClass = KeystoneMod.class.getClassLoader().loadClass(declaringClassDescriptor.getClassLoaderName());
                
                // Parse the method descriptor
                Optional<Class<?>[]> parameterTypes = MethodDescriptorUtils.parseDescriptor(mapping.getNative(), this.mappings);
                if (parameterTypes.isEmpty())
                {
                    Keystone.LOGGER.warn("Could not parse descriptor of method " + mapping.getDeobfuscated() + "!");
                    return;
                }
                
                // Find the reflection method
                Optional<Method> method = findMethod(declaringClass, mapping, parameterTypes.get());
                if (method.isEmpty())
                {
                    Keystone.LOGGER.warn("Method " + mapping.getDeobfuscated() + " is missing a reflection method!");
                    return;
                }
                
                // Build the MethodMappingInfo
                MethodMappingInfo methodInfo = new MethodMappingInfo(declaringClass, parameterTypes.get(), method.get(), mapping);
                
                // Add Obfuscating Info
                Map<String, List<MethodMappingInfo>> map = methodMappings.computeIfAbsent(RemappingDirection.OBFUSCATING, type -> new TreeMap<>());
                List<MethodMappingInfo> infos = map.computeIfAbsent(mapping.getDeobfuscated().substring(0, mapping.getDeobfuscated().indexOf('(')), ignored -> new ArrayList<>());
                infos.add(methodInfo);
                
                // Add Deobfuscating Info
                map = methodMappings.computeIfAbsent(RemappingDirection.DEOBFUSCATING, type -> new TreeMap<>());
                infos = map.computeIfAbsent(mapping.getObfuscated().substring(0, mapping.getObfuscated().indexOf('(')), ignored -> new ArrayList<>());
                infos.add(methodInfo);
            }
            catch (Throwable e)
            {
                throw new RuntimeException(e);
            }
        }, true);
    }
    private Optional<Method> findMethod(Class<?> clazz, Mapping mapping, Class<?>... parameterTypes)
    {
        Optional<Method> method = findMethod(clazz, mapping.getObfuscated().substring(0, mapping.getObfuscated().indexOf('(')), parameterTypes);
        if (method.isPresent()) return method;
        else return findMethod(clazz, mapping.getDeobfuscated().substring(0, mapping.getDeobfuscated().indexOf('(')), parameterTypes);
    }
    private Optional<Method> findMethod(Class<?> clazz, String name, Class<?>... parameterTypes)
    {
        try { return Optional.of(clazz.getDeclaredMethod(name, parameterTypes)); }
        catch (Throwable ignored)
        {
            if (clazz.getSuperclass() != null) return findMethod(clazz.getSuperclass(), name, parameterTypes);
            else return Optional.empty();
        }
    }
}
