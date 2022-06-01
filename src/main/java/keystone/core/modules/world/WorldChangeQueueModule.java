package keystone.core.modules.world;

import keystone.core.KeystoneConfig;
import keystone.core.KeystoneGlobalState;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.history.WorldHistoryChunk;
import keystone.core.utils.ProgressBar;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayDeque;
import java.util.Queue;

public class WorldChangeQueueModule implements IKeystoneModule
{
    private static record WorldChange(WorldHistoryChunk chunk, boolean undo)
    {
        public void apply()
        {
            if (undo) chunk.undo();
            else chunk.redo();
        }
    }

    private final Queue<WorldChange> changeQueue = new ArrayDeque<>();
    private boolean waitingForChanges;
    private int cooldown;

    @Override public boolean isEnabled() { return true; }

    @Override
    public void postInit()
    {
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
                this.changeQueue.remove().apply();
                if (waitingForChanges) ProgressBar.nextStep();

                updatesLeft--;
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
        this.changeQueue.add(new WorldChange(change, undo));
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
