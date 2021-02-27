package keystone.core.modules.history;

import keystone.api.Keystone;
import keystone.core.KeystoneConfig;
import keystone.core.modules.IKeystoneModule;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class HistoryModule implements IKeystoneModule
{
    private List<HistoryStackFrame> history = new ArrayList<>();
    private HistoryStackFrame currentStackFrame;
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

    //region History
    public void beginHistoryEntry()
    {
        if (currentStackFrame != null)
        {
            Keystone.LOGGER.warn("Calling HistoryModule.beginHistoryEntry without first calling HistoryModule.endHistoryEntry! This may cause issues");
            endHistoryEntry();
        }

        currentStackFrame = new HistoryStackFrame();
    }
    public void endHistoryEntry()
    {
        if (currentStackFrame == null)
        {
            Keystone.LOGGER.warn("Calling HistoryModule.endHistoryEntry without first calling HistoryModule.beginHistoryEntry! This may cause issues");
            return;
        }

        if (currentHistoryIndex < history.size() - 1) for (int i = history.size() - 1; i > currentHistoryIndex; i--) history.remove(i);
        history.add(currentStackFrame);
        currentHistoryIndex++;

        if (currentStackFrame.addToUnsavedChanges()) unsavedChanges++;
        if (KeystoneConfig.debugHistoryLog) logHistoryStack();

        currentStackFrame.applyBlocks();
        currentStackFrame = null;
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
            HistoryStackFrame ret = history.get(currentHistoryIndex);
            currentHistoryIndex--;
            return ret;
        }
        else return null;
    }
    public void clearHistory()
    {
        history.clear();
        currentHistoryIndex = -1;
    }

    public void undo()
    {
        Keystone.runOnMainThread(() ->
        {
            HistoryStackFrame historyStackFrame = popFromHistory();
            if (historyStackFrame != null)
            {
                historyStackFrame.undo();
                if (historyStackFrame.addToUnsavedChanges()) unsavedChanges++;
            }

            if (KeystoneConfig.debugHistoryLog) logHistoryStack();
        });
    }
    public void redo()
    {
        Keystone.runOnMainThread(() ->
        {
            if (currentHistoryIndex < history.size() - 1)
            {
                currentHistoryIndex++;
                HistoryStackFrame historyStackFrame = history.get(currentHistoryIndex);

                historyStackFrame.redo();
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

    public void logHistoryStack()
    {
        for(int i = history.size() - 1; i >= 0; i--) history.get(i).debugLog(i - currentHistoryIndex);
        Keystone.LOGGER.info("");
    }
    //endregion
}
