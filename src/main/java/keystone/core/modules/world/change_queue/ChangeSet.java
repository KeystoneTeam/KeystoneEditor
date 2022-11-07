package keystone.core.modules.world.change_queue;

import keystone.core.KeystoneConfig;
import keystone.core.KeystoneGlobalState;
import keystone.core.modules.history.WorldHistoryChunk;
import keystone.core.modules.session.SessionModule;
import keystone.core.utils.ProgressBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        public void apply(QueueState queueState)
        {
            if (queueState == QueueState.PLACING_BLOCKS) applyChanges();
            else if (queueState == QueueState.PROCESSING_UPDATES) applyUpdates();
        }
    
        public void applyChanges()
        {
            session.registerChange(chunk);
            if (undoing) chunk.revertBlocks();
            else chunk.placeBlocks();
        }
    
        public void applyUpdates()
        {
            chunk.processUpdates(undoing);
        }
    }
    
    private final List<WorldChange> changeQueue;
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
                
                if (state == QueueState.IDLE)
                {
                    this.flushMode = null;
                    this.changeQueue.clear();
                    if (callback != null) callback.run();
                }
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
        while (state != QueueState.IDLE) applyNextChange();
    }
    private void tickAsync()
    {
        // Process a number of changes equal to KeystoneConfig.maxChunkUpdatesPerTick
        applyChanges(KeystoneConfig.maxChunkUpdatesPerTick);
    }
    private void applyChanges(int count)
    {
        for (int i = 0; i < count && state != QueueState.IDLE; i++) applyNextChange();
    }
    private void applyNextChange()
    {
        if (queueIndex >= changeQueue.size())
        {
            transitionToIdle();
            return;
        }
    
        WorldChange change = this.changeQueue.get(queueIndex++);
        change.apply(state);
        if (flushMode == FlushMode.BLOCKING) ProgressBar.nextStep();
    
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
        KeystoneGlobalState.WaitingForChangeQueue = false;
        if (hasProgressBar) ProgressBar.finish();
    }
    //endregion
}
