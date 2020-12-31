package keystone.modules.selection;

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

    public List<SelectionBoundingBox> getSelectionBoxes()
    {
        return selectionBoxes;
    }

    @Override
    public IBoundingBoxProvider[] getBoundingBoxProviders()
    {
        return renderProviders;
    }

    @Override
    public void prepareRender(float partialTicks, DimensionId dimensionId)
    {
        if (creatingSelection) selectionBoxes.get(selectionBoxes.size() - 1).setCorner2(Player.getHighlightedBlock());
    }

    @SubscribeEvent
    public void onMouseInput(final InputEvent.MouseInputEvent event)
    {
        if (Keystone.Active && Minecraft.getInstance().world != null)
        {
            if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT)
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
            else if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            {
                // If press right click, enable close selection
                if (event.getAction() == GLFW.GLFW_PRESS) Keystone.CloseSelection = true;
                // If release right click, disable close selection
                else if (event.getAction() == GLFW.GLFW_RELEASE) Keystone.CloseSelection = false;
            }
        }
    }
}
