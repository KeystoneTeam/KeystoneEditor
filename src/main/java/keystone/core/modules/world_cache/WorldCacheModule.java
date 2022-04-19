package keystone.core.modules.world_cache;

import keystone.core.events.keystone.KeystoneLifecycleEvents;
import keystone.core.modules.IKeystoneModule;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class WorldCacheModule implements IKeystoneModule
{
    private Map<RegistryKey<World>, ServerWorld> loadedWorlds;

    public WorldCacheModule()
    {
        loadedWorlds = new HashMap<>();

        ServerWorldEvents.LOAD.register(this::onWorldLoaded);
        KeystoneLifecycleEvents.LEAVE.register(this::onSaveUnloaded);
    }

    @Override
    public boolean isEnabled()
    {
        return true;
    }
    @Override
    public void resetModule()
    {
        loadedWorlds.clear();
    }

    public static RegistryKey<World> getDimensionKey(Identifier dimension)
    {
        return RegistryKey.of(Registry.WORLD_KEY, dimension);
    }

    public boolean hasDimensionWorld(RegistryKey<World> dimensionId)
    {
        return loadedWorlds.containsKey(dimensionId);
    }
    public ServerWorld getDimensionWorld(RegistryKey<World> dimension)
    {
        return loadedWorlds.getOrDefault(dimension, null);
    }

    private void onWorldLoaded(MinecraftServer server, ServerWorld world)
    {
        RegistryKey<World> dimensionId = world.getRegistryKey();
        if (loadedWorlds.containsKey(dimensionId)) loadedWorlds.clear();
        loadedWorlds.put(dimensionId, world);
    }
    private void onSaveUnloaded()
    {
        loadedWorlds.clear();
    }
}