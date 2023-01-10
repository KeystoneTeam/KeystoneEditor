package keystone.core.modules.selection;

import keystone.api.Keystone;
import keystone.api.WorldRegion;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.core.KeystoneGlobalState;
import keystone.core.client.Player;
import keystone.core.events.keystone.KeystoneHotbarEvents;
import keystone.core.events.keystone.KeystoneInputEvents;
import keystone.core.events.keystone.KeystoneLifecycleEvents;
import keystone.core.gui.hotbar.KeystoneHotbar;
import keystone.core.gui.hotbar.KeystoneHotbarSlot;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.history.entries.SelectionHistoryEntry;
import keystone.core.modules.mouse.MouseModule;
import keystone.core.renderer.Color4f;
import keystone.core.renderer.RenderBox;
import keystone.core.renderer.RendererFactory;
import keystone.core.renderer.overlay.ComplexOverlayRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
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
    private final List<SelectionBoundingBox> selectionBoxes;
    private SelectionHistoryEntry revertHistoryEntry;
    private ComplexOverlayRenderer highlightRenderer;
    private SelectionBoxRenderer selectionBoxRenderer;

    private Vec3i firstSelectionPoint;
    private boolean multiSelect;
    private boolean creatingSelection;

    public SelectionModule()
    {
        selectionBoxes = Collections.synchronizedList(new ArrayList<>());

        KeystoneInputEvents.MOUSE_CLICKED.register(this::onMouseClick);
        KeystoneInputEvents.START_MOUSE_DRAG.register(this::onMouseDragStart);
        KeystoneInputEvents.END_MOUSE_DRAG.register(this::onMouseDragEnd);
        KeystoneHotbarEvents.ALLOW_CHANGE.register(this::allowHotbarChange);
        KeystoneLifecycleEvents.SELECTION_CHANGED.register(this::onSelectionChanged);
    }
    
    //region Module Implementation
    @Override
    public void postInit()
    {
        historyModule = Keystone.getModule(HistoryModule.class);
        mouseModule = Keystone.getModule(MouseModule.class);
        revertHistoryEntry = new SelectionHistoryEntry(selectionBoxes);

        highlightRenderer = RendererFactory.createComplexOverlay(
                RendererFactory.createPolygonOverlay().buildFill(),
                RendererFactory.createWireframeOverlay().ignoreDepth().buildWireframe()
        );
        selectionBoxRenderer = new SelectionBoxRenderer();
    }
    @Override
    public void resetModule()
    {
        selectionBoxes.clear();
        revertHistoryEntry = new SelectionHistoryEntry(selectionBoxes);
        firstSelectionPoint = null;
        creatingSelection = false;
    }
    @Override
    public Collection<? extends SelectableCuboid> getSelectableBoxes() { return this.selectionBoxes; }
    //endregion
    //region Selection Methods
    public void deselect()
    {
        if (selectionBoxes.size() > 0)
        {
            selectionBoxes.clear();
            KeystoneLifecycleEvents.SELECTION_CHANGED.invoker().selectionChanged(selectionBoxes, false, true);
        }
    }
    public void setSelectionBoxes(List<SelectionBoundingBox> boxes, boolean createHistoryEntry)
    {
        selectionBoxes.clear();
        boxes.forEach(box -> selectionBoxes.add(box.clone()));
        KeystoneLifecycleEvents.SELECTION_CHANGED.invoker().selectionChanged(selectionBoxes, false, createHistoryEntry);
    }
    public WorldRegion[] buildRegions(boolean allowBlocksOutside, boolean splitIntersections)
    {
        List<SelectionBoundingBox> filterBoxes = selectionBoxes;
        if (splitIntersections)
        {
            NonIntersectingCuboidList<SelectionBoundingBox> splitList = new NonIntersectingCuboidList<>(((minX, minY, minZ, maxX, maxY, maxZ) -> new SelectionBoundingBox(new Vec3i(minX, minY, minZ), new Vec3i(maxX, maxY, maxZ))));
            splitList.addAll(selectionBoxes);
            splitList.finish();
            filterBoxes = splitList.getContents();
        }

        WorldRegion[] regions = new WorldRegion[filterBoxes.size()];
        for (int i = 0; i < regions.length; i++)
        {
            regions[i] = new WorldRegion(filterBoxes.get(i).getMin(), filterBoxes.get(i).getMax());
            regions[i].allowBlocksOutside = allowBlocksOutside;
        }
        return regions;
    }
    public void setSelections(List<BoundingBox> boxes)
    {
        selectionBoxes.clear();
        boxes.forEach(box -> selectionBoxes.add(SelectionBoundingBox.createFromBoundingBox(box)));
        KeystoneLifecycleEvents.SELECTION_CHANGED.invoker().selectionChanged(selectionBoxes, false, true);
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
            synchronized (this.selectionBoxes)
            {
                for (int i = 0; i < selectionBoxes.size(); i++)
                {
                    if (!creatingSelection || multiSelect || i == selectionBoxes.size() - 1) selectionBoxRenderer.render(context, selectionBoxes.get(i));
                }
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
            highlightRenderer.drawMode(ComplexOverlayRenderer.DrawMode.FILL).drawCuboid(box, Color4f.yellow.withAlpha(0.125f));
            highlightRenderer.drawMode(ComplexOverlayRenderer.DrawMode.WIREFRAME).drawCuboid(box, Color4f.yellow);
        }
    }
    //endregion
    //region Events
    private void onMouseClick(int button, int modifiers, double mouseX, double mouseY, boolean gui)
    {
        if (!Keystone.isEnabled() || MinecraftClient.getInstance().currentScreen != null || gui) return;

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
        if (!Keystone.isEnabled() || MinecraftClient.getInstance().currentScreen != null || gui) return;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            if (mouseModule.getSelectedFace() == null) startSelectionBox();
        }
    }
    public void onMouseDragEnd(int button, double mouseX, double mouseY, boolean gui)
    {
        if (!Keystone.isEnabled() || MinecraftClient.getInstance().currentScreen != null || gui) return;

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            if (mouseModule.getSelectedFace() == null) endSelectionBox();
        }
    }
    public boolean allowHotbarChange(KeystoneHotbarSlot previous, KeystoneHotbarSlot slot)
    {
        return !creatingSelection;
    }
    public void onSelectionChanged(List<SelectionBoundingBox> selections, boolean createdSelection, boolean createHistoryEntry)
    {
        if (!createHistoryEntry) return;
        addHistoryEntry();
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
            multiSelect = Screen.hasControlDown();
            selectionBoxes.add(SelectionBoundingBox.startNew(firstSelectionPoint));
        }
    }
    private void endSelectionBox()
    {
        if (!isEnabled()) return;

        if (creatingSelection)
        {
            if (!multiSelect)
            {
                SelectionBoundingBox selection = selectionBoxes.get(selectionBoxes.size() - 1);
                selectionBoxes.clear();
                selectionBoxes.add(selection);
            }

            firstSelectionPoint = null;
            creatingSelection = false;
            multiSelect = false;
            KeystoneLifecycleEvents.SELECTION_CHANGED.invoker().selectionChanged(selectionBoxes, true, true);
        }
    }
    public void addHistoryEntry()
    {
        historyModule.tryBeginHistoryEntry();

        SelectionHistoryEntry entry = new SelectionHistoryEntry(selectionBoxes);
        historyModule.pushToEntry(entry, revertHistoryEntry);
        revertHistoryEntry = entry;

        historyModule.tryEndHistoryEntry();
    }
    //endregion
}
