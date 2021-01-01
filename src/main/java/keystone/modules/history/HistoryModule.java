package keystone.modules.history;

import keystone.api.Keystone;
import keystone.modules.IKeystoneModule;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class HistoryModule implements IKeystoneModule
{
    private List<IHistoryEntry> history = new ArrayList<>();
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

    //region History
    public void pushToHistory(IHistoryEntry historyEntry)
    {
        if (currentHistoryIndex < history.size() - 1) for (int i = history.size() - 1; i > currentHistoryIndex; i--) history.remove(i);
        history.add(historyEntry);
        currentHistoryIndex++;

        if (historyEntry.addToUnsavedChanges()) unsavedChanges++;
    }
    private IHistoryEntry popFromHistory()
    {
        if (currentHistoryIndex >= 0)
        {
            IHistoryEntry ret = history.get(currentHistoryIndex);
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
        IHistoryEntry historyEntry = popFromHistory();
        if (historyEntry != null)
        {
            historyEntry.undo();
            if (historyEntry.addToUnsavedChanges()) unsavedChanges++;
        }
    }
    public void redo()
    {
        if (currentHistoryIndex < history.size() - 1)
        {
            currentHistoryIndex++;
            IHistoryEntry historyEntry = history.get(currentHistoryIndex);

            historyEntry.redo();
            if (historyEntry.addToUnsavedChanges()) unsavedChanges++;
        }
    }

    public void logHistoryStack()
    {
        for(int i = history.size() - 1; i >= 0; i--)
        {
            Keystone.LOGGER.info(i > currentHistoryIndex ? "*" + history.get(i).getClass().getSimpleName() : history.get(i).getClass().getSimpleName());
        }
        Keystone.LOGGER.info("");
    }
    //endregion
}
