package keystone.core.modules.clipboard;

import keystone.api.Keystone;
import keystone.api.tools.FillTool;
import keystone.core.KeystoneGlobalState;
import keystone.core.events.KeystoneSelectionChangedEvent;
import keystone.core.modules.history.entries.PasteBoxHistoryEntry;
import keystone.core.schematic.KeystoneSchematic;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.clipboard.boxes.PasteBoundingBox;
import keystone.core.modules.clipboard.providers.PasteBoxProvider;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.modules.selection.boxes.SelectionBoundingBox;
import keystone.core.modules.world_cache.WorldCacheModule;
import net.minecraft.block.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.glfw.GLFW;

import javax.swing.text.AsyncBoxView;
import java.util.ArrayList;
import java.util.List;

public class ClipboardModule implements IKeystoneModule
{
    private List<PasteBoundingBox> pasteBoxes;

    public ClipboardModule()
    {
        pasteBoxes = new ArrayList<>();

        MinecraftForge.EVENT_BUS.addListener(this::onKeyPressed);
    }

    public List<PasteBoundingBox> getPasteBoxes() { return pasteBoxes; }
    public List<PasteBoundingBox> restorePasteBoxes(List<PasteBoundingBox> boxes)
    {
        List<PasteBoundingBox> old = new ArrayList<>();
        pasteBoxes.forEach(box -> old.add(box.clone()));

        pasteBoxes.clear();
        boxes.forEach(box -> pasteBoxes.add(box.clone()));

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
    public void clearPasteBoxes()
    {
        HistoryModule historyModule = Keystone.getModule(HistoryModule.class);
        historyModule.tryBeginHistoryEntry();
        historyModule.pushToEntry(new PasteBoxHistoryEntry(pasteBoxes));
        historyModule.tryEndHistoryEntry();

        pasteBoxes.clear();
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
            else if (event.getKey() == GLFW.GLFW_KEY_ESCAPE) clearPasteBoxes();
        }
    }

    public void cut()
    {
        Keystone.runOnMainThread(() ->
        {
            HistoryModule historyModule = Keystone.getModule(HistoryModule.class);
            historyModule.tryBeginHistoryEntry();
            copy();
            Keystone.runTool(new FillTool(Blocks.AIR.getDefaultState()));
            historyModule.tryEndHistoryEntry();
        });
    }
    public void copy()
    {
        KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.CLONE);

        World world = Keystone.getModule(WorldCacheModule.class).getDimensionWorld(Player.getDimensionId());
        if (world == null)
        {
            Keystone.LOGGER.error("Trying to paste when there is no loaded world for dimension '" + Player.getDimensionId().getDimensionType().getRegistryName() + "'!");
            return;
        }

        HistoryModule historyModule = Keystone.getModule(HistoryModule.class);
        historyModule.tryBeginHistoryEntry();
        historyModule.pushToEntry(new PasteBoxHistoryEntry(this.pasteBoxes));
        historyModule.tryEndHistoryEntry();

        pasteBoxes.clear();
        for (SelectionBoundingBox selection : Keystone.getModule(SelectionModule.class).getSelectionBoundingBoxes()) pasteBoxes.add(PasteBoundingBox.create(selection.getMinCoords(), KeystoneSchematic.createFromSelection(selection, world)));
        KeystoneGlobalState.HideSelectionBoxes = true;
    }
    public void paste()
    {
        World world = Keystone.getModule(WorldCacheModule.class).getDimensionWorld(Player.getDimensionId());
        if (world == null)
        {
            Keystone.LOGGER.error("Trying to paste when there is no loaded world for dimension '" + Player.getDimensionId().getDimensionType().getRegistryName() + "'!");
            return;
        }

        HistoryModule historyModule = Keystone.getModule(HistoryModule.class);
        historyModule.tryBeginHistoryEntry();
        pasteBoxes.forEach(paste -> paste.paste(world));
        clearPasteBoxes();
        historyModule.tryEndHistoryEntry();

        KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.SELECTION);
    }
}
