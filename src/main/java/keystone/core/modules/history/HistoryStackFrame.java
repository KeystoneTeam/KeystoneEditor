package keystone.core.modules.history;

import keystone.api.Keystone;
import keystone.api.wrappers.Biome;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.entities.Entity;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.renderer.client.Player;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IServerWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryStackFrame
{
    private HistoryModule historyModule;
    private IServerWorld world;
    private List<IHistoryEntry> entries;
    private Map<Vector3i, WorldHistoryChunk> chunks;

    public HistoryStackFrame()
    {
        this.historyModule = Keystone.getModule(HistoryModule.class);
        this.world = Keystone.getModule(WorldCacheModule.class).getDimensionServerWorld(Player.getDimensionId());
        this.entries = new ArrayList<>();
        this.chunks = new HashMap<>();
    }

    public void undo()
    {
        for (WorldHistoryChunk chunk : chunks.values()) chunk.undo();
        for (int i = entries.size() - 1; i >= 0; i--) entries.get(i).undo();
    }
    public void redo()
    {
        for (WorldHistoryChunk chunk : chunks.values()) chunk.redo();
        for (IHistoryEntry entry : entries) entry.redo();
    }
    public void applyChanges()
    {
        for (WorldHistoryChunk chunk : chunks.values()) chunk.redo();
    }
    public boolean addToUnsavedChanges()
    {
        if (chunks.size() > 0) return true;
        for (IHistoryEntry entry : entries) if (entry.addToUnsavedChanges()) return true;
        return false;
    }
    public void debugLog(int index)
    {
        if (index == 0) Keystone.LOGGER.info("CURRENT");
        else Keystone.LOGGER.info(index > 0 ? "+" + index : index);

        for (int i = entries.size() - 1; i >= 0; i--) Keystone.LOGGER.info("    " + entries.get(i).getClass().getSimpleName());
        Keystone.LOGGER.info("    " + chunks.size() + " Chunks");
    }

    public void pushEntry(IHistoryEntry entry)
    {
        entry.onPushToHistory(historyModule, true);
        entries.add(entry);
        entry.onPushToHistory(historyModule, false);
    }
    public void setBlock(int x, int y, int z, Block block)
    {
        Vector3i chunkPosition = new Vector3i(x >> 4, y >> 4, z >> 4);
        if (!chunks.containsKey(chunkPosition)) chunks.put(chunkPosition, new WorldHistoryChunk(chunkPosition, world));
        chunks.get(chunkPosition).setBlock(x, y, z, block);
    }
    public void setBiome(int x, int y, int z, Biome biome)
    {
        Vector3i chunkPosition = new Vector3i(x >> 4, y >> 4, z >> 4);
        if (!chunks.containsKey(chunkPosition)) chunks.put(chunkPosition, new WorldHistoryChunk(chunkPosition, world));
        chunks.get(chunkPosition).setBiome(x, y, z, biome);
    }
    public void setEntity(Entity entity)
    {
        Vector3i chunkPosition = new Vector3i((int)entity.x() >> 4, (int)entity.y() >> 4, (int)entity.z() >> 4);
        if (!chunks.containsKey(chunkPosition)) chunks.put(chunkPosition, new WorldHistoryChunk(chunkPosition, world));
        chunks.get(chunkPosition).setEntity(entity);
    }

    public WorldHistoryChunk getChunk(int x, int y, int z)
    {
        Vector3i chunkPosition = new Vector3i(x >> 4, y >> 4, z >> 4);
        if (chunks.containsKey(chunkPosition)) return chunks.get(chunkPosition);
        else return null;
    }
    public WorldHistoryChunk getOrAddChunk(int x, int y, int z)
    {
        Vector3i chunkPosition = new Vector3i(x >> 4, y >> 4, z >> 4);
        if (!chunks.containsKey(chunkPosition)) chunks.put(chunkPosition, new WorldHistoryChunk(chunkPosition, world));
        return chunks.get(chunkPosition);
    }
    public void swapBlockBuffers(boolean copy)
    {
        for (WorldHistoryChunk chunk : chunks.values()) chunk.swapBlockBuffers(copy);
    }
    public void swapBiomeBuffers(boolean copy)
    {
        for (WorldHistoryChunk chunk : chunks.values()) chunk.swapBiomeBuffers(copy);
    }
    public void swapEntityBuffers(boolean copy)
    {
        for (WorldHistoryChunk chunk : chunks.values()) chunk.swapEntityBuffers(copy);
    }
}
