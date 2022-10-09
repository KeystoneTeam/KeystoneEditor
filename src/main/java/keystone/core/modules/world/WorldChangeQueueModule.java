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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class WorldChangeQueueModule implements IKeystoneModule
{
    private interface IWorldChange
    {
        default int size() { return 1; }
        void apply();
    }
    private static class WorldChangeSet implements IWorldChange
    {
        private final SessionModule session;
        private final List<ChunkChange> changes;
        private final boolean undo;

        protected WorldChangeSet(SessionModule session, boolean undo)
        {
            this.session = session;
            this.changes = new ArrayList<>();
            this.undo = undo;
        }
        public WorldChangeSet addChunk(WorldHistoryChunk chunk)
        {
            changes.add(new ChunkChange(session, chunk, undo));
            return this;
        }

        @Override public int size() { return changes.size(); }
        public void apply() { for (ChunkChange change : changes) change.apply(); }
    }
    private record ChunkChange(SessionModule session, WorldHistoryChunk chunk, boolean undo) implements IWorldChange
    {
        public void apply()
        {
            session.registerChange(chunk);
            if (undo) chunk.undo();
            else chunk.redo();
        }
    }

    private final Queue<IWorldChange> changeQueue = new ArrayDeque<>();
    private SessionModule session;
    private boolean waitingForChanges;
    private int cooldown;

    @Override public boolean isEnabled() { return true; }

    @Override
    public void postInit()
    {
        session = Keystone.getModule(SessionModule.class);
        ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);
    }

    private void onServerTick(MinecraftServer server)
    {
        if (cooldown > 0) cooldown--;
        else
        {
            int updatesLeft = KeystoneConfig.maxChunkUpdatesPerTick;
            while (this.changeQueue.size() > 0 && updatesLeft > 0)
            {
                IWorldChange change = this.changeQueue.remove();
                change.apply();
                if (waitingForChanges) ProgressBar.nextStep();

                updatesLeft -= change.size();
            }

            if (waitingForChanges && this.changeQueue.size() <= 0)
            {
                waitingForChanges = false;
                KeystoneGlobalState.BlockingKeys = false;
                ProgressBar.finish();
            }
            cooldown = KeystoneConfig.chunkUpdateCooldownTicks;
        }
    }

    public void enqueueChange(WorldHistoryChunk change, boolean undo)
    {
        this.changeQueue.add(new ChunkChange(session, change, undo));
    }
    public void waitForChanges(String progressBarTitle)
    {
        if (this.changeQueue.size() > 0)
        {
            KeystoneGlobalState.BlockingKeys = true;
            this.waitingForChanges = true;

            ProgressBar.start(progressBarTitle, 1);
            ProgressBar.beginIteration(this.changeQueue.size());
        }
    }
}
