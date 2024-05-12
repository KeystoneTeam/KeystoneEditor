package keystone.core.modules.world.biomes;

import keystone.api.Keystone;
import keystone.api.enums.RetrievalMode;
import keystone.api.wrappers.Biome;
import keystone.core.client.Player;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.history.WorldHistoryChunk;
import keystone.core.modules.world_cache.WorldCacheModule;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.dimension.DimensionType;

import java.util.HashMap;
import java.util.Map;

public class BiomesModule implements IKeystoneModule
{
    private HistoryModule historyModule;
    private WorldCacheModule worldCacheModule;

    private Map<DimensionType, Map<RetrievalMode, BiomeAccess>> biomeSmoothers;

    @Override
    public void postInit()
    {
        historyModule = Keystone.getModule(HistoryModule.class);
        worldCacheModule = Keystone.getModule(WorldCacheModule.class);
        biomeSmoothers = new HashMap<>();
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

    public Biome getBiome(int x, int y, int z, RetrievalMode retrievalMode, boolean smooth)
    {
        World world = getWorld();

        if (!historyModule.isEntryOpen())
        {
            BlockPos pos = new BlockPos(x, y, z);
            return new Biome(world.getBiome(pos));
        }

        if (smooth)
        {
            BiomeAccess smoother = getSmoother(world, retrievalMode);
            return new Biome(smoother.getBiome(new BlockPos(x, y, z)));
        }
        else
        {
            WorldHistoryChunk chunk = historyModule.getOpenEntry().getOrAddChunk(x, y, z);
            return chunk.getBiome(x, y, z, retrievalMode);
        }
    }
    public void setBiome(int x, int y, int z, Biome biome)
    {
        historyModule.getOpenEntry().setBiome(x, y, z, biome);
    }

    private BiomeAccess getSmoother(World world, RetrievalMode retrievalMode)
    {
        Map<RetrievalMode, BiomeAccess> dimensionSmoothers = biomeSmoothers.get(world.getDimension());
        if (dimensionSmoothers == null)
        {
            dimensionSmoothers = new HashMap<>();
            for (RetrievalMode mode : RetrievalMode.values()) dimensionSmoothers.put(mode, world.getBiomeAccess().withSource(new BiomeSmootherStorage(mode)));
            biomeSmoothers.put(world.getDimension(), dimensionSmoothers);
        }
        return dimensionSmoothers.get(retrievalMode);
    }
}
