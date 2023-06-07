package keystone.core.modules.filter.blocks;

import keystone.api.Keystone;
import net.minecraft.util.Identifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public final class BlockProviderTypes
{
    private static final Map<Identifier, Class<? extends IBlockProvider>> providerTypes = new HashMap<>();
    private static final Map<Class<? extends IBlockProvider>, Identifier> reverseProviderTypes = new HashMap<>();
    
    public static void register(Identifier id, Class<? extends IBlockProvider> providerClass)
    {
        providerTypes.put(id, providerClass);
        reverseProviderTypes.put(providerClass, id);
    }
    
    public static Identifier getID(IBlockProvider provider)
    {
        Identifier id = reverseProviderTypes.get(provider.getClass());
        if (id == null)
        {
            String error = "Trying to get the ID of unregistered BlockProvider type '" + provider.getClass().getName() + "'!";
            Keystone.LOGGER.error(error);
            throw new IllegalArgumentException(error);
        }
        return id;
    }
    public static IBlockProvider createFromID(Identifier id)
    {
        Class<? extends IBlockProvider> providerClass = providerTypes.get(id);
        if (providerClass == null)
        {
            String error = "Trying to get the class of unregistered BlockProvider ID '" + id + "'!";
            Keystone.LOGGER.error(error);
            throw new IllegalArgumentException(error);
        }
        
        try
        {
            Constructor<? extends IBlockProvider> constructor = providerClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
        catch (NoSuchMethodException e)
        {
            String error = "IBlockProvider '" + providerClass.getSimpleName() + "' does not have a zero-argument constructor!";
            Keystone.LOGGER.error(error);
            throw new RuntimeException(e);
        }
    }
}
