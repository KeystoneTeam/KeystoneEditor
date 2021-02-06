package keystone.modules.selection;

import keystone.api.Keystone;
import keystone.api.SelectionBox;
import keystone.core.events.KeystoneInputEvent;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.common.models.Coords;
import keystone.core.renderer.common.models.DimensionId;
import keystone.gui.KeystoneOverlayHandler;
import keystone.gui.screens.hotbar.KeystoneHotbar;
import keystone.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.modules.IKeystoneModule;
import keystone.modules.history.HistoryModule;
import keystone.modules.history.entries.SelectionHistoryEntry;
import keystone.modules.mouse.MouseModule;
import keystone.modules.paste.CloneModule;
import keystone.modules.selection.boxes.SelectionBoundingBox;
import keystone.modules.selection.providers.HighlightBoxProvider;
import keystone.modules.selection.providers.SelectionBoxProvider;
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
        CloneModule paste = Keystone.getModule(CloneModule.class);
        if (paste.getPasteBoxes().size() > 0) paste.clearPasteBoxes();
        else clearSelectionBoxes();
    }
    public void clearSelectionBoxes()
    {
        if (selectionBoxes.size() > 0)
        {
            Keystone.getModule(HistoryModule.class).pushToHistory(new SelectionHistoryEntry(selectionBoxes, true));
            selectionBoxes.clear();
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
        if (!Keystone.isActive() || Minecraft.getInstance().currentScreen != null) return;

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
        if (!Keystone.isActive() || Minecraft.getInstance().currentScreen != null) return;

        if (event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            if (mouseModule.getSelectedFace() == null) startSelectionBox();
        }
    }
    @SubscribeEvent
    public void onMouseDragEnd(final KeystoneInputEvent.MouseDragEndEvent event)
    {
        if (!Keystone.isActive() || Minecraft.getInstance().currentScreen != null) return;

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
            firstSelectionPoint = null;
            creatingSelection = false;
        }
    }
    //endregion
}
