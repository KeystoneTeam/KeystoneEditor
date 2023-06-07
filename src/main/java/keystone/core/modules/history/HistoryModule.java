package keystone.core.modules.history;

import keystone.api.Keystone;
import keystone.api.KeystoneDirectories;
import keystone.core.KeystoneConfig;
import keystone.core.events.keystone.KeystoneLifecycleEvents;
import keystone.core.events.keystone.KeystoneRegistryEvents;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.world.change_queue.FlushMode;
import keystone.core.utils.NBTSerializer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class HistoryModule implements IKeystoneModule
{
    private Map<String, KeystoneRegistryEvents.RegisterHistoryEntriesListener.HistoryEntryDeserializer> deserializers = new HashMap<>();
    private HistoryStackFrame currentStackFrame;
    private int historyStackSize = 0;
    private int tryBeginHooksOpen = 0;
    private int currentHistoryIndex = -1;
    private int unsavedChanges = 0;
    private boolean skipClearOnLoad = false;

    public HistoryModule()
    {
        KeystoneLifecycleEvents.OPEN_WORLD.register(this::onJoinWorld);
        KeystoneLifecycleEvents.SAVE_SESSION_INFO.register(this::saveSession);
        KeystoneLifecycleEvents.REPAIR_SESSION.register(this::repairSession);
        KeystoneLifecycleEvents.COMMIT_SESSION.register(this::commitOrRevertSession);
        KeystoneLifecycleEvents.REVERT_SESSION.register(this::commitOrRevertSession);
    }
    
    private void onJoinWorld(World world)
    {
        if (skipClearOnLoad) skipClearOnLoad = false;
        else Keystone.getModule(HistoryModule.class).clearHistory();
    }

    @Override
    public boolean isEnabled()
    {
        return true;
    }

    @Override
    public void resetModule()
    {
        historyStackSize = 0;
        tryBeginHooksOpen = 0;
        currentHistoryIndex = -1;
        unsavedChanges = 0;
    }

    public int getUnsavedChanges() { return unsavedChanges; }
    
    public void commitOrRevertSession()
    {
        unsavedChanges = 0;
    }
    public void saveSession(Properties sessionInfo)
    {
        sessionInfo.setProperty("unsaved_changes", Integer.toString(unsavedChanges));
        sessionInfo.setProperty("current_history_index", Integer.toString(currentHistoryIndex));
    }
    public void repairSession(Properties sessionInfo)
    {
        this.unsavedChanges = Integer.parseInt(sessionInfo.getProperty("unsaved_changes"));
        this.currentHistoryIndex = Integer.parseInt(sessionInfo.getProperty("current_history_index"));
        this.skipClearOnLoad = true;
    }
    
    //region Deserializers
    public void registerDeserializer(String id, KeystoneRegistryEvents.RegisterHistoryEntriesListener.HistoryEntryDeserializer deserializer)
    {
        if (deserializers.containsKey(id))
        {
            Keystone.LOGGER.error("Trying to register already registered IHistoryEntry deserializer '" + id + "'!");
            return;
        }
        deserializers.put(id, deserializer);
    }
    public IHistoryEntry deserializeHistoryEntry(NbtCompound entryNBT)
    {
        String id = entryNBT.getString("id");
        if (!deserializers.containsKey(id))
        {
            Keystone.LOGGER.error("Trying to deserialize unregistered IHistoryEntry '" + id +"'!");
            return null;
        }
        return deserializers.get(id).deserialize(entryNBT);
    }
    //endregion
    //region History
    public void tryBeginHistoryEntry()
    {
        tryBeginHooksOpen++;
        if (currentStackFrame == null) beginHistoryEntry();
    }
    public void tryEndHistoryEntry()
    {
        if (tryBeginHooksOpen <= 0)
        {
            Keystone.LOGGER.warn("Calling HistoryModule.tryEndHistoryEntry without first calling HistoryModule.beginHistoryEntry! This may cause issues");
            tryBeginHooksOpen = 0;
        }
        else
        {
            tryBeginHooksOpen--;
            if (tryBeginHooksOpen <= 0) endHistoryEntry();
        }
    }
    public void beginHistoryEntry()
    {
        if (currentStackFrame != null)
        {
            Keystone.LOGGER.warn("Calling HistoryModule.beginHistoryEntry without first calling HistoryModule.endHistoryEntry! This may cause issues");
            return;
        }

        currentStackFrame = new HistoryStackFrame(currentHistoryIndex + 1);
        if (tryBeginHooksOpen <= 0) tryBeginHooksOpen = 1;
    }
    public void endHistoryEntry()
    {
        if (currentStackFrame == null)
        {
            Keystone.LOGGER.warn("Calling HistoryModule.endHistoryEntry without first calling HistoryModule.beginHistoryEntry! This may cause issues");
            return;
        }

        if (currentHistoryIndex < historyStackSize - 1) removeHistoryTail();
        addHistoryEntry(currentStackFrame);
        currentHistoryIndex++;

        if (currentStackFrame.addToUnsavedChanges()) unsavedChanges++;
        if (KeystoneConfig.debugHistoryLog) logHistoryStack();

        currentStackFrame.applyChanges(FlushMode.BLOCKING, null, "Applying Changes");
        currentStackFrame = null;
        tryBeginHooksOpen = 0;
    }
    public void applyChunksWithoutEnding(FlushMode flushMode, Runnable callback, String progressBarTitle)
    {
        if (currentStackFrame == null)
        {
            Keystone.LOGGER.warn("Calling HistoryModule.applyChunksWithoutEnding without first calling HistoryModule.beginHistoryEntry! This may cause issues");
            return;
        }
        currentStackFrame.applyChanges(flushMode, callback, progressBarTitle);
    }
    public void abortHistoryEntry()
    {
        currentStackFrame.undo();
        currentStackFrame = null;
        tryBeginHooksOpen = 0;
    }

    public void pushToEntry(IHistoryEntry historyEntry, IHistoryEntry revertEntry)
    {
        if (currentStackFrame == null)
        {
            Keystone.LOGGER.error("Calling HistoryModule.pushToEntry without first calling HistoryModule.beginHistoryEntry! This may cause issues");
            beginHistoryEntry();
        }

        currentStackFrame.pushEntry(historyEntry, revertEntry);
    }
    private HistoryStackFrame popFromHistory()
    {
        if (currentHistoryIndex >= 0)
        {
            HistoryStackFrame ret = loadHistoryEntry(currentHistoryIndex);
            currentHistoryIndex--;
            return ret;
        }
        else return null;
    }
    public void clearHistory()
    {
        File historyDirectory = KeystoneDirectories.getHistoryDirectory().toFile();
        for (File file : historyDirectory.listFiles()) file.delete();
        currentHistoryIndex = -1;
    }

    public void swapBlockBuffers(boolean copy)
    {
        if (currentStackFrame == null)
        {
            Keystone.LOGGER.error("Cannot call HistoryModule.swapBlockBuffers without an open history entry!");
            return;
        }

        currentStackFrame.swapBlockBuffers(copy);
    }
    public void swapBiomeBuffers(boolean copy)
    {
        if (currentStackFrame == null)
        {
            Keystone.LOGGER.error("Cannot call HistoryModule.swapBiomeBuffers without an open history entry!");
            return;
        }

        currentStackFrame.swapBiomeBuffers(copy);
    }
    public void swapEntityBuffers(boolean copy)
    {
        if (currentStackFrame == null)
        {
            Keystone.LOGGER.error("Cannot call HistoryModule.swapEntityBuffers without an open history entry!");
            return;
        }

        currentStackFrame.swapEntityBuffers(copy);
    }

    public void undo()
    {
        if (currentStackFrame != null)
        {
            Keystone.LOGGER.error("Cannot call HistoryModule.undo while a history entry is open!");
            return;
        }

        Keystone.runOnMainThread(() ->
        {
            HistoryStackFrame historyStackFrame = popFromHistory();
            if (historyStackFrame != null)
            {
                historyStackFrame.undo();
                addHistoryEntry(historyStackFrame);
                if (historyStackFrame.addToUnsavedChanges()) unsavedChanges--;
            }

            if (KeystoneConfig.debugHistoryLog) logHistoryStack();
        });
    }
    public void redo()
    {
        if (currentStackFrame != null)
        {
            Keystone.LOGGER.error("Cannot call HistoryModule.redo while a history entry is open!");
            return;
        }

        Keystone.runOnMainThread(() ->
        {
            if (currentHistoryIndex < historyStackSize - 1)
            {
                currentHistoryIndex++;
                HistoryStackFrame historyStackFrame = loadHistoryEntry(currentHistoryIndex);

                historyStackFrame.redo();
                addHistoryEntry(historyStackFrame);
                if (historyStackFrame.addToUnsavedChanges()) unsavedChanges++;
            }

            if (KeystoneConfig.debugHistoryLog) logHistoryStack();
        });
    }
    public HistoryStackFrame getOpenEntry()
    {
        if (currentStackFrame == null)
        {
            Keystone.LOGGER.error("Calling HistoryModule.getOpenEntry without first calling HistoryModule.beginHistoryEntry! This may cause issues");
            beginHistoryEntry();
        }

        return currentStackFrame;
    }
    public boolean isEntryOpen()
    {
        return currentStackFrame != null;
    }

    private void removeHistoryTail()
    {
        for (int i = currentHistoryIndex + 1; i < historyStackSize; i++)
        {
            File entryFile = KeystoneDirectories.getHistoryDirectory().resolve(i + ".nbt").toFile();
            if (entryFile.exists()) entryFile.delete();
        }
        historyStackSize = currentHistoryIndex + 1;
        unsavedChanges = Math.abs(unsavedChanges);
    }
    private void addHistoryEntry(HistoryStackFrame stackFrame)
    {
        File entryFile = KeystoneDirectories.getHistoryDirectory().resolve(stackFrame.index + ".nbt").toFile();
        NBTSerializer.serialize(entryFile, stackFrame.serialize());
        if (stackFrame.index > historyStackSize - 1) historyStackSize = stackFrame.index + 1;
    }
    private HistoryStackFrame loadHistoryEntry(int index)
    {
        File entryFile = KeystoneDirectories.getHistoryDirectory().resolve(index + ".nbt").toFile();
        if (entryFile.exists())
        {
            NbtCompound historyNBT = NBTSerializer.deserialize(entryFile);
            return new HistoryStackFrame(index, historyNBT);
        }
        else return null;
    }

    public void logHistoryStack()
    {
        Keystone.LOGGER.info("###########################################################################");
        for(int i = historyStackSize - 1; i >= 0; i--) loadHistoryEntry(i).debugLog(i - currentHistoryIndex);
        Keystone.LOGGER.info("###########################################################################");
        Keystone.LOGGER.info("");
    }
    //endregion
}
