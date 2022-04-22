package keystone.core.gui;

import keystone.api.Keystone;
import keystone.core.KeystoneGlobalState;
import keystone.core.events.keystone.KeystoneInputEvents;
import keystone.core.events.keystone.KeystoneLifecycleEvents;
import keystone.core.events.minecraft.InputEvents;
import keystone.core.gui.screens.KeystoneOverlay;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class KeystoneOverlayHandler
{
    private static boolean worldFinishedLoading = false;
    private static Queue<IKeystoneTooltip> tooltips = new ArrayDeque<>();
    private static List<Screen> overlays = Collections.synchronizedList(new ArrayList<>());
    private static List<Screen> addList = Collections.synchronizedList(new ArrayList<>());
    private static List<Screen> removeList = Collections.synchronizedList(new ArrayList<>());

    private static boolean rendering;
    
    public static void addOverlay(Screen overlay)
    {
        if (!(overlay instanceof KeystoneOverlay)) Keystone.LOGGER.warn("Adding non-KeystoneOverlay screen to Keystone Overlay Handler! This is not recommended.");

        MinecraftClient mc = MinecraftClient.getInstance();
        overlay.init(mc, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
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

    public static void registerEvents()
    {
        KeystoneLifecycleEvents.JOIN.register(world ->
        {
            overlays.clear();
            addList.clear();
            removeList.clear();
            addOverlay(new KeystoneHotbar());
        });
        KeystoneLifecycleEvents.LEAVE.register(() ->
        {
            overlays.clear();
            addList.clear();
            removeList.clear();
        });

        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(KeystoneOverlayHandler::onWorldUnload);
        ClientTickEvents.START_CLIENT_TICK.register(client -> tick());
        InputEvents.MOUSE_CLICKED.register(KeystoneOverlayHandler::mouseClicked);
        InputEvents.MOUSE_SCROLLED.register(KeystoneOverlayHandler::mouseScrolled);
        InputEvents.MOUSE_MOVED.register(KeystoneOverlayHandler::mouseMoved);
        KeystoneInputEvents.MOUSE_DRAG.register(KeystoneOverlayHandler::mouseDragged);
    }

    //region Event Subscribers
    public static void onPreRenderGui()
    {
        if (!worldFinishedLoading)
        {
            addList.forEach(add -> overlays.add(add));
            addList.clear();

            MinecraftClient mc = MinecraftClient.getInstance();
            resize(mc, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
            worldFinishedLoading = true;
        }
    }
    public static void onWorldUnload(ServerPlayerEntity player, ServerWorld origin, ServerWorld destination)
    {
        worldFinishedLoading = false;
    }
    public static void mouseClicked(int button, int action, int modifiers)
    {
        if (Keystone.isActive())
        {
            MinecraftClient mc = MinecraftClient.getInstance();
            double mouseX = mc.mouse.getX() * mc.getWindow().getScaledWidth() / mc.getWindow().getWidth();
            double mouseY = mc.mouse.getY() * mc.getWindow().getScaledHeight() / mc.getWindow().getHeight();

            if (action == GLFW.GLFW_PRESS) mouseClicked(mouseX, mouseY, button);
            if (action == GLFW.GLFW_RELEASE) mouseReleased(mouseX, mouseY, button);
        }
    }
    public static void mouseScrolled(double offsetX, double offsetY)
    {
        if (Keystone.isActive())
        {
            MinecraftClient mc = MinecraftClient.getInstance();
            double mouseX = mc.mouse.getX() * mc.getWindow().getScaledWidth() / mc.getWindow().getWidth();
            double mouseY = mc.mouse.getY() * mc.getWindow().getScaledHeight() / mc.getWindow().getHeight();
            mouseScrolled(mouseX, mouseY, offsetY);
        }
    }
    public static void mouseDragged(int button, double mouseX, double mouseY, double dragX, double dragY, boolean gui)
    {
        if (Keystone.isActive()) mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    //endregion
    //region Event Forwarding
    private static void tick()
    {
        KeystoneGlobalState.BlockingKeys = MinecraftClient.getInstance().currentScreen != null;
        if (MinecraftClient.getInstance().currentScreen != null) return;

        overlays.forEach(screen ->
        {
            screen.tick();
            for (Element listener : screen.children())
            {
                if (listener instanceof TextFieldWidget)
                {
                    if (((TextFieldWidget) listener).isFocused()) KeystoneGlobalState.BlockingKeys = true;
                }
            }
        });
    }
    public static void resize(MinecraftClient minecraft, int width, int height)
    {
        if (MinecraftClient.getInstance().currentScreen != null) return;
        addList.forEach(screen -> screen.resize(minecraft, width, height));
        overlays.forEach(screen -> screen.resize(minecraft, width, height));
    }
    public static void render(MatrixStack matrixStack, float partialTicks)
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        int mouseX = (int)(mc.mouse.getX() * mc.getWindow().getScaledWidth() / mc.getWindow().getWidth());
        int mouseY = (int)(mc.mouse.getY() * mc.getWindow().getScaledHeight() / mc.getWindow().getHeight());

        KeystoneGlobalState.MouseOverGUI = mc.currentScreen != null;
        if (Keystone.isActive())
        {
            rendering = true;
            addList.forEach(add -> overlays.add(add));
            addList.clear();

            // Render overlays
            for (int i = 0; i < overlays.size(); i++)
            {
                if (MinecraftClient.getInstance().currentScreen != null && i > 0) continue;

                matrixStack.push();
                matrixStack.translate(0, 0, i * 200);

                Screen screen = overlays.get(i);
                screen.render(matrixStack, mouseX, mouseY, partialTicks);
                if (!KeystoneGlobalState.MouseOverGUI && screen instanceof KeystoneOverlay) ((KeystoneOverlay) screen).checkMouseOverGui();

                matrixStack.pop();
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
        if (MinecraftClient.getInstance().currentScreen != null) return false;

        for (int i = overlays.size() - 1; i >= 0; i--)
        {
            Screen screen = overlays.get(i);
            if (screen.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return false;
    }
    public static boolean keyReleased(int keyCode, int scanCode, int modifiers)
    {
        if (MinecraftClient.getInstance().currentScreen != null) return false;

        for (int i = overlays.size() - 1; i >= 0; i--)
        {
            Screen screen = overlays.get(i);
            if (screen.keyReleased(keyCode, scanCode, modifiers)) return true;
        }
        return false;
    }
    private static boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (MinecraftClient.getInstance().currentScreen != null) return false;

        for (int i = overlays.size() - 1; i >= 0; i--)
        {
            Screen screen = overlays.get(i);
            if (screen.mouseClicked(mouseX, mouseY, button)) return true;
        }
        return false;
    }
    private static boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if (MinecraftClient.getInstance().currentScreen != null) return false;

        for (int i = overlays.size() - 1; i >= 0; i--)
        {
            Screen screen = overlays.get(i);
            if (screen.mouseReleased(mouseX, mouseY, button)) return true;
        }
        return false;
    }
    private static boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        if (MinecraftClient.getInstance().currentScreen != null) return false;

        for (int i = overlays.size() - 1; i >= 0; i--)
        {
            Screen screen = overlays.get(i);
            if (screen.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        }
        return false;
    }
    private static boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        if (MinecraftClient.getInstance().currentScreen != null) return false;

        for (int i = overlays.size() - 1; i >= 0; i--)
        {
            Screen screen = overlays.get(i);
            if (screen.mouseScrolled(mouseX, mouseY, delta)) return true;
        }
        return false;
    }
    public static boolean charTyped(char codePoint, int modifiers)
    {
        if (MinecraftClient.getInstance().currentScreen != null) return false;

        for (int i = overlays.size() - 1; i >= 0; i--)
        {
            Screen screen = overlays.get(i);
            if (screen.charTyped(codePoint, modifiers)) return true;
        }
        return false;
    }
    private static void mouseMoved(double xPos, double mouseY)
    {
        if (MinecraftClient.getInstance().currentScreen != null) return;

        overlays.forEach(screen -> screen.mouseMoved(xPos, mouseY));
    }
    //endregion
}
