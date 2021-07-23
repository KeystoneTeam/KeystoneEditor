package keystone.core.modules.entities;

import keystone.api.Keystone;
import keystone.api.wrappers.coordinates.BlockPos;
import keystone.api.wrappers.entities.Entity;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.blocks.BlocksModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.renderer.client.Player;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntitiesModule implements IKeystoneModule
{
    public interface EntityListener
    {
        void onChanged(Entity entity);
    }

    private HistoryModule historyModule;
    private WorldCacheModule worldCacheModule;
    private Set<BlocksModule.BlockListener> listeners = new HashSet<>();

    public void addListener(BlocksModule.BlockListener listener) { listeners.add(listener); }
    public void removeListener(BlocksModule.BlockListener listener) { listeners.remove(listener); }
    public void clearListeners() { listeners.clear(); }

    @Override
    public void postInit()
    {
        this.historyModule = Keystone.getModule(HistoryModule.class);
        this.worldCacheModule = Keystone.getModule(WorldCacheModule.class);
    }
    @Override
    public boolean isEnabled()
    {
        return worldCacheModule.hasDimensionWorld(Player.getDimensionId());
    }

    public World getWorld()
    {
        return worldCacheModule.getDimensionWorld(Player.getDimensionId());
    }
    public List<Entity> getEntities(BlockPos min, BlockPos max)
    {
        AxisAlignedBB bb = new AxisAlignedBB(min.getMinecraftBlockPos(), max.getMinecraftBlockPos().offset(1, 1, 1));
        return getEntities(bb);
    }
    public List<Entity> getEntities(AxisAlignedBB boundingBox)
    {
        if (!isEnabled())
        {
            Keystone.LOGGER.info("Trying to call EntitiesModule.getEntities when the EntitiesModule is not enabled!");
            return null;
        }
        List<net.minecraft.entity.Entity> mcEntities = getWorld().getEntitiesOfClass(net.minecraft.entity.Entity.class, boundingBox);
        List<Entity> entities = new ArrayList<>(mcEntities.size());
        for (net.minecraft.entity.Entity mcEntity : mcEntities) entities.add(new Entity(mcEntity));
        return entities;
    }
}
