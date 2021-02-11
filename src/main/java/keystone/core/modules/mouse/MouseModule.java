package keystone.core.modules.mouse;

import keystone.api.Keystone;
import keystone.core.KeystoneStateFlags;
import keystone.core.events.KeystoneInputEvent;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.selection.SelectedFace;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.renderer.client.ClientRenderer;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.common.models.AbstractBoundingBox;
import keystone.core.renderer.common.models.DimensionId;
import keystone.core.renderer.common.models.SelectableBoundingBox;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class MouseModule implements IKeystoneModule
{
    private SelectionModule selectionModule;
    private SelectedFace selectedFace;
    private boolean draggingBox;

    public MouseModule()
    {
        MinecraftForge.EVENT_BUS.register(this);
        selectedFace = null;
        draggingBox = false;
    }

    @Override
    public boolean isEnabled()
    {
        return true;
    }
    @Override
    public void prepareRender(float partialTicks, DimensionId dimensionId)
    {
        if (selectionModule == null) selectionModule = Keystone.getModule(SelectionModule.class);

        if (selectionModule.isCreatingSelection()) selectedFace = null;
        else
        {
            if (!draggingBox)
            {
                selectedFace = null;
                List<AbstractBoundingBox> boxes = new ArrayList<>();
                ClientRenderer.getBoundingBoxes(Player.getDimensionId()).forEachOrdered(box -> boxes.add(box));
                for (AbstractBoundingBox box : boxes)
                {
                    if (box instanceof SelectableBoundingBox)
                    {
                        SelectableBoundingBox selectable = (SelectableBoundingBox)box;
                        if (!selectable.isEnabled()) continue;

                        SelectedFace face = selectable.getSelectedFace();
                        if (face != null)
                        {
                            if (selectedFace == null) selectedFace = face;
                            else if (face.getDistance() < selectedFace.getDistance()) selectedFace = face;
                            else if (face.getDistance() == selectedFace.getDistance() && face.getBox().getPriority() > selectedFace.getBox().getPriority()) selectedFace = face;
                        }
                    }
                }
            }
            else if (selectedFace != null) selectedFace.getBox().drag(selectedFace);
        }
    }

    //region Getters
    public SelectedFace getSelectedFace() { return selectedFace; }
    public boolean isDraggingBox() { return draggingBox; }
    //endregion
    //region Events
    @SubscribeEvent
    public final void onMouseClick(final KeystoneInputEvent.MouseClickEvent event)
    {
        if (Keystone.isActive() && Minecraft.getInstance().currentScreen == null && !event.gui && event.button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) setLookEnabled(!KeystoneStateFlags.AllowPlayerLook);
    }
    @SubscribeEvent
    public final void onMouseDragStart(final KeystoneInputEvent.MouseDragStartEvent event)
    {
        if (Keystone.isActive() && Minecraft.getInstance().currentScreen == null && !event.gui)
        {
            if (event.button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) setLookEnabled(true);
            else if (event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT && selectedFace != null) startDraggingBox();
        }
    }
    @SubscribeEvent
    public final void onMouseDragEnd(final KeystoneInputEvent.MouseDragEndEvent event)
    {
        if (Keystone.isActive() && Minecraft.getInstance().currentScreen == null && !event.gui)
        {
            if (event.button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) setLookEnabled(false);
            else if (event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT) endDraggingBox();
        }
    }
    //endregion
    //region Helpers
    private void setLookEnabled(boolean allowLook)
    {
        KeystoneStateFlags.AllowPlayerLook = allowLook;
        KeystoneStateFlags.CloseSelection = allowLook;
        if (allowLook) Minecraft.getInstance().mouseHelper.grabMouse();
        else Minecraft.getInstance().mouseHelper.ungrabMouse();
    }
    private void startDraggingBox()
    {
        if (!draggingBox)
        {
            if (selectedFace.getBox().isEnabled())
            {
                draggingBox = true;
                selectedFace.getBox().startDrag(selectedFace);
            }
        }
    }
    private void endDraggingBox()
    {
        if (draggingBox)
        {
            draggingBox = false;
            selectedFace.getBox().endDrag(selectedFace);
        }
    }
    //endregion
}
