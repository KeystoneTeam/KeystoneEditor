package keystone.core.modules.history;

import keystone.api.Keystone;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.blocks.BlockType;
import keystone.api.wrappers.entities.Entity;
import keystone.core.client.Player;
import keystone.core.modules.world.WorldChangeQueueModule;
import keystone.core.modules.world_cache.WorldCacheModule;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.ServerWorldAccess;

import java.util.*;

public class HistoryStackFrame
{
    public final int index;

    private ServerWorldAccess world;
    private final HistoryModule historyModule;
    private final WorldChangeQueueModule worldChangeQueue;
    private final List<IHistoryEntry> entries;
    private final Map<Vec3i, WorldHistoryChunk> chunks;

    public HistoryStackFrame(int index)
    {
        this (index, null);
    }
    public HistoryStackFrame(int index, NbtCompound nbt)
    {
        this.historyModule = Keystone.getModule(HistoryModule.class);
        this.worldChangeQueue = Keystone.getModule(WorldChangeQueueModule.class);
        this.index = index;
        this.world = Keystone.getModule(WorldCacheModule.class).getDimensionWorld(Player.getDimension());
        this.entries = Collections.synchronizedList(new ArrayList<>());
        this.chunks = Collections.synchronizedMap(new HashMap<>());
        if (nbt != null) deserialize(nbt);
    }

    public NbtCompound serialize()
    {
        NbtCompound nbt = new NbtCompound();

        nbt.putString("world", world.toServerWorld().getRegistryKey().getValue().toString());

        NbtList entriesNBT = new NbtList();
        for (IHistoryEntry entry : entries)
        {
            NbtCompound entryNBT = new NbtCompound();
            entryNBT.putString("id", entry.id());
            entry.serialize(entryNBT);
            entriesNBT.add(entryNBT);
        }
        nbt.put("entries", entriesNBT);

        NbtList chunksNBT = new NbtList();
        for (WorldHistoryChunk chunk : chunks.values()) chunksNBT.add(chunk.serialize());
        nbt.put("chunks", chunksNBT);

        return nbt;
    }
    public void deserialize(NbtCompound nbt)
    {
        WorldCacheModule worldCacheModule = Keystone.getModule(WorldCacheModule.class);
        world = worldCacheModule.getDimensionWorld(WorldCacheModule.getDimensionKey(new Identifier(nbt.getString("world"))));

        NbtList entriesNBT = nbt.getList("entries", NbtElement.COMPOUND_TYPE);
        entries.clear();
        for (int i = 0; i < entriesNBT.size(); i++)
        {
            NbtCompound entryNBT = entriesNBT.getCompound(i);
            IHistoryEntry entry = historyModule.deserializeHistoryEntry(entryNBT);
            entries.add(entry);
        }

        NbtList chunksNBT = nbt.getList("chunks", NbtElement.COMPOUND_TYPE);
        chunks.clear();
        for (int i = 0; i < chunksNBT.size(); i++)
        {
            WorldHistoryChunk chunk = new WorldHistoryChunk(chunksNBT.getCompound(i));
            chunks.put(new Vec3i(chunk.chunkX, chunk.chunkY, chunk.chunkZ), chunk);
        }
    }

    public void undo()
    {
        for (WorldHistoryChunk chunk : chunks.values()) worldChangeQueue.enqueueChange(chunk, true);
        for (int i = entries.size() - 1; i >= 0; i--) entries.get(i).undo();
    }
    public void redo()
    {
        for (WorldHistoryChunk chunk : chunks.values()) worldChangeQueue.enqueueChange(chunk, false);
        for (IHistoryEntry entry : entries) entry.redo();
    }
    public void applyChanges()
    {
        for (WorldHistoryChunk chunk : chunks.values()) worldChangeQueue.enqueueChange(chunk, false);
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
    public void setBlock(int x, int y, int z, BlockType blockType)
    {
        getOrAddChunk(x, y, z).setBlock(x, y, z, blockType);
    }
    public void setBlock(int x, int y, int z, Block block)
    {
        getOrAddChunk(x, y, z).setBlock(x, y, z, block);
    }
    public void setEntity(Entity entity)
    {
        int x = (int)entity.x() >> 4;
        int y = (int)entity.y() >> 4;
        int z = (int)entity.z() >> 4;
        getOrAddChunk(x, y, z).setEntity(entity);
    }

    public WorldHistoryChunk getChunk(int x, int y, int z)
    {
        Vec3i chunkPosition = new Vec3i(x >> 4, y >> 4, z >> 4);
        return chunks.getOrDefault(chunkPosition, null);
    }
    public WorldHistoryChunk getOrAddChunk(int x, int y, int z)
    {
        Vec3i chunkPosition = new Vec3i(x >> 4, y >> 4, z >> 4);
        WorldHistoryChunk chunk = chunks.getOrDefault(chunkPosition, null);
        if (chunk == null)
        {
            chunk = new WorldHistoryChunk(chunkPosition, world);
            chunks.put(chunkPosition, chunk);
        }
        return chunk;
    }
    public void preloadChunk(int chunkX, int chunkY, int chunkZ)
    {
        Vec3i chunkPosition = new Vec3i(chunkX, chunkY, chunkZ);
        if (!chunks.containsKey(chunkPosition)) chunks.put(chunkPosition, new WorldHistoryChunk(chunkPosition, world));
    }
    public void swapBlockBuffers(boolean copy)
    {
        for (WorldHistoryChunk chunk : chunks.values()) chunk.swapBlockBuffers(copy);
    }
    public void swapEntityBuffers(boolean copy)
    {
        for (WorldHistoryChunk chunk : chunks.values()) chunk.swapEntityBuffers(copy);
    }
}
