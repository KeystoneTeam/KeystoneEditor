package keystone.core.modules.selection;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.api.WorldRegion;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.core.KeystoneGlobalState;
import keystone.core.events.KeystoneHotbarEvent;
import keystone.core.events.KeystoneInputEvent;
import keystone.core.events.KeystoneSelectionChangedEvent;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.history.entries.SelectionHistoryEntry;
import keystone.core.modules.mouse.MouseModule;
import keystone.core.modules.selection.boxes.SelectionBoundingBox;
import keystone.core.modules.selection.providers.HighlightBoxProvider;
import keystone.core.modules.selection.providers.SelectionBoxProvider;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.common.models.Coords;
import keystone.core.renderer.common.models.DimensionId;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class SelectionModule implements IKeystoneModule
{
    private HistoryModule historyModule;
    private MouseModule mouseModule;
    private List<SelectionBoundingBox> selectionBoxes;
    private IBoundingBoxProvider[] renderProviders;

    private Coords firstSelectionPoint;
    private boolean creatingSelection;

    public SelectionModule()
    {
        MinecraftForge.EVENT_BUS.register(this);

        selectionBoxes = new ArrayList<>();
        renderProviders = new IBoundingBoxProvider[]
        {
                new SelectionBoxProvider(this),
                new HighlightBoxProvider()
        };
    }
    @Override
    public void postInit()
    {
        historyModule = Keystone.getModule(HistoryModule.class);
        mouseModule = Keystone.getModule(MouseModule.class);
    }
    @Override
    public void resetModule()
    {
        selectionBoxes.clear();
        firstSelectionPoint = null;
        creatingSelection = false;
    }

    public void deselect()
    {
        if (selectionBoxes.size() > 0)
        {
            historyModule.beginHistoryEntry();
            historyModule.pushToEntry(new SelectionHistoryEntry(selectionBoxes, true));
            historyModule.endHistoryEntry();

            selectionBoxes.clear();
            MinecraftForge.EVENT_BUS.post(new KeystoneSelectionChangedEvent(selectionBoxes, false));
        }
    }
    public List<SelectionBoundingBox> restoreSelectionBoxes(List<SelectionBoundingBox> boxes)
    {
        List<SelectionBoundingBox> old = new ArrayList<>();
        selectionBoxes.forEach(box -> old.add(box.clone()));

        selectionBoxes.clear();
        boxes.forEach(box -> selectionBoxes.add(box.clone()));

        MinecraftForge.EVENT_BUS.post(new KeystoneSelectionChangedEvent(selectionBoxes, false));
        return old;
    }

    public WorldRegion[] buildRegions(boolean allowBlocksOutside)
    {
        WorldRegion[] regions = new WorldRegion[selectionBoxes.size()];
        for (int i = 0; i < regions.length; i++)
        {
            regions[i] = new WorldRegion(selectionBoxes.get(i).getMinCoords(), selectionBoxes.get(i).getMaxCoords());
            regions[i].allowBlocksOutside = allowBlocksOutside;
        }
        return regions;
    }

    public void setSelections(List<BoundingBox> boxes)
    {
        historyModule.tryBeginHistoryEntry();
        historyModule.pushToEntry(new SelectionHistoryEntry(selectionBoxes, true));
        selectionBoxes.clear();
        boxes.forEach(box -> selectionBoxes.add(SelectionBoundingBox.createFromBoundingBox(box)));
        MinecraftForge.EVENT_BUS.post(new KeystoneSelectionChangedEvent(selectionBoxes, false));
        historyModule.tryEndHistoryEntry();
    }

    //region Getters
    public boolean isCreatingSelection()
    {
        return creatingSelection;
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
    public void preRender(MatrixStack stack, float partialTicks, DimensionId dimensionId)
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
    public void onKeyInput(final InputEvent.KeyInputEvent event)
    {
        if (!Keystone.isActive() || Minecraft.getInstance().screen != null) return;

        if (event.getKey() == GLFW.GLFW_KEY_D && event.getModifiers() == GLFW.GLFW_MOD_CONTROL)
        {
            GameSettings settings = Minecraft.getInstance().options;
            for (KeyBinding keyBinding : settings.keyMappings)
            {
                if (keyBinding.isDown())
                {
                    if (keyBinding.getKey().getValue() != GLFW.GLFW_KEY_D &&
                            keyBinding.getKey().getValue() != GLFW.GLFW_KEY_LEFT_CONTROL &&
                            keyBinding.getKey().getValue() != GLFW.GLFW_KEY_RIGHT_CONTROL) return;
                }
            }
            deselect();
        }
    }
    @SubscribeEvent
    public void onMouseClick(final KeystoneInputEvent.MouseClickEvent event)
    {
        if (!Keystone.isActive() || Minecraft.getInstance().screen != null || event.gui) return;

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
        if (!Keystone.isActive() || Minecraft.getInstance().screen != null || event.gui) return;

        if (event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            if (mouseModule.getSelectedFace() == null) startSelectionBox();
        }
    }
    @SubscribeEvent
    public void onMouseDragEnd(final KeystoneInputEvent.MouseDragEndEvent event)
    {
        if (!Keystone.isActive() || Minecraft.getInstance().screen != null || event.gui) return;

        if (event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            if (mouseModule.getSelectedFace() == null) endSelectionBox();
        }
    }
    @SubscribeEvent
    public void onHotbarChanged(final KeystoneHotbarEvent event)
    {
        if (creatingSelection) event.setCanceled(true);
    }
    //endregion
    //region Controls
    private void startSelectionBox()
    {
        if (!isEnabled()) return;

        if (!creatingSelection && !KeystoneGlobalState.MouseOverGUI)
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
            historyModule.beginHistoryEntry();
            historyModule.pushToEntry(new SelectionHistoryEntry(selectionBoxes, false));
            historyModule.endHistoryEntry();

            firstSelectionPoint = null;
            creatingSelection = false;
            MinecraftForge.EVENT_BUS.post(new KeystoneSelectionChangedEvent(selectionBoxes, true));
        }
    }
    //endregion
}
