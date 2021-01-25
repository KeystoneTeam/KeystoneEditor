package keystone.modules.world_cache;

import keystone.core.renderer.common.models.DimensionId;
import keystone.modules.IKeystoneModule;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import java.util.HashMap;
import java.util.Map;

public class WorldCacheModule implements IKeystoneModule
{
    private Map<DimensionId, World> loadedWorlds = new HashMap<>();

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

    public boolean hasDimensionWorld(DimensionId dimensionId)
    {
        return loadedWorlds.containsKey(dimensionId);
    }
    public World getDimensionWorld(DimensionId dimension)
    {
        if (loadedWorlds.containsKey(dimension)) return loadedWorlds.get(dimension);
        else return null;
    }

    private void onWorldLoaded(final WorldEvent.Load event)
    {
        if (event.getWorld() instanceof World)
        {
            World world = (World)event.getWorld();
            if (!world.isRemote)
            {
                DimensionId dimensionId = DimensionId.from(world.getDimensionKey());
                if (loadedWorlds.containsKey(dimensionId)) loadedWorlds.clear();
                loadedWorlds.put(dimensionId, world);
            }
        }
    }
    private void onWorldUnloaded(final WorldEvent.Unload event)
    {
        if (event.getWorld() instanceof World)
        {
            World world = (World)event.getWorld();
            if (!world.isRemote)
            {
                DimensionId dimensionId = DimensionId.from(world.getDimensionKey());
                if (loadedWorlds.containsKey(dimensionId)) loadedWorlds.clear();
            }
        }
    }
}
