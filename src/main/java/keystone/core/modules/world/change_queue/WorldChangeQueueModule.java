package keystone.core.modules.world.change_queue;

import keystone.api.Keystone;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.history.WorldHistoryChunk;
import keystone.core.modules.session.SessionModule;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayDeque;
import java.util.Queue;

public class WorldChangeQueueModule implements IKeystoneModule
{
    private ChangeSet flushingSet;
    private Queue<ChangeSet> readyToFlush;
    private ChangeSet currentChangeSet;
    private SessionModule session;

    @Override public boolean isEnabled() { return true; }
    @Override
    public void postInit()
    {
        flushingSet = new ChangeSet();
        readyToFlush = new ArrayDeque<>();
        currentChangeSet = new ChangeSet();
        session = Keystone.getModule(SessionModule.class);
        ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);
    }
    @Override
    public void resetModule()
    {
        flushingSet = new ChangeSet();
        readyToFlush.clear();
        currentChangeSet = new ChangeSet();
    }
    
    private void onServerTick(MinecraftServer server)
    {
        if (!flushingSet.isFlushing() && readyToFlush.size() > 0) flushingSet = readyToFlush.remove();
        if (flushingSet.isFlushing()) flushingSet.tick();
    }

    public void enqueueChange(WorldHistoryChunk change, boolean undo)
    {
        this.currentChangeSet.enqueue(session, change, undo);
    }
    
    public void flushImmediate()
    {
        flush(FlushMode.IMMEDIATE, null, null);
    }
    public void flushAsync(Runnable callback)
    {
        flush(FlushMode.ASYNC, callback, null);
    }
    public void flushBlocking(String progressBarTitle)
    {
        flush(FlushMode.BLOCKING, null, progressBarTitle);
    }
    public void flush(FlushMode flushMode, Runnable callback, String progressBarTitle)
    {
        currentChangeSet.beginFlush(flushMode, callback, progressBarTitle);
        if (flushMode != FlushMode.IMMEDIATE) readyToFlush.add(currentChangeSet);
        currentChangeSet = new ChangeSet();
    }
}
