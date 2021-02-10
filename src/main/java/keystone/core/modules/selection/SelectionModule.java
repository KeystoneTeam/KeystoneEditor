package keystone.core.modules.selection;

import keystone.api.Keystone;
import keystone.api.SelectionBox;
import keystone.core.events.KeystoneInputEvent;
import keystone.core.events.KeystoneSelectionChangedEvent;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.common.models.Coords;
import keystone.core.renderer.common.models.DimensionId;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.history.entries.SelectionHistoryEntry;
import keystone.core.modules.mouse.MouseModule;
import keystone.core.modules.clipboard.ClipboardModule;
import keystone.core.modules.selection.boxes.SelectionBoundingBox;
import keystone.core.modules.selection.providers.HighlightBoxProvider;
import keystone.core.modules.selection.providers.SelectionBoxProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class SelectionModule implements IKeystoneModule
{
    public static boolean HideSelectionBoxes = false;

    private final MouseModule mouseModule;
    private List<SelectionBoundingBox> selectionBoxes;
    private IBoundingBoxProvider[] renderProviders;

    private Coords firstSelectionPoint;
    private boolean creatingSelection;

    public SelectionModule()
    {
        MinecraftForge.EVENT_BUS.register(this);

        mouseModule = Keystone.getModule(MouseModule.class);
        selectionBoxes = new ArrayList<>();
        renderProviders = new IBoundingBoxProvider[]
        {
                new SelectionBoxProvider(this),
                new HighlightBoxProvider()
        };
    }

    public List<SelectionBoundingBox> getSelectionBoundingBoxes()
    {
        return selectionBoxes;
    }
    public int getSelectionBoxCount()
    {
        int count = selectionBoxes.size();
        if (creatingSelection) count--;
        return count;
    }
    public void onCancelPressed()
    {
        ClipboardModule paste = Keystone.getModule(ClipboardModule.class);
        if (paste.getPasteBoxes().size() > 0) paste.clearPasteBoxes();
        else deselect();
    }
    public void deselect()
    {
        if (selectionBoxes.size() > 0)
        {
            Keystone.getModule(HistoryModule.class).pushToHistory(new SelectionHistoryEntry(selectionBoxes, true));
            selectionBoxes.clear();
            MinecraftForge.EVENT_BUS.post(new KeystoneSelectionChangedEvent(selectionBoxes));
        }
    }
    public List<SelectionBoundingBox> restoreSelectionBoxes(List<SelectionBoundingBox> boxes)
    {
        List<SelectionBoundingBox> old = new ArrayList<>();
        selectionBoxes.forEach(box -> old.add(box.clone()));

        selectionBoxes.clear();
        boxes.forEach(box -> selectionBoxes.add(box.clone()));

        return old;
    }

    public SelectionBox[] buildSelectionBoxes(World world)
    {
        SelectionBox[] boxes = new SelectionBox[selectionBoxes.size()];
        for (int i = 0; i < boxes.length; i++) boxes[i] = new SelectionBox(selectionBoxes.get(i).getMinCoords(), selectionBoxes.get(i).getMaxCoords(), world);
        return boxes;
    }

    //region Getters
    public boolean isCreatingSelection()
    {
        return creatingSelection;
    }
    //endregion
    //region Rendering
    @Override
    public boolean isEnabled()
    {
        return KeystoneHotbar.getSelectedSlot() == KeystoneHotbarSlot.SELECTION;
    }
    @Override
    public IBoundingBoxProvider[] getBoundingBoxProviders()
    {
        return renderProviders;
    }
    @Override
    public void prepareRender(float partialTicks, DimensionId dimensionId)
    {
        if (creatingSelection)
        {
            if (selectionBoxes.size() == 0) creatingSelection = false;
            else selectionBoxes.get(selectionBoxes.size() - 1).setCorner2(Player.getHighlightedBlock());
        }
    }
    //endregion
    //region Events
    @SubscribeEvent
    public void onMouseClick(final KeystoneInputEvent.MouseClickEvent event)
    {
        if (!Keystone.isActive() || Minecraft.getInstance().currentScreen != null || event.gui) return;

        if (event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            if (mouseModule.getSelectedFace() == null)
            {
                if (!creatingSelection) startSelectionBox();
                else endSelectionBox();
            }
        }
    }
    @SubscribeEvent
    public void onMouseDragStart(final KeystoneInputEvent.MouseDragStartEvent event)
    {
        if (!Keystone.isActive() || Minecraft.getInstance().currentScreen != null || event.gui) return;

        if (event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            if (mouseModule.getSelectedFace() == null) startSelectionBox();
        }
    }
    @SubscribeEvent
    public void onMouseDragEnd(final KeystoneInputEvent.MouseDragEndEvent event)
    {
        if (!Keystone.isActive() || Minecraft.getInstance().currentScreen != null || event.gui) return;

        if (event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            if (mouseModule.getSelectedFace() == null) endSelectionBox();
        }
    }
    //endregion
    //region Controls
    private void startSelectionBox()
    {
        if (!isEnabled()) return;

        if (!creatingSelection && !KeystoneOverlayHandler.MouseOverGUI)
        {
            firstSelectionPoint = Player.getHighlightedBlock();
            creatingSelection = true;
            selectionBoxes.add(SelectionBoundingBox.startNew(firstSelectionPoint));
        }
    }
    private void endSelectionBox()
    {
        if (!isEnabled()) return;

        if (creatingSelection)
        {
            Keystone.getModule(HistoryModule.class).pushToHistory(new SelectionHistoryEntry(selectionBoxes, false));
            MinecraftForge.EVENT_BUS.post(new KeystoneSelectionChangedEvent(selectionBoxes));
            firstSelectionPoint = null;
            creatingSelection = false;
        }
    }
    //endregion
}
