package keystone.core.modules.history;

import keystone.api.Keystone;
import keystone.api.KeystoneDirectories;
import keystone.core.KeystoneConfig;
import keystone.core.events.KeystoneEvent;
import keystone.core.modules.IKeystoneModule;
import keystone.core.utils.NBTSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryModule implements IKeystoneModule
{
    private Map<String, KeystoneEvent.RegisterHistoryEntryTypes.HistoryEntryDeserializer> deserializers = new HashMap<>();
    private HistoryStackFrame currentStackFrame;
    private int historyStackSize = 0;
    private int tryBeginHooksOpen = 0;
    private int currentHistoryIndex = -1;
    private int unsavedChanges = 0;

    public HistoryModule()
    {
        MinecraftForge.EVENT_BUS.addListener(this::onKeyboardInput);
    }

    private void onKeyboardInput(final InputEvent.KeyInputEvent event)
    {
        if (Keystone.isActive() && event.getAction() == GLFW.GLFW_PRESS && (event.getModifiers() & GLFW.GLFW_MOD_CONTROL) > 0)
        {
            if (event.getKey() == GLFW.GLFW_KEY_Z) undo();
            else if (event.getKey() == GLFW.GLFW_KEY_Y) redo();
        }
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

    //region Deserializers
    public void registerDeserializer(String id, KeystoneEvent.RegisterHistoryEntryTypes.HistoryEntryDeserializer deserializer)
    {
        if (deserializers.containsKey(id))
        {
            Keystone.LOGGER.error("Trying to register already registered IHistoryEntry deserializer '" + id + "'!");
            return;
        }
        deserializers.put(id, deserializer);
    }
    public IHistoryEntry deserializeHistoryEntry(CompoundNBT entryNBT)
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
            return;
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
            endHistoryEntry();
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

        currentStackFrame.applyChanges();
        currentStackFrame = null;
        tryBeginHooksOpen = 0;
    }
    public void applyBlocksWithoutEnding()
    {
        if (currentStackFrame == null)
        {
            Keystone.LOGGER.warn("Calling HistoryModule.applyBlocksWithoutEnding without first calling HistoryModule.beginHistoryEntry! This may cause issues");
            return;
        }
        currentStackFrame.applyChanges();
    }
    public void abortHistoryEntry()
    {
        currentStackFrame.undo();
        currentStackFrame = null;
    }

    public void pushToEntry(IHistoryEntry historyEntry)
    {
        if (currentStackFrame == null)
        {
            Keystone.LOGGER.error("Calling HistoryModule.pushToEntry without first calling HistoryModule.beginHistoryEntry! This may cause issues");
            beginHistoryEntry();
        }

        currentStackFrame.pushEntry(historyEntry);
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
        File historyDirectory = KeystoneDirectories.getHistoryDirectory();
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
                if (historyStackFrame.addToUnsavedChanges()) unsavedChanges++;
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
            File entryFile = KeystoneDirectories.getHistoryDirectory().toPath().resolve(i + ".nbt").toFile();
            if (entryFile.exists()) entryFile.delete();
        }
        historyStackSize = currentHistoryIndex + 1;
    }
    private void addHistoryEntry(HistoryStackFrame stackFrame)
    {
        File entryFile = KeystoneDirectories.getHistoryDirectory().toPath().resolve(stackFrame.index + ".nbt").toFile();
        NBTSerializer.serialize(entryFile, stackFrame.serialize());
        if (stackFrame.index > historyStackSize - 1) historyStackSize = stackFrame.index + 1;
    }
    private HistoryStackFrame loadHistoryEntry(int index)
    {
        File entryFile = KeystoneDirectories.getHistoryDirectory().toPath().resolve(index + ".nbt").toFile();
        if (entryFile.exists())
        {
            CompoundNBT historyNBT = NBTSerializer.deserialize(entryFile);
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
