package keystone.core.modules.world;

import keystone.api.Keystone;
import keystone.api.enums.RetrievalMode;
import keystone.api.wrappers.coordinates.BlockPos;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.coordinates.Vector3i;
import keystone.api.wrappers.entities.Entity;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.renderer.client.Player;
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
    private Set<EntityListener> listeners = new HashSet<>();

    public void addListener(EntityListener listener) { listeners.add(listener); }
    public void removeListener(EntityListener listener) { listeners.remove(listener); }
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
    public List<Entity> getEntities(BlockPos min, BlockPos max, RetrievalMode retrievalMode)
    {
        BoundingBox bb = new BoundingBox(min, max);
        return getEntities(bb, retrievalMode);
    }
    public List<Entity> getEntities(BoundingBox boundingBox, RetrievalMode retrievalMode)
    {
        if (!isEnabled())
        {
            Keystone.LOGGER.info("Trying to call EntitiesModule.getEntities when the EntitiesModule is not enabled!");
            return null;
        }

        List<Entity> entities = new ArrayList<>();
        if (historyModule.isEntryOpen())
        {
            Vector3i minChunk = new Vector3i((int)boundingBox.minX >> 4, (int)boundingBox.minY >> 4, (int)boundingBox.minZ >> 4);
            Vector3i maxChunk = new Vector3i((int)boundingBox.maxX >> 4, (int)boundingBox.maxY >> 4, (int)boundingBox.maxZ >> 4);
            for (int x = minChunk.x; x <= maxChunk.x; x++)
            {
                for (int y = minChunk.y; y <= maxChunk.y; y++)
                {
                    for (int z = minChunk.z; z <= maxChunk.z; z++)
                    {
                        historyModule.getOpenEntry().getOrAddChunk(x << 4, y << 4, z << 4).getEntities(entities, boundingBox, retrievalMode);
                    }
                }
            }
        }
        else
        {
            World world = worldCacheModule.getDimensionWorld(Player.getDimensionId());
            List<net.minecraft.entity.Entity> mcEntities = world.getEntitiesOfClass(net.minecraft.entity.Entity.class, boundingBox.getMinecraftBoundingBox());
            for (net.minecraft.entity.Entity mcEntity : mcEntities) entities.add(new Entity(mcEntity));
        }
        return entities;
    }
    public void setEntity(Entity entity)
    {
        historyModule.getOpenEntry().setEntity(entity);
        listeners.forEach(listener -> listener.onChanged(entity));
    }
}
