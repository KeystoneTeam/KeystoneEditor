package keystone.core.modules.history;

import keystone.api.Keystone;
import keystone.api.wrappers.blocks.Block;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.renderer.client.Player;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryStackFrame
{
    private HistoryModule historyModule;
    private World world;
    private List<IHistoryEntry> entries;
    private Map<Vector3i, BlockHistoryChunk> chunks;

    public HistoryStackFrame()
    {
        this.historyModule = Keystone.getModule(HistoryModule.class);
        this.world = Keystone.getModule(WorldCacheModule.class).getDimensionWorld(Player.getDimensionId());
        this.entries = new ArrayList<>();
        this.chunks = new HashMap<>();
    }

    public void undo()
    {
        for (BlockHistoryChunk chunk : chunks.values()) chunk.undo();
        for (int i = entries.size() - 1; i >= 0; i--) entries.get(i).undo();
    }
    public void redo()
    {
        for (BlockHistoryChunk chunk : chunks.values()) chunk.redo();
        for (IHistoryEntry entry : entries) entry.redo();
    }
    public void applyBlocks()
    {
        for (BlockHistoryChunk chunk : chunks.values()) chunk.redo();
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
        if (!chunks.containsKey(chunkPosition)) chunks.put(chunkPosition, new BlockHistoryChunk(chunkPosition, world));
        chunks.get(chunkPosition).setBlock(x, y, z, block);
    }

    public BlockHistoryChunk getChunk(int x, int y, int z)
    {
        Vector3i chunkPosition = new Vector3i(x >> 4, y >> 4, z >> 4);
        if (chunks.containsKey(chunkPosition)) return chunks.get(chunkPosition);
        else return null;
    }
    public void swapBlockBuffers(boolean copy)
    {
        for (BlockHistoryChunk chunk : chunks.values()) chunk.swapBuffers(copy);
    }
}
