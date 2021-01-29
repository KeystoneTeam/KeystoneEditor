package keystone.modules.paste;

import keystone.api.Keystone;
import keystone.api.schematic.KeystoneSchematic;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.gui.screens.hotbar.KeystoneHotbar;
import keystone.modules.IKeystoneModule;
import keystone.modules.history.HistoryModule;
import keystone.modules.history.entries.PasteHistoryEntry;
import keystone.modules.paste.boxes.PasteBoundingBox;
import keystone.modules.paste.providers.PasteBoxProvider;
import keystone.modules.selection.SelectionModule;
import keystone.modules.selection.boxes.SelectionBoundingBox;
import keystone.modules.world_cache.WorldCacheModule;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class CloneModule implements IKeystoneModule
{
    private List<PasteBoundingBox> pasteBoxes;

    public CloneModule()
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
        }
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
