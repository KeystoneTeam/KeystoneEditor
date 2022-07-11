package keystone.core.modules.world.biomes;

import keystone.api.Keystone;
import keystone.api.enums.RetrievalMode;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.history.WorldHistoryChunk;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeCoords;

public class BiomeSmootherStorage implements BiomeAccess.Storage
{
    private final HistoryModule historyModule;
    private final RetrievalMode retrievalMode;

    public BiomeSmootherStorage(RetrievalMode retrievalMode)
    {
        this.historyModule = Keystone.getModule(HistoryModule.class);
        this.retrievalMode = retrievalMode;
    }

    @Override
    public RegistryEntry<Biome> getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ)
    {
        int blockX = BiomeCoords.toBlock(biomeX);
        int blockY = BiomeCoords.toBlock(biomeY);
        int blockZ = BiomeCoords.toBlock(biomeZ);
        biomeX = BiomeCoords.method_39920(biomeX);
        biomeY = BiomeCoords.method_39920(biomeY);
        biomeZ = BiomeCoords.method_39920(biomeZ);

        WorldHistoryChunk chunk = historyModule.getOpenEntry().getOrAddChunk(blockX, blockY, blockZ);
        return chunk.getBiomeRaw(biomeX, biomeY, biomeZ, retrievalMode);
    }
}
