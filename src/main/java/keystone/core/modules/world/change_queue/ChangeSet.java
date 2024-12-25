package keystone.core.modules.world.change_queue;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import keystone.core.KeystoneConfig;
import keystone.core.KeystoneGlobalState;
import keystone.core.mixins.common.ChunkSectionAccessor;
import keystone.core.mixins.common.ChunkSectionMixin;
import keystone.core.mixins.common.ServerChunkLoadingManagerAccessor;
import keystone.core.mixins.interfaces.KeystoneChunkSection;
import keystone.core.modules.history.chunk.WorldHistoryChunk;
import keystone.core.modules.session.SessionModule;
import keystone.core.utils.ProgressBar;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.ChunkBlockLightProvider;
import net.minecraft.world.chunk.light.ChunkLightingView;
import net.minecraft.world.chunk.light.LightingProvider;

import java.util.*;

public class ChangeSet
{
    public enum QueueState
    {
        IDLE,
        PLACING_BLOCKS,
        PROCESSING_UPDATES
    }
    
    private record WorldChange(SessionModule session, WorldHistoryChunk chunk, boolean undoing)
    {
        public void apply(QueueState queueState, Map<ServerWorld, DirtyChunkList> dirtyChunks)
        {
            if (queueState == QueueState.PLACING_BLOCKS) applyChanges(dirtyChunks);
            else if (queueState == QueueState.PROCESSING_UPDATES) applyUpdates();
        }
    
        public void applyChanges(Map<ServerWorld, DirtyChunkList> dirtyChunks)
        {
            session.registerChange(chunk);
            if (undoing) chunk.revertBlocks(dirtyChunks);
            else chunk.place(dirtyChunks);
        }
    
        public void applyUpdates()
        {
            chunk.processUpdates(undoing);
        }
    }
    
    private final List<WorldChange> changeQueue;
    private boolean redrawBiomes;
    private int queueIndex;
    private FlushMode flushMode;
    private QueueState state;
    private Runnable callback;
    private boolean hasProgressBar;
    private int cooldown;
    
    public ChangeSet()
    {
        this.changeQueue = Collections.synchronizedList(new ArrayList<>());
        this.queueIndex = 0;
        this.state = QueueState.IDLE;
        this.cooldown = 0;
    }
    
    public void enqueue(SessionModule session, WorldHistoryChunk chunk, boolean undoing)
    {
        if (state != QueueState.IDLE) throw new IllegalStateException("Trying to call ChangeSet.enqueue while queue is not idle! This is not supported and will cause issues!");
        this.changeQueue.add(new WorldChange(session, chunk, undoing));
        if (chunk.isBiomesChanged()) this.redrawBiomes = true;
    }
    
    public void beginFlush(FlushMode flushMode, Runnable callback, String progressBarTitle)
    {
        if (changeQueue.size() > 0)
        {
            KeystoneGlobalState.SuppressPlacementChecks = true;
            
            this.queueIndex = 0;
            this.flushMode = flushMode;
            this.state = QueueState.PLACING_BLOCKS;
            this.callback = callback;
            this.hasProgressBar = progressBarTitle != null && progressBarTitle.trim().length() > 0;
            this.cooldown = 0;
            
            if (hasProgressBar)
            {
                ProgressBar.start(progressBarTitle.trim(), 1);
                ProgressBar.beginIteration(this.changeQueue.size());
            }
            
            if (flushMode == FlushMode.IMMEDIATE) tickImmediate();
            else if (flushMode == FlushMode.BLOCKING) KeystoneGlobalState.WaitingForChangeQueue = true;
        }
        else if (callback != null) callback.run();
    }
    public void tick()
    {
        if (flushMode != null)
        {
            if (cooldown > 0) cooldown--;
    
            if (state == QueueState.PLACING_BLOCKS || state == QueueState.PROCESSING_UPDATES)
            {
                if (flushMode == FlushMode.BLOCKING) tickImmediate();
                else tickAsync();
            }
        }
    }
    public boolean isFlushing()
    {
        return flushMode != null;
    }
    
    //region Flush Helpers
    private void tickImmediate()
    {
        // Process all changes
        applyChanges(Integer.MAX_VALUE);
    }
    private void tickAsync()
    {
        // Process a number of changes equal to KeystoneConfig.maxChunkUpdatesPerTick
        applyChanges(KeystoneConfig.maxChunkUpdatesPerTick);
    }
    private void applyChanges(int count)
    {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld clientWorld = client.world;
        
        // Apply Changes
        Map<ServerWorld, DirtyChunkList> dirtyChunks = new Reference2ObjectArrayMap<>();
        for (int i = 0; i < count && state != QueueState.IDLE; i++) applyNextChange(dirtyChunks);
        
        // Send Packets and Updates
        for (Map.Entry<ServerWorld, DirtyChunkList> entry : dirtyChunks.entrySet())
        {
            ServerWorld world = entry.getKey();
            DirtyChunkList chunks = entry.getValue();
            ServerChunkLoadingManager chunkLoadingManager = world.getChunkManager().chunkLoadingManager;
            ServerLightingProvider lightingProvider = ((ServerChunkLoadingManagerAccessor)chunkLoadingManager).getLightingProvider();
            boolean updateClient = world.getDimensionEntry().getKey().orElseThrow().equals(clientWorld.getDimensionEntry().getKey().orElseThrow());
            
            // Repopulate Heightmaps
            for (Chunk chunk : chunks.getChunks())
            {
                Heightmap.populateHeightmaps(chunk, EnumSet.allOf(Heightmap.Type.class));
                chunk.refreshSurfaceY();
            }
            
            // Process Chunk Sections
            entry.getValue().forEachSection((section, pos) ->
            {
                relightChunk(section, pos, lightingProvider);
                
                // Sync with Client
                if (updateClient)
                {
                    // Get Client Chunk and Section
                    WorldChunk clientChunk = clientWorld.getChunk(pos.getSectionX(), pos.getSectionZ());
                    ChunkSection clientSection = clientChunk.getSection(clientChunk.sectionCoordToIndex(pos.getSectionY()));
                    
                    // Update Client Section BlockState Container
                    ((KeystoneChunkSection)clientSection).keystone_copyFrom(section);
                    relightChunk(section, pos, clientWorld.getLightingProvider());
                    
                    // Re-render Chunk
                    clientWorld.enqueueChunkUpdate(() ->
                    {
                        clientWorld.getChunkManager().getLightingProvider().setSectionStatus(pos, section.isEmpty());
                        clientWorld.scheduleBlockRenders(pos.getSectionX(), pos.getSectionY(), pos.getSectionZ());
                    });
                }
            });
            
            // Send Biome Packets
            chunkLoadingManager.sendChunkBiomePackets(chunks.getChunks());
        }
    }
    
    private void relightChunk(ChunkSection chunkSection, ChunkSectionPos pos, LightingProvider lightingProvider)
    {
        lightingProvider.setSectionStatus(pos, true);
        lightingProvider.enqueueSectionData(LightType.BLOCK, pos, null);
        lightingProvider.enqueueSectionData(LightType.SKY, pos, null);
        lightingProvider.propagateLight(new ChunkPos(pos.getSectionX(), pos.getSectionZ()));
    }
    
    private void applyNextChange(Map<ServerWorld, DirtyChunkList> dirtyChunks)
    {
        if (queueIndex >= changeQueue.size())
        {
            transitionToIdle();
            return;
        }
    
        WorldChange change = this.changeQueue.get(queueIndex++);
        change.apply(state, dirtyChunks);
        if (flushMode == FlushMode.BLOCKING) ProgressBar.nextStep();
    
        // Check if the end of the queue was reached
        if (queueIndex >= changeQueue.size())
        {
            queueIndex = 0;
        
            // If queue was placing blocks, transition to processing updates
            if (state == QueueState.PLACING_BLOCKS)
            {
                if (KeystoneGlobalState.SuppressingBlockTicks) transitionToIdle();
                else transitionToUpdates();
            }
            
            // If queue was processing updates, finalize changes and transition to idle
            else if (state == QueueState.PROCESSING_UPDATES) transitionToIdle();
        }
        else cooldown = KeystoneConfig.chunkUpdateCooldownTicks;
    }
    private void transitionToUpdates()
    {
        if (KeystoneGlobalState.SuppressingBlockTicks) transitionToIdle();
        else
        {
            state = QueueState.PROCESSING_UPDATES;
            KeystoneGlobalState.SuppressPlacementChecks = false;
        }
    }
    private void transitionToIdle()
    {
        flushMode = null;
        state = QueueState.IDLE;
        changeQueue.clear();
        if (callback != null) callback.run();
    
        KeystoneGlobalState.WaitingForChangeQueue = false;
        if (redrawBiomes)
        {
            for (Map.Entry<ServerWorld, List<Chunk>> chunkList : KeystoneGlobalState.DirtyChunks.entrySet()) chunkList.getKey().getChunkManager().chunkLoadingManager.sendChunkBiomePackets(chunkList.getValue());
            KeystoneGlobalState.DirtyChunks.clear();
        }
        if (hasProgressBar) ProgressBar.finish();
    }
    //endregion
}
