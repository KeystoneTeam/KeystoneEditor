package keystone.core.events;

import keystone.core.KeystoneConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeystoneInputHandler
{
    private static long leftClickTimestamp;
    private static long middleClickTimestamp;
    private static long rightClickTimestamp;
    private static int leftClickModifiers;
    private static int middleClickModifiers;
    private static int rightClickModifiers;
    private static Vector3d leftClickLocation;
    private static Vector3d middleClickLocation;
    private static Vector3d rightClickLocation;
    private static byte leftDragging;
    private static byte middleDragging;
    private static byte rightDragging;

    public static void setLeftClickLocation(double x, double y)
    {
        KeystoneInputHandler.leftClickLocation = new Vector3d(x, y, 0);
    }
    public static void setMiddleClickLocation(double x, double y)
    {
        KeystoneInputHandler.middleClickLocation = new Vector3d(x, y, 0);
    }
    public static void setRightClickLocation(double x, double y)
    {
        KeystoneInputHandler.rightClickLocation = new Vector3d(x, y, 0);
    }

    public static final void onMouseMove(double mouseX, double mouseY)
    {
        if (Minecraft.getInstance().world == null) return;
        MinecraftForge.EVENT_BUS.post(new KeystoneInputEvent.MouseMoveEvent(mouseX, mouseY));
    }
    @SubscribeEvent
    public static final void postRender(final RenderWorldLastEvent event)
    {
        MouseHelper mouse = Minecraft.getInstance().mouseHelper;
        if (leftClickTimestamp > 0)
        {
            long time = System.currentTimeMillis();
            Vector3d mouseLocation = new Vector3d(mouse.getMouseX(), mouse.getMouseY(), 0);

            if (!mouse.isLeftDown()) onRelease(GLFW.GLFW_MOUSE_BUTTON_LEFT);
            else if (time - leftClickTimestamp > KeystoneConfig.clickThreshold || leftClickLocation.squareDistanceTo(mouseLocation) > KeystoneConfig.dragThresholdSqr)
            {
                if (leftDragging == 0)
                {
                    MinecraftForge.EVENT_BUS.post(new KeystoneInputEvent.MouseDragStartEvent(GLFW.GLFW_MOUSE_BUTTON_LEFT, mouse.getMouseX(), mouse.getYVelocity()));
                    leftDragging = 3;
                }
            }
        }
        if (middleClickTimestamp > 0)
        {
            long time = System.currentTimeMillis();
            Vector3d mouseLocation = new Vector3d(mouse.getMouseX(), mouse.getMouseY(), 0);

            if (!mouse.isMiddleDown()) onRelease(GLFW.GLFW_MOUSE_BUTTON_MIDDLE);
            else if (time - middleClickTimestamp > KeystoneConfig.clickThreshold || middleClickLocation.squareDistanceTo(mouseLocation) > KeystoneConfig.dragThresholdSqr)
            {
                if (middleDragging == 0)
                {
                    MinecraftForge.EVENT_BUS.post(new KeystoneInputEvent.MouseDragStartEvent(GLFW.GLFW_MOUSE_BUTTON_MIDDLE, mouse.getMouseX(), mouse.getYVelocity()));
                    middleDragging = 3;
                }
            }
        }
        if (rightClickTimestamp > 0)
        {
            long time = System.currentTimeMillis();
            Vector3d mouseLocation = new Vector3d(mouse.getMouseX(), mouse.getMouseY(), 0);

            if (!mouse.isRightDown()) onRelease(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
            else if (time - rightClickTimestamp > KeystoneConfig.clickThreshold || rightClickLocation.squareDistanceTo(mouseLocation) > KeystoneConfig.dragThresholdSqr)
            {
                if (rightDragging == 0)
                {
                    MinecraftForge.EVENT_BUS.post(new KeystoneInputEvent.MouseDragStartEvent(GLFW.GLFW_MOUSE_BUTTON_RIGHT, mouse.getMouseX(), mouse.getYVelocity()));
                    rightDragging = 3;
                }
            }
        }
    }

    @SubscribeEvent
    public static final void onMouseInput(final InputEvent.MouseInputEvent event)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.world == null) return;

        if (event.getAction() == GLFW.GLFW_PRESS)
        {
            if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                leftClickTimestamp = System.currentTimeMillis();
                leftClickModifiers = event.getMods();
                leftClickLocation = new Vector3d(mc.mouseHelper.getMouseX(), mc.mouseHelper.getMouseY(), 0);
            }
            else if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE)
            {
                middleClickTimestamp = System.currentTimeMillis();
                middleClickModifiers = event.getMods();
                middleClickLocation = new Vector3d(mc.mouseHelper.getMouseX(), mc.mouseHelper.getMouseY(), 0);
            }
            else if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            {
                rightClickTimestamp = System.currentTimeMillis();
                rightClickModifiers = event.getMods();
                rightClickLocation = new Vector3d(mc.mouseHelper.getMouseX(), mc.mouseHelper.getMouseY(), 0);
            }
        }
        else if (event.getAction() == GLFW.GLFW_RELEASE) onRelease(event.getButton());
    }
    public static final void onMouseDrag(int button, double mouseX, double mouseY, double dragX, double dragY)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.world == null) return;

        Vector3d mouseLocation = new Vector3d(mc.mouseHelper.getMouseX(), mc.mouseHelper.getMouseY(), 0);
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            if (System.currentTimeMillis() - leftClickTimestamp > KeystoneConfig.clickThreshold || leftClickLocation.squareDistanceTo(mouseLocation) > KeystoneConfig.dragThresholdSqr)
            {
                if (leftDragging == 0)
                {
                    MinecraftForge.EVENT_BUS.post(new KeystoneInputEvent.MouseDragStartEvent(button, mouseX, mouseY));
                    leftDragging = 3;
                }
                else if (leftDragging == 2) leftDragging--;
                else if (leftDragging == 1) MinecraftForge.EVENT_BUS.post(new KeystoneInputEvent.MouseDragEvent(button, mouseX, mouseY, dragX, dragY));
            }
        }
        else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE)
        {
            if (System.currentTimeMillis() - middleClickTimestamp > KeystoneConfig.clickThreshold || middleClickLocation.squareDistanceTo(mouseLocation) > KeystoneConfig.dragThresholdSqr)
            {
                if (middleDragging == 0)
                {
                    MinecraftForge.EVENT_BUS.post(new KeystoneInputEvent.MouseDragStartEvent(button, mouseX, mouseY));
                    middleDragging = 3;
                }
                else if (middleDragging == 2) middleDragging--;
                else if (leftDragging == 1) MinecraftForge.EVENT_BUS.post(new KeystoneInputEvent.MouseDragEvent(button, mouseX, mouseY, dragX, dragY));
            }
        }
        else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
        {
            if (System.currentTimeMillis() - rightClickTimestamp > KeystoneConfig.clickThreshold || rightClickLocation.squareDistanceTo(mouseLocation) > KeystoneConfig.dragThresholdSqr)
            {
                if (rightDragging == 0)
                {
                    MinecraftForge.EVENT_BUS.post(new KeystoneInputEvent.MouseDragStartEvent(button, mouseX, mouseY));
                    rightDragging = 3;
                }
                else if (rightDragging == 2) rightDragging--;
                else if (rightDragging == 1) MinecraftForge.EVENT_BUS.post(new KeystoneInputEvent.MouseDragEvent(button, mouseX, mouseY, dragX, dragY));
            }
        }
    }

    private static void onRelease(int button)
    {
        Minecraft mc = Minecraft.getInstance();
        Vector3d mouseLocation = new Vector3d(mc.mouseHelper.getMouseX(), mc.mouseHelper.getMouseY(), 0);
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && leftClickTimestamp > 0)
        {
            if (System.currentTimeMillis() - leftClickTimestamp <= KeystoneConfig.clickThreshold && leftClickLocation.squareDistanceTo(mouseLocation) <= KeystoneConfig.dragThresholdSqr)
            {
                MinecraftForge.EVENT_BUS.post(new KeystoneInputEvent.MouseClickEvent(button, leftClickModifiers, leftClickLocation.x, leftClickLocation.y));
            }
            else
            {
                if (leftDragging == 0) MinecraftForge.EVENT_BUS.post(new KeystoneInputEvent.MouseDragStartEvent(button, mouseLocation.x, mouseLocation.y));
                MinecraftForge.EVENT_BUS.post(new KeystoneInputEvent.MouseDragEndEvent(button, mouseLocation.x, mouseLocation.y));
            }
            leftDragging = 0;
            leftClickTimestamp = -1;
        }
        else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE && middleClickTimestamp > 0)
        {
            if (System.currentTimeMillis() - middleClickTimestamp <= KeystoneConfig.clickThreshold && middleClickLocation.squareDistanceTo(mouseLocation) <= KeystoneConfig.dragThresholdSqr)
            {
                MinecraftForge.EVENT_BUS.post(new KeystoneInputEvent.MouseClickEvent(button, middleClickModifiers, middleClickLocation.x, middleClickLocation.y));
            }
            else
            {
                if (middleDragging == 0) MinecraftForge.EVENT_BUS.post(new KeystoneInputEvent.MouseDragStartEvent(button, mouseLocation.x, mouseLocation.y));
                MinecraftForge.EVENT_BUS.post(new KeystoneInputEvent.MouseDragEndEvent(button, mouseLocation.x, mouseLocation.y));
            }
            middleDragging = 0;
            middleClickTimestamp = -1;
        }
        else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && rightClickTimestamp > 0)
        {
            if (System.currentTimeMillis() - rightClickTimestamp <= KeystoneConfig.clickThreshold && rightClickLocation.squareDistanceTo(mouseLocation) <= KeystoneConfig.dragThresholdSqr)
            {
                MinecraftForge.EVENT_BUS.post(new KeystoneInputEvent.MouseClickEvent(button, rightClickModifiers, rightClickLocation.x, rightClickLocation.y));
            }
            else
            {
                if (rightDragging == 0) MinecraftForge.EVENT_BUS.post(new KeystoneInputEvent.MouseDragStartEvent(button, mouseLocation.x, mouseLocation.y));
                MinecraftForge.EVENT_BUS.post(new KeystoneInputEvent.MouseDragEndEvent(button, mouseLocation.x, mouseLocation.y));
            }
            rightDragging = 0;
            rightClickTimestamp = -1;
        }
    }
}
