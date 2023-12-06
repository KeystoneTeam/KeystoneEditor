package keystone.core.modules.world_cache;

import keystone.core.events.keystone.KeystoneLifecycleEvents;
import keystone.core.modules.IKeystoneModule;
import keystone.core.renderer.blocks.world.GhostBlocksWorld;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class WorldCacheModule implements IKeystoneModule
{
    private Map<RegistryKey<World>, ServerWorld> loadedWorlds;
    private Map<RegistryKey<World>, GhostBlocksWorld> ghostWorlds;

    public WorldCacheModule()
    {
        loadedWorlds = new HashMap<>();
        ghostWorlds = new HashMap<>();

        ServerWorldEvents.LOAD.register(this::onWorldLoaded);
        KeystoneLifecycleEvents.CLOSE_WORLD.register(this::onSaveUnloaded);
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
        return RegistryKey.of(RegistryKeys.WORLD, dimension);
    }

    public boolean hasDimensionWorld(RegistryKey<World> dimensionId)
    {
        return loadedWorlds.containsKey(dimensionId);
    }
    public ServerWorld getDimensionWorld(RegistryKey<World> dimension)
    {
        return loadedWorlds.getOrDefault(dimension, null);
    }
    public GhostBlocksWorld getGhostWorld(RegistryKey<World> dimension)
    {
        return ghostWorlds.getOrDefault(dimension, null);
    }

    private void onWorldLoaded(MinecraftServer server, ServerWorld world)
    {
        RegistryKey<World> dimensionId = world.getRegistryKey();
        if (loadedWorlds.containsKey(dimensionId)) loadedWorlds.clear();
        loadedWorlds.put(dimensionId, world);
        if (ghostWorlds.containsKey(dimensionId)) ghostWorlds.clear();
        ghostWorlds.put(dimensionId, new GhostBlocksWorld(world, BlockRotation.NONE, BlockMirror.NONE));
    }
    private void onSaveUnloaded()
    {
        loadedWorlds.clear();
    }
}