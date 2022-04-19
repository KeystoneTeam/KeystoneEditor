package keystone.core.modules.world;

import keystone.api.Keystone;
import keystone.api.enums.RetrievalMode;
import keystone.api.wrappers.Biome;
import keystone.core.client.Player;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.history.WorldHistoryChunk;
import keystone.core.modules.world_cache.WorldCacheModule;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class BiomesModule implements IKeystoneModule
{
    public interface BiomeListener
    {
        void onChanged(int x, int y, int z, Biome biome);
    }

    private HistoryModule historyModule;
    private WorldCacheModule worldCacheModule;
    private Set<BiomeListener> listeners = new HashSet<>();

    public void addListener(BiomeListener listener) { listeners.add(listener); }
    public void removeListener(BiomeListener listener) { listeners.remove(listener); }
    public void clearListeners() { listeners.clear(); }

    @Override
    public void postInit()
    {
        historyModule = Keystone.getModule(HistoryModule.class);
        worldCacheModule = Keystone.getModule(WorldCacheModule.class);
    }
    @Override
    public boolean isEnabled()
    {
        return worldCacheModule.getDimensionWorld(Player.getDimension()) != null;
    }

    public World getWorld()
    {
        return worldCacheModule.getDimensionWorld(Player.getDimension());
    }

    /**
     * Set the {@link Biome} of a block in the current world. This will automatically hook into the history system, allowing
     * for undo and redo support. Be sure that the {@link HistoryModule} has an entry open first
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @param z The z-coordinate
     * @param biome The {@link Biome} to set
     */
    public void setBiome(int x, int y, int z, Biome biome)
    {
        historyModule.getOpenEntry().setBiome(x, y, z, biome);
        listeners.forEach(listener -> listener.onChanged(x, y, z, biome));
    }
    public Biome getBiome(int x, int y, int z, RetrievalMode retrievalMode)
    {
        World world = worldCacheModule.getDimensionWorld(Player.getDimension());
        if (!historyModule.isEntryOpen())
        {
            BlockPos pos = new BlockPos(x, y, z);
            return new Biome(world.getBiome(pos));
        }

        WorldHistoryChunk chunk = historyModule.getOpenEntry().getChunk(x, y, z);
        if (chunk != null) return chunk.getBiome(x, y, z, retrievalMode);
        else
        {
            BlockPos pos = new BlockPos(x, y, z);
            return new Biome(world.getBiome(pos));
        }
    }

    public void swapBuffers(boolean copy)
    {
        historyModule.swapBiomeBuffers(copy);
    }
}
