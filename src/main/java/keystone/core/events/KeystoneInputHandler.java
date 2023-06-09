package keystone.core.events;

import keystone.core.KeystoneConfig;
import keystone.core.KeystoneGlobalState;
import keystone.core.events.keystone.KeystoneInputEvents;
import keystone.core.events.minecraft.InputEvents;
import keystone.core.gui.KeystoneOverlayHandler;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class KeystoneInputHandler
{
    private static long leftClickTimestamp;
    private static long middleClickTimestamp;
    private static long rightClickTimestamp;
    private static int leftClickModifiers;
    private static int middleClickModifiers;
    private static int rightClickModifiers;
    private static Vec3d leftClickLocation;
    private static Vec3d middleClickLocation;
    private static Vec3d rightClickLocation;
    private static byte leftDragging;
    private static byte middleDragging;
    private static byte rightDragging;
    private static boolean leftClickGui;
    private static boolean middleClickGui;
    private static boolean rightClickGui;

    public static void setLeftClickLocation(double x, double y)
    {
        KeystoneInputHandler.leftClickLocation = new Vec3d(x, y, 0);
    }
    public static void setMiddleClickLocation(double x, double y)
    {
        KeystoneInputHandler.middleClickLocation = new Vec3d(x, y, 0);
    }
    public static void setRightClickLocation(double x, double y)
    {
        KeystoneInputHandler.rightClickLocation = new Vec3d(x, y, 0);
    }

    public static void onMouseMove(double mouseX, double mouseY)
    {
        if (MinecraftClient.getInstance().world == null) return;
        KeystoneInputEvents.MOUSE_MOVED.invoker().mouseMoved(mouseX, mouseY);
    }
    
    public static void registerEvents()
    {
        WorldRenderEvents.LAST.register(KeystoneInputHandler::postRender);
        InputEvents.MOUSE_CLICKED.register(KeystoneInputHandler::mouseClicked);
        InputEvents.KEY_EVENT.register(KeystoneInputHandler::onKeyInput);
    }
    
    public static void postRender(WorldRenderContext context)
    {
        Mouse mouse = MinecraftClient.getInstance().mouse;
        Vec3d mouseLocation = new Vec3d(mouse.getX(), mouse.getY(), 0);
        long time = System.currentTimeMillis();

        if (leftClickTimestamp > 0)
        {
            if (!mouse.wasLeftButtonClicked()) onRelease(GLFW.GLFW_MOUSE_BUTTON_LEFT);
            else if (time - leftClickTimestamp > KeystoneConfig.clickThreshold || leftClickLocation.squaredDistanceTo(mouseLocation) > KeystoneConfig.dragThresholdSqr)
            {
                if (leftDragging == 0)
                {
                    KeystoneInputEvents.START_MOUSE_DRAG.invoker().startDrag(GLFW.GLFW_MOUSE_BUTTON_LEFT, mouse.getX(), mouse.getY(), leftClickGui);
                    leftDragging = 3;
                }
            }
        }
        if (middleClickTimestamp > 0)
        {
            if (!mouse.wasMiddleButtonClicked()) onRelease(GLFW.GLFW_MOUSE_BUTTON_MIDDLE);
            else if (time - middleClickTimestamp > KeystoneConfig.clickThreshold || middleClickLocation.squaredDistanceTo(mouseLocation) > KeystoneConfig.dragThresholdSqr)
            {
                if (middleDragging == 0)
                {
                    KeystoneInputEvents.START_MOUSE_DRAG.invoker().startDrag(GLFW.GLFW_MOUSE_BUTTON_MIDDLE, mouse.getX(), mouse.getY(), middleClickGui);
                    middleDragging = 3;
                }
            }
        }
        if (rightClickTimestamp > 0)
        {
            if (!mouse.wasRightButtonClicked()) onRelease(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
            else if (time - rightClickTimestamp > KeystoneConfig.clickThreshold || rightClickLocation.squaredDistanceTo(mouseLocation) > KeystoneConfig.dragThresholdSqr)
            {
                if (rightDragging == 0)
                {
                    KeystoneInputEvents.START_MOUSE_DRAG.invoker().startDrag(GLFW.GLFW_MOUSE_BUTTON_RIGHT, mouse.getX(), mouse.getY(), rightClickGui);
                    rightDragging = 3;
                }
            }
        }
    }

    public static void mouseClicked(int button, int action, int modifiers)
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return;

        if (action == GLFW.GLFW_PRESS)
        {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                leftClickTimestamp = System.currentTimeMillis();
                leftClickModifiers = modifiers;
                leftClickLocation = new Vec3d(mc.mouse.getX(), mc.mouse.getY(), 0);
                leftClickGui = KeystoneOverlayHandler.isMouseOverGUI();
            }
            else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE)
            {
                middleClickTimestamp = System.currentTimeMillis();
                middleClickModifiers = modifiers;
                middleClickLocation = new Vec3d(mc.mouse.getX(), mc.mouse.getY(), 0);
                middleClickGui = KeystoneOverlayHandler.isMouseOverGUI();
            }
            else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            {
                rightClickTimestamp = System.currentTimeMillis();
                rightClickModifiers = modifiers;
                rightClickLocation = new Vec3d(mc.mouse.getX(), mc.mouse.getY(), 0);
                rightClickGui = KeystoneOverlayHandler.isMouseOverGUI();
            }
        }
        else if (action == GLFW.GLFW_RELEASE) onRelease(button);
    }
    public static void onMouseDrag(int button, double mouseX, double mouseY, double dragX, double dragY)
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return;

        Vec3d mouseLocation = new Vec3d(mc.mouse.getX(), mc.mouse.getY(), 0);
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            if (System.currentTimeMillis() - leftClickTimestamp > KeystoneConfig.clickThreshold || leftClickLocation.squaredDistanceTo(mouseLocation) > KeystoneConfig.dragThresholdSqr)
            {
                if (leftDragging == 0)
                {
                    KeystoneInputEvents.START_MOUSE_DRAG.invoker().startDrag(button, mouseX, mouseY, leftClickGui);
                    leftDragging = 3;
                }
                else if (leftDragging == 2) leftDragging--;
                else if (leftDragging == 1) KeystoneInputEvents.MOUSE_DRAG.invoker().drag(button, mouseX, mouseY, dragX, dragY, leftClickGui);
            }
        }
        else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE)
        {
            if (System.currentTimeMillis() - middleClickTimestamp > KeystoneConfig.clickThreshold || middleClickLocation.squaredDistanceTo(mouseLocation) > KeystoneConfig.dragThresholdSqr)
            {
                if (middleDragging == 0)
                {
                    KeystoneInputEvents.START_MOUSE_DRAG.invoker().startDrag(button, mouseX, mouseY, middleClickGui);
                    middleDragging = 3;
                }
                else if (middleDragging == 2) middleDragging--;
                else if (leftDragging == 1) KeystoneInputEvents.MOUSE_DRAG.invoker().drag(button, mouseX, mouseY, dragX, dragY, middleClickGui);
            }
        }
        else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
        {
            if (System.currentTimeMillis() - rightClickTimestamp > KeystoneConfig.clickThreshold || rightClickLocation.squaredDistanceTo(mouseLocation) > KeystoneConfig.dragThresholdSqr)
            {
                if (rightDragging == 0)
                {
                    KeystoneInputEvents.START_MOUSE_DRAG.invoker().startDrag(button, mouseX, mouseY, rightClickGui);
                    rightDragging = 3;
                }
                else if (rightDragging == 2) rightDragging--;
                else if (rightDragging == 1) KeystoneInputEvents.MOUSE_DRAG.invoker().drag(button, mouseX, mouseY, dragX, dragY, rightClickGui);
            }
        }
    }
    public static boolean onKeyInput(int key, int action, int scancode, int modifiers)
    {
        GameOptions settings = MinecraftClient.getInstance().options;
        Mouse mouse = MinecraftClient.getInstance().mouse;

        if (key == settings.forwardKey.getDefaultKey().getCode() ||
                key == settings.backKey.getDefaultKey().getCode() ||
                key == settings.leftKey.getDefaultKey().getCode() ||
                key == settings.rightKey.getDefaultKey().getCode() ||
                key == settings.jumpKey.getDefaultKey().getCode() ||
                key == settings.sneakKey.getDefaultKey().getCode())
        {
            if (leftClickTimestamp > 0)
            {
                if (leftDragging == 0)
                {
                    KeystoneInputEvents.START_MOUSE_DRAG.invoker().startDrag(GLFW.GLFW_MOUSE_BUTTON_LEFT, mouse.getX(), mouse.getY(), leftClickGui);
                    leftDragging = 3;
                }
            }
            if (middleClickTimestamp > 0)
            {
                if (middleDragging == 0)
                {
                    KeystoneInputEvents.START_MOUSE_DRAG.invoker().startDrag(GLFW.GLFW_MOUSE_BUTTON_MIDDLE, mouse.getX(), mouse.getY(), middleClickGui);
                    middleDragging = 3;
                }
            }
            if (rightClickTimestamp > 0)
            {
                if (rightDragging == 0)
                {
                    KeystoneInputEvents.START_MOUSE_DRAG.invoker().startDrag(GLFW.GLFW_MOUSE_BUTTON_RIGHT, mouse.getX(), mouse.getY(), rightClickGui);
                    rightDragging = 3;
                }
            }
        }
        
        return false;
    }

    private static void onRelease(int button)
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        Vec3d mouseLocation = new Vec3d(mc.mouse.getX(), mc.mouse.getY(), 0);
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && leftClickTimestamp > 0)
        {
            if (System.currentTimeMillis() - leftClickTimestamp <= KeystoneConfig.clickThreshold && leftClickLocation.squaredDistanceTo(mouseLocation) <= KeystoneConfig.dragThresholdSqr)
            {
                KeystoneInputEvents.MOUSE_CLICKED.invoker().mouseClicked(button, leftClickModifiers, leftClickLocation.x, leftClickLocation.y, leftClickGui);
            }
            else
            {
                if (leftDragging == 0) KeystoneInputEvents.START_MOUSE_DRAG.invoker().startDrag(button, mouseLocation.x, mouseLocation.y, leftClickGui);
                KeystoneInputEvents.END_MOUSE_DRAG.invoker().endDrag(button, mouseLocation.x, mouseLocation.y, leftClickGui);
            }
            leftDragging = 0;
            leftClickTimestamp = -1;
            leftClickGui = false;
        }
        else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && middleClickTimestamp > 0)
        {
            if (System.currentTimeMillis() - middleClickTimestamp <= KeystoneConfig.clickThreshold && middleClickLocation.squaredDistanceTo(mouseLocation) <= KeystoneConfig.dragThresholdSqr)
            {
                KeystoneInputEvents.MOUSE_CLICKED.invoker().mouseClicked(button, middleClickModifiers, middleClickLocation.x, middleClickLocation.y, middleClickGui);
            }
            else
            {
                if (middleDragging == 0) KeystoneInputEvents.START_MOUSE_DRAG.invoker().startDrag(button, mouseLocation.x, mouseLocation.y, middleClickGui);
                KeystoneInputEvents.END_MOUSE_DRAG.invoker().endDrag(button, mouseLocation.x, mouseLocation.y, middleClickGui);
            }
            middleDragging = 0;
            middleClickTimestamp = -1;
            middleClickGui = false;
        }
        else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && rightClickTimestamp > 0)
        {
            if (System.currentTimeMillis() - rightClickTimestamp <= KeystoneConfig.clickThreshold && rightClickLocation.squaredDistanceTo(mouseLocation) <= KeystoneConfig.dragThresholdSqr)
            {
                KeystoneInputEvents.MOUSE_CLICKED.invoker().mouseClicked(button, rightClickModifiers, rightClickLocation.x, rightClickLocation.y, rightClickGui);
            }
            else
            {
                if (rightDragging == 0) KeystoneInputEvents.START_MOUSE_DRAG.invoker().startDrag(button, mouseLocation.x, mouseLocation.y, rightClickGui);
                KeystoneInputEvents.END_MOUSE_DRAG.invoker().endDrag(button, mouseLocation.x, mouseLocation.y, rightClickGui);
            }
            rightDragging = 0;
            rightClickTimestamp = -1;
            rightClickGui = false;
        }
    }
}
