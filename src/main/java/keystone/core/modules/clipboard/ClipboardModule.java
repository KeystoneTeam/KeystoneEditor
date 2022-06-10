package keystone.core.modules.clipboard;

import keystone.api.Keystone;
import keystone.api.enums.RetrievalMode;
import keystone.api.tools.FillTool;
import keystone.core.client.Player;
import keystone.core.events.minecraft.InputEvents;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.schematic_import.ImportModule;
import keystone.core.modules.selection.SelectionBoundingBox;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.modules.world.WorldModifierModules;
import keystone.core.schematic.KeystoneSchematic;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClipboardModule implements IKeystoneModule
{
    private WorldModifierModules worldModifiers;
    private ImportModule importModule;

    private List<KeystoneSchematic> clipboard;

    public ClipboardModule()
    {
        this.clipboard = new ArrayList<>();
        InputEvents.KEY_PRESSED.register(this::onKeyPressed);
    }

    //region Module Implementation
    @Override
    public boolean isEnabled()
    {
        return true;
    }
    @Override
    public void postInit()
    {
        this.worldModifiers = new WorldModifierModules();
        this.importModule = Keystone.getModule(ImportModule.class);
    }
    //endregion
    //region Event Handlers
    private void onKeyPressed(int key, int action, int scancode, int modifiers)
    {
        if (action == GLFW.GLFW_PRESS)
        {
            if (modifiers == GLFW.GLFW_MOD_CONTROL)
            {
                if (key == GLFW.GLFW_KEY_X) cut();
                else if (key == GLFW.GLFW_KEY_C) copy();
                else if (key == GLFW.GLFW_KEY_V) paste();
            }
        }
    }
    //endregion

    public List<KeystoneSchematic> getClipboard()
    {
        return clipboard;
    }
    public List<KeystoneSchematic> restoreClipboard(List<KeystoneSchematic> newClipboard)
    {
        List<KeystoneSchematic> old = new ArrayList<>();
        clipboard.forEach(schematic -> old.add(schematic));

        clearClipboard();
        newClipboard.forEach(box -> clipboard.add(box));

        return old;
    }

    public void clearClipboard()
    {
        clipboard.clear();
    }

    public void cut()
    {
        Keystone.runOnMainThread(() ->
        {
            HistoryModule historyModule = Keystone.getModule(HistoryModule.class);
            historyModule.tryBeginHistoryEntry();
            copy();
            Keystone.runInternalFilters(new FillTool(Blocks.AIR.getDefaultState()));
            historyModule.tryEndHistoryEntry();
        });
    }
    public void copy()
    {
        Keystone.runOnMainThread(() ->
        {
            clearClipboard();
            for (SelectionBoundingBox selection : Keystone.getModule(SelectionModule.class).getSelectionBoundingBoxes())
            {
                clipboard.add(KeystoneSchematic.createFromSelection(selection, worldModifiers, RetrievalMode.ORIGINAL, Blocks.STRUCTURE_VOID.getDefaultState()));
            }
        });
    }
    public void paste()
    {
        if (clipboard.size() > 0)
        {
            for (KeystoneSchematic schematic : clipboard)
            {
                Vec3i minPosition = Player.getHighlightedBlock();
                this.importModule.importSchematic(schematic, minPosition, true, true);
            }
        }
    }
}
