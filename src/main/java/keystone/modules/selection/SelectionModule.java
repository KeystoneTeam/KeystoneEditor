package keystone.modules.selection;

import keystone.api.Keystone;
import keystone.api.SelectionBox;
import keystone.core.renderer.client.ClientRenderer;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.common.models.Coords;
import keystone.core.renderer.common.models.DimensionId;
import keystone.core.renderer.common.models.SelectableBoundingBox;
import keystone.modules.IKeystoneModule;
import keystone.modules.history.HistoryModule;
import keystone.modules.history.entries.SelectionHistoryEntry;
import keystone.modules.paste.PasteModule;
import keystone.modules.selection.boxes.SelectionBoundingBox;
import keystone.modules.selection.providers.HighlightBoxProvider;
import keystone.modules.selection.providers.SelectionBoxProvider;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class SelectionModule implements IKeystoneModule
{
    public static boolean HideSelectionBoxes = false;

    private List<SelectionBoundingBox> selectionBoxes;
    private IBoundingBoxProvider[] renderProviders;

    private SelectedFace selectedFace;
    private Coords firstSelectionPoint;
    private boolean creatingSelection;
    private boolean draggingBox;

    public SelectionModule()
    {
        MinecraftForge.EVENT_BUS.register(this);

        draggingBox = false;

        selectionBoxes = new ArrayList<>();
        renderProviders = new IBoundingBoxProvider[]
        {
                new SelectionBoxProvider(this),
                new HighlightBoxProvider()
        };
    }

    public SelectedFace getSelectedFace() { return selectedFace; }
    public List<SelectionBoundingBox> getSelectionBoundingBoxes()
    {
        return selectionBoxes;
    }
    public void onCancelPressed()
    {
        PasteModule paste = Keystone.getModule(PasteModule.class);
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

    public SelectionBox[] buildSelectionBoxes(World world)
    {
        SelectionBox[] boxes = new SelectionBox[selectionBoxes.size()];
        for (int i = 0; i < boxes.length; i++) boxes[i] = new SelectionBox(selectionBoxes.get(i).getMinCoords(), selectionBoxes.get(i).getMaxCoords(), world);
        return boxes;
    }
    public List<SelectionBoundingBox> restoreSelectionBoxes(List<SelectionBoundingBox> boxes)
    {
        List<SelectionBoundingBox> old = new ArrayList<>();
        selectionBoxes.forEach(box -> old.add(box.clone()));

        selectionBoxes.clear();
        boxes.forEach(box -> selectionBoxes.add(box.clone()));

        return old;
    }

    //region Rendering
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
            selectedFace = null;
            selectionBoxes.get(selectionBoxes.size() - 1).setCorner2(Player.getHighlightedBlock());
        }
        else
        {
            if (!draggingBox)
            {
                selectedFace = null;
                ClientRenderer.getBoundingBoxes(Player.getDimensionId()).forEachOrdered(box ->
                {
                    if (box instanceof SelectableBoundingBox)
                    {
                        SelectableBoundingBox selectable = (SelectableBoundingBox)box;
                        SelectedFace face = selectable.getSelectedFace();
                        if (face != null && selectedFace != null)
                        {
                            if (selectedFace.getDistance() > face.getDistance()) selectedFace = face;
                            else if (selectedFace.getDistance() == face.getDistance() && face.getBox().getPriority() > selectedFace.getBox().getPriority()) selectedFace = face;
                        }
                        else selectedFace = face;
                    }
                });
            }

            Keystone.RenderHighlightBox = selectedFace == null && !draggingBox;
            if (selectedFace != null && draggingBox) selectedFace.getBox().drag(selectedFace);
        }
    }
    //endregion
    //region Input
    @SubscribeEvent
    public void onMouseInput(final InputEvent.MouseInputEvent event)
    {
        if (Keystone.isActive())
        {
            if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                if (selectedFace == null) createSelectionBox(event);
                else
                {
                    if (event.getAction() == GLFW.GLFW_PRESS)
                    {
                        draggingBox = true;
                        if (selectedFace.getBox() instanceof SelectionBoundingBox)
                        {
                            Keystone.getModule(HistoryModule.class).pushToHistory(new SelectionHistoryEntry(selectionBoxes, true));
                        }
                    }
                    else if (event.getAction() == GLFW.GLFW_RELEASE) draggingBox = false;
                }
            }
            else if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            {
                if (event.getAction() == GLFW.GLFW_PRESS) Keystone.CloseSelection = true;
                else if (event.getAction() == GLFW.GLFW_RELEASE) Keystone.CloseSelection = false;
            }
        }
    }
    //endregion
    //region Controls
    private void createSelectionBox(final InputEvent.MouseInputEvent event)
    {
        // If press left click
        if (event.getAction() == GLFW.GLFW_PRESS)
        {
            // If not holding control, clear selection blocks
            if ((event.getMods() & GLFW.GLFW_MOD_CONTROL) == 0) this.selectionBoxes.clear();

            // Start new selection box
            firstSelectionPoint = Player.getHighlightedBlock();
            creatingSelection = true;
            selectionBoxes.add(SelectionBoundingBox.startNew(firstSelectionPoint));
        }
        // If release left click
        else if (event.getAction() == GLFW.GLFW_RELEASE)
        {
            // Push to history
            if (creatingSelection) Keystone.getModule(HistoryModule.class).pushToHistory(new SelectionHistoryEntry(selectionBoxes, false));

            // End selection box creation
            firstSelectionPoint = null;
            creatingSelection = false;
        }
    }
    //endregion
}
