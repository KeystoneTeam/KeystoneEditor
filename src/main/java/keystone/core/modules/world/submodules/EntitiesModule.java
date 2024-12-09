package keystone.core.modules.world.submodules;

import keystone.api.Keystone;
import keystone.api.enums.RetrievalMode;
import keystone.api.wrappers.entities.Entity;
import keystone.core.client.Player;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.world_cache.WorldCacheModule;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.*;

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
        Entity.setEntitiesModule(this);
    }
    @Override
    public boolean isEnabled()
    {
        return worldCacheModule.hasDimensionWorld(Player.getDimension());
    }

    public World getWorld()
    {
        return worldCacheModule.getDimensionWorld(Player.getDimension());
    }

    public net.minecraft.entity.Entity getMinecraftEntity(NbtCompound nbt)
    {
        if (nbt.containsUuid(net.minecraft.entity.Entity.UUID_KEY))
        {
            UUID uuid = nbt.getUuid(net.minecraft.entity.Entity.UUID_KEY);
            net.minecraft.entity.Entity entity = worldCacheModule.getDimensionWorld(Player.getDimension()).getEntity(uuid);
            if (entity != null) return entity;
        }
        return null;
    }
    public net.minecraft.entity.Entity createPreviewEntity(EntityType<?> type, NbtCompound nbt)
    {
        net.minecraft.entity.Entity minecraftEntity = type.create(worldCacheModule.getGhostWorld(Player.getDimension()));
        minecraftEntity.readNbt(nbt);
        return minecraftEntity;
    }

    public List<Entity> getEntities(BlockPos min, BlockPos max, RetrievalMode retrievalMode)
    {
        Box bb = new Box(Vec3d.of(min), Vec3d.of(max.add(1, 1, 1)));
        return getEntities(bb, retrievalMode);
    }
    public List<Entity> getEntities(Box boundingBox, RetrievalMode retrievalMode)
    {
        if (!isEnabled())
        {
            Keystone.LOGGER.info("Trying to call EntitiesModule.getEntities when the EntitiesModule is not enabled!");
            return null;
        }

        List<Entity> entities = new ArrayList<>();
        int minX = (int)Math.floor(boundingBox.minX);
        int minY = (int)Math.floor(boundingBox.minY);
        int minZ = (int)Math.floor(boundingBox.minZ);
        int maxX = (int)Math.ceil(boundingBox.maxX);
        int maxY = (int)Math.ceil(boundingBox.maxY);
        int maxZ = (int)Math.ceil(boundingBox.maxZ);
        if (historyModule.isEntryOpen())
        {
            Vec3i minChunk = new Vec3i(ChunkSectionPos.getSectionCoord(minX), ChunkSectionPos.getSectionCoord(minY), ChunkSectionPos.getSectionCoord(minZ));
            Vec3i maxChunk = new Vec3i(ChunkSectionPos.getSectionCoord(maxX), ChunkSectionPos.getSectionCoord(maxY), ChunkSectionPos.getSectionCoord(maxZ));
            for (int x = minChunk.getX(); x <= maxChunk.getX(); x++)
            {
                for (int y = minChunk.getY(); y <= maxChunk.getY(); y++)
                {
                    for (int z = minChunk.getZ(); z <= maxChunk.getZ(); z++)
                    {
                        historyModule.getOpenEntry().getOrAddChunk(x << 4, y << 4, z << 4).getEntities(entities, boundingBox, retrievalMode);
                    }
                }
            }
        }
        else
        {
            World world = worldCacheModule.getDimensionWorld(Player.getDimension());
            List<net.minecraft.entity.Entity> mcEntities = world.getNonSpectatingEntities(net.minecraft.entity.Entity.class, boundingBox);
            for (net.minecraft.entity.Entity mcEntity : mcEntities) entities.add(new Entity(mcEntity));
        }
        return entities;
    }
    public void commitEntityChanges(Entity entity)
    {
        historyModule.getOpenEntry().commitEntityChanges(entity);
        listeners.forEach(listener -> listener.onChanged(entity));
    }
}
