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
    private HistoryModule historyModule;
    private WorldCacheModule worldCacheModule;

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
}
