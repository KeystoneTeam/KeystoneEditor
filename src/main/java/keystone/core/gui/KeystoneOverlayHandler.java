package keystone.core.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.core.KeystoneGlobalState;
import keystone.core.events.KeystoneInputEvent;
import keystone.core.gui.screens.KeystoneOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeystoneOverlayHandler
{
    private static boolean worldFinishedLoading = false;
    private static Queue<IKeystoneTooltip> tooltips = new ArrayDeque<>();
    private static List<Screen> overlays = new ArrayList<>();
    private static List<Screen> addList = new ArrayList<>();
    private static List<Screen> removeList = new ArrayList<>();

    private static boolean rendering;

    public static void addOverlay(Screen overlay)
    {
        if (!(overlay instanceof KeystoneOverlay)) Keystone.LOGGER.warn("Adding non-KeystoneOverlay screen to Keystone Overlay Handler! This is not recommended.");

        Minecraft mc = Minecraft.getInstance();
        overlay.init(mc, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
        addList.add(overlay);
    }
    public static void removeOverlay(Screen overlay)
    {
        overlay.removed();
        removeList.add(overlay);
    }
    public static void addTooltip(IKeystoneTooltip tooltip)
    {
        tooltips.add(tooltip);
    }
    public static boolean isRendering()
    {
        return rendering;
    }

    @SubscribeEvent
    public static void onPreRenderGui(final RenderGameOverlayEvent.Pre event)
    {
        if (Keystone.isActive())
        {
            if (event.getType() == RenderGameOverlayEvent.ElementType.VIGNETTE)
            {
                if (!worldFinishedLoading)
                {
                    addList.forEach(add -> overlays.add(add));
                    addList.clear();

                    Minecraft mc = Minecraft.getInstance();
                    resize(mc, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
                    worldFinishedLoading = true;
                }
                event.setCanceled(true);
            }
        }
    }

    //region Event Subscribers
    @SubscribeEvent
    public static void onWorldUnload(final WorldEvent.Unload event)
    {
        worldFinishedLoading = false;
    }
    @SubscribeEvent
    public static void tick(final TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START) tick();
    }
    @SubscribeEvent
    public static void mouseClicked(final InputEvent.MouseInputEvent event)
    {
        if (Keystone.isActive())
        {
            Minecraft mc = Minecraft.getInstance();
            double mouseX = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getWidth();
            double mouseY = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getHeight();

            if (event.getAction() == GLFW.GLFW_PRESS) mouseClicked(mouseX, mouseY, event.getButton());
            if (event.getAction() == GLFW.GLFW_RELEASE) mouseReleased(mouseX, mouseY, event.getButton());
        }
    }
    @SubscribeEvent
    public static void mouseScrolled(final InputEvent.MouseScrollEvent event)
    {
        if (Keystone.isActive())
        {
            Minecraft mc = Minecraft.getInstance();
            double mouseX = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getWidth();
            double mouseY = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getHeight();
            mouseScrolled(mouseX, mouseY, event.getScrollDelta());
        }
    }
    @SubscribeEvent
    public static void mouseDragged(final KeystoneInputEvent.MouseDragEvent event)
    {
        if (Keystone.isActive()) mouseDragged(event.mouseX, event.mouseY, event.button, event.dragX, event.dragY);
    }
    @SubscribeEvent
    public static void mouseMoved(final KeystoneInputEvent.MouseMoveEvent event)
    {
        if (Keystone.isActive()) mouseMoved(event.mouseX, event.mouseY);
    }
    //endregion
    //region Event Forwarding
    private static void tick()
    {
        KeystoneGlobalState.BlockingKeys = Minecraft.getInstance().screen != null;
        if (Minecraft.getInstance().screen != null) return;

        overlays.forEach(screen ->
        {
            screen.tick();
            for (IGuiEventListener listener : screen.children())
            {
                if (listener instanceof TextFieldWidget)
                {
                    if (((TextFieldWidget) listener).isFocused()) KeystoneGlobalState.BlockingKeys = true;
                }
            }
        });
    }
    public static void resize(Minecraft minecraft, int width, int height)
    {
        if (Minecraft.getInstance().screen != null) return;
        overlays.forEach(screen -> screen.resize(minecraft, width, height));
    }
    public static void render(MatrixStack matrixStack, float partialTicks)
    {
        Minecraft mc = Minecraft.getInstance();
        int mouseX = (int)(mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getWidth());
        int mouseY = (int)(mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getHeight());

        KeystoneGlobalState.MouseOverGUI = mc.screen != null;
        if (Keystone.isActive())
        {
            rendering = true;
            addList.forEach(add -> overlays.add(add));
            addList.clear();

            // Render overlays
            for (int i = 0; i < overlays.size(); i++)
            {
                if (Minecraft.getInstance().screen != null && i > 0) continue;

                matrixStack.pushPose();
                matrixStack.translate(0, 0, i * 200);

                Screen screen = overlays.get(i);
                screen.render(matrixStack, mouseX, mouseY, partialTicks);
                if (!KeystoneGlobalState.MouseOverGUI && screen instanceof KeystoneOverlay) ((KeystoneOverlay) screen).checkMouseOverGui();

                matrixStack.popPose();
            }

            // Render tooltips
            IKeystoneTooltip tooltip = tooltips.poll();
            while (tooltip != null)
            {
                tooltip.render(matrixStack, mouseX, mouseY, partialTicks);
                tooltip = tooltips.poll();
            }

            removeList.forEach(remove -> overlays.remove(remove));
            removeList.clear();
            rendering = false;
        }
    }

    public static boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (Minecraft.getInstance().screen != null) return false;

        for (int i = overlays.size() - 1; i >= 0; i--)
        {
            Screen screen = overlays.get(i);
            if (screen.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return false;
    }
    public static boolean keyReleased(int keyCode, int scanCode, int modifiers)
    {
        if (Minecraft.getInstance().screen != null) return false;

        for (int i = overlays.size() - 1; i >= 0; i--)
        {
            Screen screen = overlays.get(i);
            if (screen.keyReleased(keyCode, scanCode, modifiers)) return true;
        }
        return false;
    }
    private static boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (Minecraft.getInstance().screen != null) return false;

        for (int i = overlays.size() - 1; i >= 0; i--)
        {
            Screen screen = overlays.get(i);
            if (screen.mouseClicked(mouseX, mouseY, button)) return true;
        }
        return false;
    }
    private static boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if (Minecraft.getInstance().screen != null) return false;

        for (int i = overlays.size() - 1; i >= 0; i--)
        {
            Screen screen = overlays.get(i);
            if (screen.mouseReleased(mouseX, mouseY, button)) return true;
        }
        return false;
    }
    private static boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        if (Minecraft.getInstance().screen != null) return false;

        for (int i = overlays.size() - 1; i >= 0; i--)
        {
            Screen screen = overlays.get(i);
            if (screen.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        }
        return false;
    }
    private static boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        if (Minecraft.getInstance().screen != null) return false;

        for (int i = overlays.size() - 1; i >= 0; i--)
        {
            Screen screen = overlays.get(i);
            if (screen.mouseScrolled(mouseX, mouseY, delta)) return true;
        }
        return false;
    }
    public static boolean charTyped(char codePoint, int modifiers)
    {
        if (Minecraft.getInstance().screen != null) return false;

        for (int i = overlays.size() - 1; i >= 0; i--)
        {
            Screen screen = overlays.get(i);
            if (screen.charTyped(codePoint, modifiers)) return true;
        }
        return false;
    }
    private static void mouseMoved(double xPos, double mouseY)
    {
        if (Minecraft.getInstance().screen != null) return;

        overlays.forEach(screen -> screen.mouseMoved(xPos, mouseY));
    }
    //endregion
}
