package keystone.core.modules.clipboard;

import keystone.api.Keystone;
import keystone.api.tools.FillTool;
import keystone.core.KeystoneGlobalState;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.blocks.BlocksModule;
import keystone.core.modules.clipboard.boxes.PasteBoundingBox;
import keystone.core.modules.clipboard.providers.PasteBoxProvider;
import keystone.core.modules.ghost_blocks.GhostBlocksModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.history.entries.PasteBoxHistoryEntry;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.modules.selection.boxes.SelectionBoundingBox;
import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.schematic.KeystoneSchematic;
import net.minecraft.block.Blocks;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClipboardModule implements IKeystoneModule
{
    private List<PasteBoundingBox> pasteBoxes;
    private BlocksModule blocksModule;
    private GhostBlocksModule ghostBlocksModule;

    public ClipboardModule()
    {
        pasteBoxes = new ArrayList<>();

        MinecraftForge.EVENT_BUS.addListener(this::onKeyPressed);
    }

    @Override
    public void postInit()
    {
        this.blocksModule = Keystone.getModule(BlocksModule.class);
        this.ghostBlocksModule = Keystone.getModule(GhostBlocksModule.class);
    }

    public List<PasteBoundingBox> getPasteBoxes() { return pasteBoxes; }
    public List<PasteBoundingBox> restorePasteBoxes(List<PasteBoundingBox> boxes)
    {
        List<PasteBoundingBox> old = new ArrayList<>();
        pasteBoxes.forEach(box -> old.add(box));

        clearPasteBoxes();
        boxes.forEach(box -> pasteBoxes.add(box));

        if (pasteBoxes.size() > 0)
        {
            KeystoneGlobalState.HideSelectionBoxes = true;
            KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.CLONE);
        }
        else
        {
            KeystoneGlobalState.HideSelectionBoxes = false;
            KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.SELECTION);
        }

        return old;
    }
    public void resetModule()
    {
        HistoryModule historyModule = Keystone.getModule(HistoryModule.class);
        historyModule.tryBeginHistoryEntry();
        historyModule.pushToEntry(new PasteBoxHistoryEntry(pasteBoxes));
        historyModule.tryEndHistoryEntry();

        clearPasteBoxes();
        KeystoneGlobalState.HideSelectionBoxes = false;
        KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.SELECTION);
    }

    @Override
    public boolean isEnabled()
    {
        return KeystoneHotbar.getSelectedSlot() == KeystoneHotbarSlot.CLONE;
    }
    @Override
    public IBoundingBoxProvider[] getBoundingBoxProviders()
    {
        return new IBoundingBoxProvider[] { new PasteBoxProvider(this) };
    }

    private void onKeyPressed(final InputEvent.KeyInputEvent event)
    {
        if (event.getAction() == GLFW.GLFW_PRESS)
        {
            if (event.getKey() == GLFW.GLFW_KEY_ENTER || event.getKey() == GLFW.GLFW_KEY_KP_ENTER)
            {
                if (pasteBoxes.size() > 0) paste();
            }
            else if (event.getModifiers() == GLFW.GLFW_MOD_CONTROL)
            {
                if (event.getKey() == GLFW.GLFW_KEY_X) cut();
                else if (event.getKey() == GLFW.GLFW_KEY_C) copy();
                else if (event.getKey() == GLFW.GLFW_KEY_V) paste();
            }
            else if (event.getKey() == GLFW.GLFW_KEY_R)
            {
                for (PasteBoundingBox pasteBox : pasteBoxes) pasteBox.cycleRotate();
            }
            else if (event.getKey() == GLFW.GLFW_KEY_M)
            {
                for (PasteBoundingBox pasteBox : pasteBoxes) pasteBox.cycleMirror();
            }
            else if (event.getKey() == GLFW.GLFW_KEY_ESCAPE) resetModule();
        }
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
            KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.CLONE);

            HistoryModule historyModule = Keystone.getModule(HistoryModule.class);
            historyModule.tryBeginHistoryEntry();
            historyModule.pushToEntry(new PasteBoxHistoryEntry(this.pasteBoxes));
            historyModule.tryEndHistoryEntry();

            clearPasteBoxes();
            for (SelectionBoundingBox selection : Keystone.getModule(SelectionModule.class).getSelectionBoundingBoxes())
            {
                pasteBoxes.add(PasteBoundingBox.create(selection.getMinCoords(), KeystoneSchematic.createFromSelection(selection, blocksModule)));
            }
            KeystoneGlobalState.HideSelectionBoxes = true;
        });
    }
    public void paste()
    {
        Keystone.runOnMainThread(() ->
        {
            HistoryModule historyModule = Keystone.getModule(HistoryModule.class);
            historyModule.tryBeginHistoryEntry();
            pasteBoxes.forEach(paste -> paste.paste());
            resetModule();
            historyModule.tryEndHistoryEntry();

            KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.SELECTION);
        });
    }
    public void clearPasteBoxes()
    {
        pasteBoxes.forEach(pasteBox -> ghostBlocksModule.releaseWorld(pasteBox.getGhostBlocks()));
        pasteBoxes.clear();
    }
}
