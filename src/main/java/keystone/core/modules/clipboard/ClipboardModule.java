package keystone.core.modules.clipboard;

import keystone.api.Keystone;
import keystone.api.tools.FillTool;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.blocks.BlocksModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.schematic_import.ImportModule;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.modules.selection.boxes.SelectionBoundingBox;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.common.models.Coords;
import keystone.core.schematic.KeystoneSchematic;
import net.minecraft.block.Blocks;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClipboardModule implements IKeystoneModule
{
    private BlocksModule blocksModule;
    private ImportModule importModule;

    private List<KeystoneSchematic> clipboard;

    public ClipboardModule()
    {
        this.clipboard = new ArrayList<>();
        MinecraftForge.EVENT_BUS.addListener(this::onKeyPressed);
    }

    //region Module Implementation
    @Override
    public void postInit()
    {
        this.blocksModule = Keystone.getModule(BlocksModule.class);
        this.importModule = Keystone.getModule(ImportModule.class);
    }
    @Override
    public boolean isEnabled()
    {
        return true;
    }
    @Override
    public IBoundingBoxProvider[] getBoundingBoxProviders()
    {
        return new IBoundingBoxProvider[0];
    }
    //endregion
    //region Event Handlers
    private void onKeyPressed(final InputEvent.KeyInputEvent event)
    {
        if (event.getAction() == GLFW.GLFW_PRESS)
        {
            if (event.getModifiers() == GLFW.GLFW_MOD_CONTROL)
            {
                if (event.getKey() == GLFW.GLFW_KEY_X) cut();
                else if (event.getKey() == GLFW.GLFW_KEY_C) copy();
                else if (event.getKey() == GLFW.GLFW_KEY_V) paste();
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
            Keystone.runTool(new FillTool(Blocks.AIR.defaultBlockState()));
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
                clipboard.add(KeystoneSchematic.createFromSelection(selection, blocksModule));
            }
        });
    }
    public void paste()
    {
        if (clipboard.size() > 0)
        {
            for (KeystoneSchematic schematic : clipboard)
            {
                Coords minPosition = Player.getHighlightedBlock();
                this.importModule.importSchematic(schematic, minPosition);
            }
        }
    }
}
