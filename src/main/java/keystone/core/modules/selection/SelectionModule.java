package keystone.core.modules.selection;

import keystone.api.Keystone;
import keystone.api.WorldRegion;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.core.KeystoneGlobalState;
import keystone.core.client.Player;
import keystone.core.events.keystone.KeystoneHotbarEvents;
import keystone.core.events.keystone.KeystoneInputEvents;
import keystone.core.events.keystone.KeystoneLifecycleEvents;
import keystone.core.events.minecraft.InputEvents;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.history.entries.SelectionHistoryEntry;
import keystone.core.modules.mouse.MouseModule;
import keystone.core.renderer.Color4f;
import keystone.core.renderer.RenderBox;
import keystone.core.renderer.RendererFactory;
import keystone.core.renderer.overlay.ComplexOverlayRenderer;
import keystone.core.renderer.shapes.SelectableBoundingBox;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SelectionModule implements IKeystoneModule
{
    private HistoryModule historyModule;
    private MouseModule mouseModule;
    private List<SelectionBoundingBox> selectionBoxes;
    private ComplexOverlayRenderer highlightRenderer;
    private SelectionBoxRenderer selectionBoxRenderer;

    private Vec3i firstSelectionPoint;
    private boolean creatingSelection;

    public SelectionModule()
    {
        selectionBoxes = Collections.synchronizedList(new ArrayList<>());

        InputEvents.KEY_PRESSED.register(this::onKeyInput);
        KeystoneInputEvents.MOUSE_CLICKED.register(this::onMouseClick);
        KeystoneInputEvents.START_MOUSE_DRAG.register(this::onMouseDragStart);
        KeystoneInputEvents.END_MOUSE_DRAG.register(this::onMouseDragEnd);
        KeystoneHotbarEvents.ALLOW_CHANGE.register(this::allowHotbarChange);
    }
    @Override
    public void postInit()
    {
        historyModule = Keystone.getModule(HistoryModule.class);
        mouseModule = Keystone.getModule(MouseModule.class);
        highlightRenderer = RendererFactory.createComplexOverlay(
                RendererFactory.createWorldspaceOverlay().buildFill(),
                RendererFactory.createWorldspaceOverlay().ignoreDepth().buildWireframe()
        );
        selectionBoxRenderer = new SelectionBoxRenderer();
    }
    @Override
    public void resetModule()
    {
        selectionBoxes.clear();
        firstSelectionPoint = null;
        creatingSelection = false;
    }
    @Override
    public Collection<? extends SelectableBoundingBox> getSelectableBoxes() { return this.selectionBoxes; }
    //region Selection Methods
    public void deselect()
    {
        if (selectionBoxes.size() > 0)
        {
            historyModule.beginHistoryEntry();
            historyModule.pushToEntry(new SelectionHistoryEntry(selectionBoxes, true));
            historyModule.endHistoryEntry();

            selectionBoxes.clear();
            KeystoneLifecycleEvents.SELECTION_CHANGED.invoker().selectionChanged(selectionBoxes, false);
        }
    }
    public List<SelectionBoundingBox> restoreSelectionBoxes(List<SelectionBoundingBox> boxes)
    {
        List<SelectionBoundingBox> old = new ArrayList<>();
        selectionBoxes.forEach(box -> old.add(box.clone()));

        selectionBoxes.clear();
        boxes.forEach(box -> selectionBoxes.add(box.clone()));

        KeystoneLifecycleEvents.SELECTION_CHANGED.invoker().selectionChanged(selectionBoxes, false);
        return old;
    }
    public WorldRegion[] buildRegions(boolean allowBlocksOutside)
    {
        WorldRegion[] regions = new WorldRegion[selectionBoxes.size()];
        for (int i = 0; i < regions.length; i++)
        {
            regions[i] = new WorldRegion(selectionBoxes.get(i).getMin(), selectionBoxes.get(i).getMax());
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
        KeystoneLifecycleEvents.SELECTION_CHANGED.invoker().selectionChanged(selectionBoxes, false);
        historyModule.tryEndHistoryEntry();
    }
    //endregion
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
    public void preRender(WorldRenderContext context)
    {
        if (creatingSelection)
        {
            if (selectionBoxes.size() == 0) creatingSelection = false;
            else selectionBoxes.get(selectionBoxes.size() - 1).setCorner2(Player.getHighlightedBlock());
        }
    }
    @Override
    public void alwaysRender(WorldRenderContext context)
    {
        // Selection Boxes
        if (!KeystoneGlobalState.HideSelectionBoxes)
        {
            for (SelectionBoundingBox box : selectionBoxes)
            {
                selectionBoxRenderer.render(context, box);
            }
        }
    }
    @Override
    public void renderWhenEnabled(WorldRenderContext context)
    {
        // Block Highlight
        if (isEnabled() && !creatingSelection && mouseModule.getSelectedFace() == null)
        {
            RenderBox box = new RenderBox(Player.getHighlightedBlock(), Player.getHighlightedBlock());
            highlightRenderer.drawMode(ComplexOverlayRenderer.DrawMode.FILL).drawCuboid(box, Color4f.yellow.withAlpha(0.0625f));
            highlightRenderer.drawMode(ComplexOverlayRenderer.DrawMode.WIREFRAME).drawCuboid(box, Color4f.yellow);
        }
    }
    //endregion
    //region Events
    private void onKeyInput(int key, int action, int scancode, int modifiers)
    {
        if (!Keystone.isActive() || MinecraftClient.getInstance().currentScreen != null) return;

        if (key == GLFW.GLFW_KEY_D && modifiers == GLFW.GLFW_MOD_CONTROL)
        {
            GameOptions settings = MinecraftClient.getInstance().options;
            for (KeyBinding keyBinding : settings.allKeys)
            {
                if (keyBinding.isPressed())
                {
                    if (keyBinding.getDefaultKey().getCode() != GLFW.GLFW_KEY_D &&
                            keyBinding.getDefaultKey().getCode() != GLFW.GLFW_KEY_LEFT_CONTROL &&
                            keyBinding.getDefaultKey().getCode() != GLFW.GLFW_KEY_RIGHT_CONTROL) return;
                }
            }
            deselect();
        }
    }
    private void onMouseClick(int button, int modifiers, double mouseX, double mouseY, boolean gui)
    {
        if (!Keystone.isActive() || MinecraftClient.getInstance().currentScreen != null || gui) return;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            if (mouseModule.getSelectedFace() == null)
            {
                if (!creatingSelection) startSelectionBox();
                else endSelectionBox();
            }
        }
    }
    private void onMouseDragStart(int button, double mouseX, double mouseY, boolean gui)
    {
        if (!Keystone.isActive() || MinecraftClient.getInstance().currentScreen != null || gui) return;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            if (mouseModule.getSelectedFace() == null) startSelectionBox();
        }
    }
    public void onMouseDragEnd(int button, double mouseX, double mouseY, boolean gui)
    {
        if (!Keystone.isActive() || MinecraftClient.getInstance().currentScreen != null || gui) return;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            if (mouseModule.getSelectedFace() == null) endSelectionBox();
        }
    }
    public boolean allowHotbarChange(KeystoneHotbarSlot previous, KeystoneHotbarSlot slot)
    {
        return !creatingSelection;
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
            KeystoneLifecycleEvents.SELECTION_CHANGED.invoker().selectionChanged(selectionBoxes, true);
        }
    }
    //endregion
}
