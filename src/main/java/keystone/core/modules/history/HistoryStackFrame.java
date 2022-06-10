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
    private final List<IHistoryEntry> revertEntries;
    private final List<IHistoryEntry> applyEntries;
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
        this.revertEntries = Collections.synchronizedList(new ArrayList<>());
        this.applyEntries = Collections.synchronizedList(new ArrayList<>());
        this.chunks = Collections.synchronizedMap(new HashMap<>());
        if (nbt != null) deserialize(nbt);
    }

    public NbtCompound serialize()
    {
        NbtCompound nbt = new NbtCompound();

        nbt.putString("world", world.toServerWorld().getRegistryKey().getValue().toString());

        NbtList revertNBT = new NbtList();
        for (IHistoryEntry entry : revertEntries)
        {
            NbtCompound entryNBT = new NbtCompound();
            entryNBT.putString("id", entry.id());
            entry.serialize(entryNBT);
            revertNBT.add(entryNBT);
        }
        nbt.put("revert", revertNBT);

        NbtList applyNBT = new NbtList();
        for (IHistoryEntry entry : applyEntries)
        {
            NbtCompound entryNBT = new NbtCompound();
            entryNBT.putString("id", entry.id());
            entry.serialize(entryNBT);
            applyNBT.add(entryNBT);
        }
        nbt.put("apply", applyNBT);

        NbtList chunksNBT = new NbtList();
        for (WorldHistoryChunk chunk : chunks.values()) chunksNBT.add(chunk.serialize());
        nbt.put("chunks", chunksNBT);

        return nbt;
    }
    public void deserialize(NbtCompound nbt)
    {
        WorldCacheModule worldCacheModule = Keystone.getModule(WorldCacheModule.class);
        world = worldCacheModule.getDimensionWorld(WorldCacheModule.getDimensionKey(new Identifier(nbt.getString("world"))));

        NbtList revertNBT = nbt.getList("revert", NbtElement.COMPOUND_TYPE);
        revertEntries.clear();
        for (int i = 0; i < revertNBT.size(); i++)
        {
            NbtCompound entryNBT = revertNBT.getCompound(i);
            IHistoryEntry entry = historyModule.deserializeHistoryEntry(entryNBT);
            revertEntries.add(entry);
        }

        NbtList applyNBT = nbt.getList("apply", NbtElement.COMPOUND_TYPE);
        applyEntries.clear();
        for (int i = 0; i < applyNBT.size(); i++)
        {
            NbtCompound entryNBT = applyNBT.getCompound(i);
            IHistoryEntry entry = historyModule.deserializeHistoryEntry(entryNBT);
            applyEntries.add(entry);
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
        for (int i = revertEntries.size() - 1; i >= 0; i--) revertEntries.get(i).apply();
        worldChangeQueue.waitForChanges("Undoing");
    }
    public void redo()
    {
        for (WorldHistoryChunk chunk : chunks.values()) worldChangeQueue.enqueueChange(chunk, false);
        for (IHistoryEntry entry : applyEntries) entry.apply();
        worldChangeQueue.waitForChanges("Redoing");
    }
    public void applyChanges()
    {
        for (WorldHistoryChunk chunk : chunks.values()) worldChangeQueue.enqueueChange(chunk, false);
        worldChangeQueue.waitForChanges("Applying Changes");
    }
    public boolean addToUnsavedChanges()
    {
        if (chunks.size() > 0) return true;
        for (IHistoryEntry entry : applyEntries) if (entry.addToUnsavedChanges()) return true;
        return false;
    }
    public void debugLog(int index)
    {
        if (index == 0) Keystone.LOGGER.info("CURRENT");
        else Keystone.LOGGER.info(index > 0 ? "+" + index : index);

        Keystone.LOGGER.info("  Apply:");
        for (int i = 0; i < applyEntries.size(); i++) Keystone.LOGGER.info("    " + applyEntries.get(i).getClass().getSimpleName());

        Keystone.LOGGER.info("  Revert:");
        for (int i = revertEntries.size() - 1; i >= 0; i--) Keystone.LOGGER.info("    " + applyEntries.get(i).getClass().getSimpleName());

        Keystone.LOGGER.info("  " + chunks.size() + " Chunks");
    }

    public void pushEntry(IHistoryEntry entry, IHistoryEntry revert)
    {
        entry.onPushToHistory(historyModule, true);
        applyEntries.add(entry);
        revertEntries.add(revert);
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
    public void commitEntityChanges(Entity entity)
    {
        getOrAddChunk((int)entity.x(), (int)entity.y(), (int)entity.z()).commitEntityChanges(entity);
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
