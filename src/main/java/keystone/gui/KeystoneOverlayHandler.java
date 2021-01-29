package keystone.gui;

import keystone.api.Keystone;
import keystone.gui.screens.hotbar.KeystoneHotbar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.IWindowEventListener;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeystoneOverlayHandler
{
    public static boolean MouseOverGUI = false;
    public static boolean BlockingKeys = false;

    private static List<Screen> overlays = new ArrayList<>();
    private static List<Screen> addList = new ArrayList<>();
    private static List<Screen> removeList = new ArrayList<>();
    private static int previousWidth;
    private static int previousHeight;
    private static double previousScale;

    static
    {
        addOverlay(new KeystoneHotbar());
    }

    public static void addOverlay(Screen overlay)
    {
        Minecraft mc = Minecraft.getInstance();
        overlay.init(mc, mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight());
        addList.add(overlay);
    }
    public static void removeOverlay(Screen overlay)
    {
        overlay.onClose();
        removeList.add(overlay);
    }

    @SubscribeEvent
    public static void onPreRenderGui(final RenderGameOverlayEvent.Pre event)
    {
        if (Keystone.isActive() && event.getType() == RenderGameOverlayEvent.ElementType.VIGNETTE) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void tick(final TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            BlockingKeys = Minecraft.getInstance().currentScreen != null;
            overlays.forEach(overlay ->
            {
                overlay.tick();
                for (IGuiEventListener listener : overlay.getEventListeners())
                {
                    if (listener instanceof Widget)
                    {
                        if (((Widget) listener).isFocused()) BlockingKeys = true;
                    }
                }
            });
        }
    }
    @SubscribeEvent
    public static void render(final RenderGameOverlayEvent.Post event)
    {
        Minecraft mc = Minecraft.getInstance();
        MouseOverGUI = mc.currentScreen != null;

        if (mc.getMainWindow().getWidth() != previousWidth || mc.getMainWindow().getHeight() != previousHeight || mc.getMainWindow().getGuiScaleFactor() != previousScale)
        {
            overlays.forEach(overlay -> overlay.resize(mc, mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight()));
            previousWidth = mc.getMainWindow().getScaledWidth();
            previousHeight = mc.getMainWindow().getScaledHeight();
            previousScale = mc.getMainWindow().getGuiScaleFactor();
        }

        if (Keystone.isActive())
        {
            if (event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE) return;

            addList.forEach(add -> overlays.add(add));
            addList.clear();

            int mouseX = (int)(mc.mouseHelper.getMouseX() * mc.getMainWindow().getScaledWidth() / mc.getMainWindow().getWidth());
            int mouseY = (int)(mc.mouseHelper.getMouseY() * mc.getMainWindow().getScaledHeight() / mc.getMainWindow().getHeight());
            overlays.forEach(overlay -> overlay.render(event.getMatrixStack(), mouseX, mouseY, event.getPartialTicks()));

            removeList.forEach(remove -> overlays.remove(remove));
            removeList.clear();
        }
    }
    @SubscribeEvent
    public static void mouseClicked(final InputEvent.MouseInputEvent event)
    {
        if (Keystone.isActive())
        {
            Minecraft mc = Minecraft.getInstance();
            double mouseX = mc.mouseHelper.getMouseX() * mc.getMainWindow().getScaledWidth() / mc.getMainWindow().getWidth();
            double mouseY = mc.mouseHelper.getMouseY() * mc.getMainWindow().getScaledHeight() / mc.getMainWindow().getHeight();

            if (event.getAction() == GLFW.GLFW_PRESS)
            {
                for (int i = overlays.size() - 1; i >= 0; i--)
                {
                    Screen overlay = overlays.get(i);
                    for (IGuiEventListener listener : overlay.getEventListeners())
                    {
                        if (listener.mouseClicked(mouseX, mouseY, event.getButton()))
                        {
                            return;
                        }
                    }
                    if (overlay.mouseClicked(mouseX, mouseY, event.getButton())) return;
                }
            }
            if (event.getAction() == GLFW.GLFW_RELEASE)
            {
                for (int i = overlays.size() - 1; i >= 0; i--)
                {
                    Screen overlay = overlays.get(i);
                    for (IGuiEventListener listener : overlay.getEventListeners())
                    {
                        if (listener.mouseReleased(mouseX, mouseY, event.getButton()))
                        {
                            return;
                        }
                    }
                    if (overlay.mouseReleased(mouseX, mouseY, event.getButton())) return;
                }
            }
        }
    }
    @SubscribeEvent
    public static void mouseScrolled(final InputEvent.MouseScrollEvent event)
    {
        if (Keystone.isActive())
        {
            Minecraft mc = Minecraft.getInstance();
            double mouseX = mc.mouseHelper.getMouseX() * mc.getMainWindow().getScaledWidth() / mc.getMainWindow().getWidth();
            double mouseY = mc.mouseHelper.getMouseY() * mc.getMainWindow().getScaledHeight() / mc.getMainWindow().getHeight();

            for (int i = overlays.size() - 1; i >= 0; i--)
            {
                Screen overlay = overlays.get(i);
                for (IGuiEventListener listener : overlay.getEventListeners())
                {
                    if (listener.mouseScrolled(mouseX, mouseY, event.getScrollDelta()))
                    {
                        return;
                    }
                }
                if (overlay.mouseScrolled(mouseX, mouseY, event.getScrollDelta())) return;
            }
        }
    }
    @SubscribeEvent
    public static void keyInput(final InputEvent.KeyInputEvent event)
    {
        if (Keystone.isActive())
        {
            if (event.getAction() == GLFW.GLFW_PRESS)
            {
                for (int i = overlays.size() - 1; i >= 0; i--)
                {
                    Screen overlay = overlays.get(i);
                    for (IGuiEventListener listener : overlay.getEventListeners())
                    {
                        if (listener.keyPressed(event.getKey(), event.getScanCode(), event.getModifiers()))
                        {
                            return;
                        }
                    }
                    if (overlay.keyPressed(event.getKey(), event.getScanCode(), event.getModifiers())) return;
                }
            }
            if (event.getAction() == GLFW.GLFW_RELEASE)
            {
                for (int i = overlays.size() - 1; i >= 0; i--)
                {
                    Screen overlay = overlays.get(i);
                    for (IGuiEventListener listener : overlay.getEventListeners())
                    {
                        if (listener.keyReleased(event.getKey(), event.getScanCode(), event.getModifiers()))
                        {
                            return;
                        }
                    }
                    if (overlay.keyReleased(event.getKey(), event.getScanCode(), event.getModifiers())) return;
                }
            }
        }
    }
}
