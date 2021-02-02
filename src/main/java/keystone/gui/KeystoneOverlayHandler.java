package keystone.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.gui.screens.hotbar.KeystoneHotbar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
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

    //region Event Subscribers
    @SubscribeEvent
    public static void render(final RenderGameOverlayEvent.Post event)
    {
        if (event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE) return;

        Minecraft mc = Minecraft.getInstance();
        int mouseX = (int)(mc.mouseHelper.getMouseX() * mc.getMainWindow().getScaledWidth() / mc.getMainWindow().getWidth());
        int mouseY = (int)(mc.mouseHelper.getMouseY() * mc.getMainWindow().getScaledHeight() / mc.getMainWindow().getHeight());

        MouseOverGUI = mc.currentScreen != null;
        if (Keystone.isActive()) render(event.getMatrixStack(), mouseX, mouseY, event.getPartialTicks());
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
            double mouseX = mc.mouseHelper.getMouseX() * mc.getMainWindow().getScaledWidth() / mc.getMainWindow().getWidth();
            double mouseY = mc.mouseHelper.getMouseY() * mc.getMainWindow().getScaledHeight() / mc.getMainWindow().getHeight();

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
            double mouseX = mc.mouseHelper.getMouseX() * mc.getMainWindow().getScaledWidth() / mc.getMainWindow().getWidth();
            double mouseY = mc.mouseHelper.getMouseY() * mc.getMainWindow().getScaledHeight() / mc.getMainWindow().getHeight();
            mouseScrolled(mouseX, mouseY, event.getScrollDelta());
        }
    }
    //endregion
    //region Event Forwarding
    private static void tick()
    {
        BlockingKeys = Minecraft.getInstance().currentScreen != null;
        overlays.forEach(screen ->
        {
            screen.tick();
            for (IGuiEventListener listener : screen.getEventListeners())
            {
                if (listener instanceof TextFieldWidget)
                {
                    if (((TextFieldWidget) listener).isFocused()) BlockingKeys = true;
                }
            }
        });
    }
    public static void resize(Minecraft minecraft, int width, int height)
    {
        overlays.forEach(overlay -> overlay.resize(minecraft, width, height));
    }
    private static void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        MouseOverGUI = Minecraft.getInstance().currentScreen != null;

        addList.forEach(add -> overlays.add(add));
        addList.clear();

        overlays.forEach(screen -> screen.render(matrixStack, mouseX, mouseY, partialTicks));

        removeList.forEach(remove -> overlays.remove(remove));
        removeList.clear();
    }

    public static boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        for (Screen screen : overlays) if (screen.keyPressed(keyCode, scanCode, modifiers)) return true;
        return false;
    }
    public static boolean keyReleased(int keyCode, int scanCode, int modifiers)
    {
        for (Screen screen : overlays) if (screen.keyReleased(keyCode, scanCode, modifiers)) return true;
        return false;
    }
    private static boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        for (Screen screen : overlays) if (screen.mouseClicked(mouseX, mouseY, button)) return true;
        return false;
    }
    private static boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        for (Screen screen : overlays) if (screen.mouseReleased(mouseX, mouseY, button)) return true;
        return false;
    }
    private static boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        for (Screen screen : overlays) if (screen.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        return false;
    }
    private static boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        for (Screen screen : overlays) if (screen.mouseScrolled(mouseX, mouseY, delta)) return true;
        return false;
    }
    public static boolean charTyped(char codePoint, int modifiers)
    {
        for (Screen screen : overlays) if (screen.charTyped(codePoint, modifiers)) return true;
        return false;
    }
    private static void mouseMoved(double xPos, double mouseY)
    {
        overlays.forEach(screen -> screen.mouseMoved(xPos, mouseY));
    }
    //endregion
}