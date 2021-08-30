package keystone.core.modules.world_cache;

import keystone.core.modules.IKeystoneModule;
import keystone.core.renderer.common.models.DimensionId;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import java.util.HashMap;
import java.util.Map;

public class WorldCacheModule implements IKeystoneModule
{
    private Map<DimensionId, IServerWorld> loadedWorlds;

    public WorldCacheModule()
    {
        loadedWorlds = new HashMap<>();

        MinecraftForge.EVENT_BUS.addListener(this::onWorldLoaded);
        MinecraftForge.EVENT_BUS.addListener(this::onWorldUnloaded);
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

    public boolean hasDimensionWorld(DimensionId dimensionId)
    {
        return loadedWorlds.containsKey(dimensionId);
    }
    public IServerWorld getDimensionServerWorld(DimensionId dimension)
    {
        return loadedWorlds.getOrDefault(dimension, null);
    }
    public World getDimensionWorld(DimensionId dimension)
    {
        if (loadedWorlds.containsKey(dimension)) return loadedWorlds.get(dimension).getLevel();
        else return null;
    }

    private void onWorldLoaded(final WorldEvent.Load event)
    {
        if (event.getWorld() instanceof World)
        {
            World world = (World)event.getWorld();
            if (!world.isClientSide && world instanceof IServerWorld)
            {
                DimensionId dimensionId = DimensionId.from(world.dimension());
                if (loadedWorlds.containsKey(dimensionId)) loadedWorlds.clear();
                loadedWorlds.put(dimensionId, (IServerWorld)world);
            }
        }
    }
    private void onWorldUnloaded(final WorldEvent.Unload event)
    {
        if (event.getWorld() instanceof World)
        {
            World world = (World)event.getWorld();
            if (!world.isClientSide && world instanceof IServerWorld)
            {
                DimensionId dimensionId = DimensionId.from(world.dimension());
                if (loadedWorlds.containsKey(dimensionId)) loadedWorlds.clear();
            }
        }
    }
}
