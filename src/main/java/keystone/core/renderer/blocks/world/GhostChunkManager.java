package keystone.core.renderer.blocks.world;

import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

public class GhostChunkManager extends ClientChunkManager
{
    private final GhostWorld world;
    private final Map<ChunkPos, WorldChunk> chunkMap = new HashMap<>();
    
    public GhostChunkManager(GhostWorld world, int loadDistance)
    {
        super(world, loadDistance);
        this.world = world;
    }
    
    @Nullable @Override public WorldChunk getChunk(int x, int z, ChunkStatus leastStatus, boolean create)
    {
        ChunkPos chunkPos = new ChunkPos(x, z);
        WorldChunk chunk = chunkMap.get(chunkPos);
        if (chunk == null && create)
        {
            chunk = new WorldChunk(world, chunkPos);
            chunkMap.put(chunkPos, chunk);
        }
        return chunk;
    }
    
    @Override public void tick(BooleanSupplier shouldKeepTicking, boolean tickChunks) { }
    @Override public String getDebugString() { return "GhostChunkManager[" + chunkMap.size() + " chunks]"; }
    @Override public int getLoadedChunkCount() { return chunkMap.size(); }
}
