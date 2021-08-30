package keystone.core.modules.history;

import keystone.api.Keystone;
import keystone.api.wrappers.Biome;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.entities.Entity;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.common.models.DimensionId;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.*;

public class HistoryStackFrame
{
    public final int index;

    private IServerWorld world;
    private final HistoryModule historyModule;
    private final List<IHistoryEntry> entries;
    private final Map<Vector3i, WorldHistoryChunk> chunks;

    public HistoryStackFrame(int index)
    {
        this.historyModule = Keystone.getModule(HistoryModule.class);
        this.index = index;
        this.world = Keystone.getModule(WorldCacheModule.class).getDimensionServerWorld(Player.getDimensionId());
        this.entries = Collections.synchronizedList(new ArrayList<>());
        this.chunks = Collections.synchronizedMap(new HashMap<>());
    }
    public HistoryStackFrame(int index, CompoundNBT nbt)
    {
        this.historyModule = Keystone.getModule(HistoryModule.class);
        this.index = index;
        this.entries = Collections.synchronizedList(new ArrayList<>());
        this.chunks = Collections.synchronizedMap(new HashMap<>());
        deserialize(nbt);
    }

    public CompoundNBT serialize()
    {
        CompoundNBT nbt = new CompoundNBT();

        nbt.putString("world", world.getLevel().dimension().location().toString());

        ListNBT entriesNBT = new ListNBT();
        for (IHistoryEntry entry : entries)
        {
            CompoundNBT entryNBT = new CompoundNBT();
            entryNBT.putString("id", entry.id());
            entry.serialize(entryNBT);
            entriesNBT.add(entryNBT);
        }
        nbt.put("entries", entriesNBT);

        ListNBT chunksNBT = new ListNBT();
        for (WorldHistoryChunk chunk : chunks.values()) chunksNBT.add(chunk.serialize());
        nbt.put("chunks", chunksNBT);

        return nbt;
    }
    public void deserialize(CompoundNBT nbt)
    {
        WorldCacheModule worldCacheModule = Keystone.getModule(WorldCacheModule.class);
        world = worldCacheModule.getDimensionServerWorld(DimensionId.from(new ResourceLocation(nbt.getString("world"))));

        ListNBT entriesNBT = nbt.getList("entries", Constants.NBT.TAG_COMPOUND);
        entries.clear();
        for (int i = 0; i < entriesNBT.size(); i++)
        {
            CompoundNBT entryNBT = entriesNBT.getCompound(i);
            IHistoryEntry entry = historyModule.deserializeHistoryEntry(entryNBT);
            entries.add(entry);
        }

        ListNBT chunksNBT = nbt.getList("chunks", Constants.NBT.TAG_COMPOUND);
        chunks.clear();
        for (int i = 0; i < chunksNBT.size(); i++)
        {
            WorldHistoryChunk chunk = new WorldHistoryChunk(chunksNBT.getCompound(i));
            chunks.put(new Vector3i(chunk.chunkX, chunk.chunkY, chunk.chunkZ), chunk);
        }
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
        getOrAddChunk(x, y, z).setBlock(x, y, z, block);
    }
    public void setBiome(int x, int y, int z, Biome biome)
    {
        getOrAddChunk(x, y, z).setBiome(x, y, z, biome);
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
        Vector3i chunkPosition = new Vector3i(x >> 4, y >> 4, z >> 4);
        return chunks.getOrDefault(chunkPosition, null);
    }
    public WorldHistoryChunk getOrAddChunk(int x, int y, int z)
    {
        Vector3i chunkPosition = new Vector3i(x >> 4, y >> 4, z >> 4);
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
        Vector3i chunkPosition = new Vector3i(chunkX, chunkY, chunkZ);
        if (!chunks.containsKey(chunkPosition)) chunks.put(chunkPosition, new WorldHistoryChunk(chunkPosition, world));
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
