package keystone.modules.selection;

import keystone.api.SelectionBox;
import keystone.core.Keystone;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.client.providers.IBoundingBoxProvider;
import keystone.core.renderer.common.models.Coords;
import keystone.core.renderer.common.models.DimensionId;
import keystone.modules.IKeystoneModule;
import keystone.modules.selection.boxes.SelectionBoundingBox;
import keystone.modules.selection.providers.HighlightBoxProvider;
import keystone.modules.selection.providers.SelectionBoxProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class SelectionModule implements IKeystoneModule
{
    private List<SelectionBoundingBox> selectionBoxes;
    private IBoundingBoxProvider[] renderProviders;

    private SelectionFace selectedFace;
    private Coords firstSelectionPoint;
    private boolean creatingSelection;
    private boolean draggingFace;

    public SelectionModule()
    {
        MinecraftForge.EVENT_BUS.register(this);

        draggingFace = false;

        selectionBoxes = new ArrayList<>();
        renderProviders = new IBoundingBoxProvider[]
        {
                new SelectionBoxProvider(this),
                new HighlightBoxProvider()
        };
    }

    public SelectionFace getSelectedFace() { return selectedFace; }
    public List<SelectionBoundingBox> getSelectionBoundingBoxes()
    {
        return selectionBoxes;
    }
    public SelectionBox[] buildSelectionBoxes(World world)
    {
        SelectionBox[] boxes = new SelectionBox[selectionBoxes.size()];
        for (int i = 0; i < boxes.length; i++) boxes[i] = new SelectionBox(selectionBoxes.get(i).getMinCoords(), selectionBoxes.get(i).getMaxCoords(), world);
        return boxes;
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
            if (!draggingFace)
            {
                selectedFace = null;
                for (SelectionBoundingBox box : selectionBoxes)
                {
                    SelectionFace face = box.getSelectedFace();
                    if (face != null && (selectedFace == null || selectedFace.distanceSqr > face.distanceSqr)) selectedFace = face;
                }
            }

            Keystone.RenderHighlightBox = selectedFace == null && !draggingFace;
            if (selectedFace != null && draggingFace) selectedFace.drag();
        }
    }
    //endregion
    //region Input
    @SubscribeEvent
    public void onMouseInput(final InputEvent.MouseInputEvent event)
    {
        if (Keystone.Active && Minecraft.getInstance().world != null)
        {
            if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                if (selectedFace == null) createSelectionBox(event);
                else
                {
                    if (event.getAction() == GLFW.GLFW_PRESS) draggingFace = true;
                    else if (event.getAction() == GLFW.GLFW_RELEASE) draggingFace = false;
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
            // End selection box creation
            firstSelectionPoint = null;
            creatingSelection = false;
        }
    }
    //endregion
}
