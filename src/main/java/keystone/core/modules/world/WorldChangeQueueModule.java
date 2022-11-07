package keystone.core.modules.world;

import keystone.api.Keystone;
import keystone.core.KeystoneConfig;
import keystone.core.KeystoneGlobalState;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.history.WorldHistoryChunk;
import keystone.core.modules.session.SessionModule;
import keystone.core.utils.ProgressBar;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WorldChangeQueueModule implements IKeystoneModule
{
    private enum QueueState
    {
        IDLE,
        PLACING_BLOCKS,
        PROCESSING_UPDATES
    }
    private record WorldChange(SessionModule session, WorldHistoryChunk chunk, boolean undo)
    {
        public void apply(QueueState queueState)
        {
            if (queueState == QueueState.PLACING_BLOCKS) applyChanges();
            else if (queueState == QueueState.PROCESSING_UPDATES) applyUpdates();
        }
        public void applyChanges()
        {
            session.registerChange(chunk);
            if (undo) chunk.revertBlocks();
            else chunk.placeBlocks();
        }
        public void applyUpdates()
        {
            chunk.processUpdates(undo);
        }
    }

    private final List<WorldChange> changeQueue = Collections.synchronizedList(new ArrayList<>());
    private int queueIndex;
    private QueueState state;
    
    private SessionModule session;
    private boolean waitingForChanges;
    private int cooldown;

    @Override public boolean isEnabled() { return true; }

    @Override
    public void postInit()
    {
        session = Keystone.getModule(SessionModule.class);
        queueIndex = 0;
        state = QueueState.IDLE;
        ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);
    }

    private void onServerTick(MinecraftServer server)
    {
        if (cooldown > 0) cooldown--;
        
        if (state == QueueState.PLACING_BLOCKS || state == QueueState.PROCESSING_UPDATES)
        {
            // Process a number of changes equal to KeystoneConfig.maxChunkUpdatesPerTick
            int updatesLeft = KeystoneConfig.maxChunkUpdatesPerTick;
            while (queueIndex < changeQueue.size() && updatesLeft > 0)
            {
                WorldChange change = this.changeQueue.get(queueIndex++);
                change.apply(state);
                if (waitingForChanges) ProgressBar.nextStep();
        
                updatesLeft--;
            }
            
            // Check if the end of the queue was reached
            if (queueIndex >= changeQueue.size())
            {
                queueIndex = 0;
                
                // If queue was placing blocks, transition to processing updates
                if (state == QueueState.PLACING_BLOCKS) transitionToUpdates();
                
                // If queue was processing updates, finalize changes and transition to idle
                else if (state == QueueState.PROCESSING_UPDATES) transitionToIdle();
            }
            else cooldown = KeystoneConfig.chunkUpdateCooldownTicks;
        }
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
        state = QueueState.IDLE;
        changeQueue.clear();
    
        if (waitingForChanges)
        {
            waitingForChanges = false;
            KeystoneGlobalState.WaitingForChangeQueue = false;
            ProgressBar.finish();
        }
    }

    public void enqueueChange(WorldHistoryChunk change, boolean undo)
    {
        if (state != QueueState.IDLE) throw new IllegalStateException("Trying to call WorldChangeQueueModule.enqueueChange while queue is not idle! This is not supported and will cause issues!");
        this.changeQueue.add(new WorldChange(session, change, undo));
    }
    public void waitForChanges(String progressBarTitle)
    {
        if (state != QueueState.IDLE) throw new IllegalStateException("Trying to call WorldChangeQueueModule.waitForChanges while queue is not idle! This is not supported and will cause issues!");
        if (this.changeQueue.size() > 0)
        {
            KeystoneGlobalState.WaitingForChangeQueue = true;
            this.waitingForChanges = true;
    
            KeystoneGlobalState.SuppressPlacementChecks = true;
            this.queueIndex = 0;
            this.state = QueueState.PLACING_BLOCKS;

            ProgressBar.start(progressBarTitle, 1);
            ProgressBar.beginIteration(this.changeQueue.size());
        }
    }
}
