package keystone.core.modules.clipboard;

import keystone.api.Keystone;
import keystone.api.tools.FillTool;
import keystone.core.schematic.KeystoneSchematic;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.history.entries.PasteHistoryEntry;
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
    public void setPasteBoxes(PasteBoundingBox...  boxes)
    {
        clearPasteBoxes();
        for (PasteBoundingBox box : boxes) pasteBoxes.add(box);
        if (pasteBoxes.size() > 0) SelectionModule.HideSelectionBoxes = true;
    }
    public void setPasteBoxes(Iterable<PasteBoundingBox> boxes)
    {
        clearPasteBoxes();
        for (PasteBoundingBox box : boxes) pasteBoxes.add(box);
        if (pasteBoxes.size() > 0) SelectionModule.HideSelectionBoxes = true;
    }
    public void clearPasteBoxes()
    {
        pasteBoxes.clear();
        SelectionModule.HideSelectionBoxes = false;
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
        }
    }

    public void cut()
    {
        copy();
        Keystone.runTool(new FillTool(Blocks.AIR.getDefaultState()));
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

        pasteBoxes.clear();
        for (SelectionBoundingBox selection : Keystone.getModule(SelectionModule.class).getSelectionBoundingBoxes()) pasteBoxes.add(PasteBoundingBox.create(selection.getMinCoords(), KeystoneSchematic.createFromSelection(selection, world)));
        SelectionModule.HideSelectionBoxes = true;
    }
    public void paste()
    {
        World world = Keystone.getModule(WorldCacheModule.class).getDimensionWorld(Player.getDimensionId());
        if (world == null)
        {
            Keystone.LOGGER.error("Trying to paste when there is no loaded world for dimension '" + Player.getDimensionId().getDimensionType().getRegistryName() + "'!");
            return;
        }

        PasteHistoryEntry historyEntry = new PasteHistoryEntry(world, pasteBoxes);
        pasteBoxes.forEach(paste -> paste.paste(world));
        historyEntry.updateSelectionBuffers();

        Keystone.getModule(HistoryModule.class).pushToHistory(historyEntry);
        clearPasteBoxes();

        KeystoneHotbar.setSelectedSlot(KeystoneHotbarSlot.SELECTION);
    }
}
