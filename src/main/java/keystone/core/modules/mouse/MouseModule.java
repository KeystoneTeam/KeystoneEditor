package keystone.core.modules.mouse;

import keystone.api.Keystone;
import keystone.core.KeystoneConfig;
import keystone.core.KeystoneGlobalState;
import keystone.core.events.keystone.KeystoneInputEvents;
import keystone.core.events.minecraft.InputEvents;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.selection.SelectedFace;
import keystone.core.modules.selection.SelectionModule;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class MouseModule implements IKeystoneModule
{
    private SelectionModule selectionModule;
    private SelectedFace selectedFace;
    private List<SelectedFace> orderedSelectedFaces;
    private boolean draggingBox;
    
    public MouseModule()
    {
        selectedFace = null;
        draggingBox = false;
        orderedSelectedFaces = new ArrayList<>();
        registerEvents();
    }

    //region Module Overrides
    @Override
    public boolean isEnabled()
    {
        return true;
    }

    @Override
    public void postInit()
    {
        this.selectionModule = Keystone.getModule(SelectionModule.class);
    }

    @Override
    public void preRender(WorldRenderContext context)
    {
        if (selectionModule.isCreatingSelection()) selectedFace = null;
        else
        {
            if (!draggingBox)
            {
                orderedSelectedFaces.clear();
                Keystone.forEachModule(module ->
                {
                    module.getSelectableBoxes().forEach(selectable ->
                    {
                        if (selectable.isEnabled())
                        {
                            selectable.getSelectedFaces(orderedSelectedFaces);
                            //if (face != null)
                            //{
                            //    if (selectedFace == null) selectedFace = face;
                            //    else if (face.getDistance() < selectedFace.getDistance()) selectedFace = face;
                            //    else if (face.getDistance() == selectedFace.getDistance() && face.getBox().getPriority() > selectedFace.getBox().getPriority()) selectedFace = face;
                            //    else if (!skippedFace && selectedFace.getInternalDistance() < KeystoneConfig.selectFaceSkipThreshold)
                            //    {
                            //        selectedFace = face;
                            //        skippedFace = true;
                            //    }
                            //}
                        }
                    });
                });
                if (orderedSelectedFaces.size() > 0)
                {
                    orderedSelectedFaces.sort(Comparator.comparingDouble(SelectedFace::getDistance));
                    selectedFace = orderedSelectedFaces.get(0);
                    if (selectedFace.getInternalDistance() < KeystoneConfig.selectFaceSkipThreshold && orderedSelectedFaces.size() > 1) selectedFace = orderedSelectedFaces.get(1);
                }
                else selectedFace = null;
            }
            else if (selectedFace != null) selectedFace.getBox().drag(selectedFace);
        }
    }
    //endregion
    //region Getters
    public SelectedFace getSelectedFace() { return selectedFace; }
    public boolean isDraggingBox() { return draggingBox; }
    //endregion
    //region Events
    private void registerEvents()
    {
        KeystoneInputEvents.MOUSE_CLICKED.register(this::onMouseClick);
        KeystoneInputEvents.START_MOUSE_DRAG.register(this::onMouseDragStart);
        KeystoneInputEvents.END_MOUSE_DRAG.register(this::onMouseDragEnd);
        InputEvents.MOUSE_SCROLLED.register(this::onScroll);
    }
    
    private void onMouseClick(int button, int modifiers, double mouseX, double mouseY, boolean gui)
    {
        if (Keystone.isActive() && MinecraftClient.getInstance().currentScreen == null && !gui && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) setLookEnabled(!KeystoneGlobalState.AllowPlayerLook);
    }
    private void onMouseDragStart(int button, double mouseX, double mouseY, boolean gui)
    {
        if (Keystone.isActive() && MinecraftClient.getInstance().currentScreen == null && !gui)
        {
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) setLookEnabled(true);
            else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && selectedFace != null) startDraggingBox();
        }
    }
    private void onMouseDragEnd(int button, double mouseX, double mouseY, boolean gui)
    {
        if (Keystone.isActive() && MinecraftClient.getInstance().currentScreen == null && !gui)
        {
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) setLookEnabled(false);
            else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) endDraggingBox();
        }
    }
    private void onScroll(double offsetX, double offsetY)
    {
        if (Keystone.isActive() && !KeystoneGlobalState.MouseOverGUI && KeystoneGlobalState.CloseSelection)
        {
            KeystoneGlobalState.CloseSelectionDistance += offsetY;
            if (KeystoneGlobalState.CloseSelectionDistance < 0) KeystoneGlobalState.CloseSelectionDistance = 0;
        }
    }
    //endregion
    //region Helpers
    private void setLookEnabled(boolean allowLook)
    {
        KeystoneGlobalState.AllowPlayerLook = allowLook;
        KeystoneGlobalState.CloseSelection = allowLook;
        if (allowLook) MinecraftClient.getInstance().mouse.lockCursor();
        else MinecraftClient.getInstance().mouse.unlockCursor();
    }
    private void startDraggingBox()
    {
        if (!draggingBox)
        {
            if (selectedFace.getBox().isEnabled())
            {
                draggingBox = true;
                selectedFace.getBox().startDrag(selectedFace);
                selectedFace.startDrag();
            }
        }
    }
    private void endDraggingBox()
    {
        if (draggingBox)
        {
            draggingBox = false;
            selectedFace.getBox().endDrag(selectedFace);
            selectedFace.endDrag();
        }
    }
    //endregion
}
