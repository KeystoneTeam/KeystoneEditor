package keystone.core.gui;

import keystone.api.Keystone;
import keystone.core.KeystoneGlobalState;
import keystone.core.events.keystone.KeystoneInputEvents;
import keystone.core.events.keystone.KeystoneLifecycleEvents;
import keystone.core.events.minecraft.InputEvents;
import keystone.core.gui.hotbar.KeystoneHotbar;
import keystone.core.gui.overlays.KeystoneHudOverlay;
import keystone.core.gui.overlays.KeystoneOverlay;
import keystone.core.gui.viewports.ScreenViewports;
import keystone.core.gui.widgets.IMouseBlocker;
import keystone.core.modules.hotkeys.HotkeysModule;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.Window;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.joml.Vector4i;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;

public final class KeystoneOverlayHandler
{
    private static boolean worldFinishedLoading = false;
    private static final Queue<IKeystoneTooltip> tooltips = new ArrayDeque<>();
    private static final List<Screen> overlays = Collections.synchronizedList(new ArrayList<>());
    private static final List<Screen> addList = Collections.synchronizedList(new ArrayList<>());
    private static final List<Screen> removeList = Collections.synchronizedList(new ArrayList<>());

    private static boolean rendering;
    
    private static final Queue<Vector4i> BOX_POOL = new ArrayDeque<>();
    private static final List<Vector4i> MOUSE_BLOCKING_REGIONS = new ArrayList<>();
    
    public static boolean isOverlayOpen(Screen overlay)
    {
        return overlays.contains(overlay) || addList.contains(overlay);
    }
    public static boolean isOverlayOpen(Class<? extends Screen> clazz)
    {
        for (Screen overlay : overlays) if (overlay.getClass().equals(clazz)) return true;
        for (Screen overlay : addList) if (overlay.getClass().equals(clazz)) return true;
        return false;
    }
    public static void addOverlay(Screen overlay)
    {
        if (overlay instanceof KeystoneOverlay casted) Keystone.getModule(HotkeysModule.class).addHotkeySet(casted.getHotkeySet());
        else Keystone.LOGGER.warn("Adding non-KeystoneOverlay screen to Keystone Overlay Handler! This is not recommended.");

        MinecraftClient mc = MinecraftClient.getInstance();
        overlay.init(mc, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
        addList.add(overlay);
    }
    public static void addUniqueOverlay(Screen overlay)
    {
        if (!isOverlayOpen(overlay)) addOverlay(overlay);
    }
    public static void removeOverlay(Screen overlay)
    {
        if (!isOverlayOpen(overlay)) return;
        if (overlay instanceof KeystoneOverlay casted) Keystone.getModule(HotkeysModule.class).removeHotkeySet(casted.getHotkeySet());
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
    public static boolean escapeKeyInUse()
    {
        if (MinecraftClient.getInstance().currentScreen != null && MinecraftClient.getInstance().currentScreen.shouldCloseOnEsc()) return true;
        for (Screen overlay : overlays) if (overlay.shouldCloseOnEsc()) return true;
        return false;
    }
    public static boolean isMouseOverGUI()
    {
        MinecraftClient client = MinecraftClient.getInstance();
        Mouse mouse = MinecraftClient.getInstance().mouse;
        Window window = client.getWindow();
        double mouseX = mouse.getX() * (double)window.getScaledWidth() / (double)window.getWidth();
        double mouseY = mouse.getY() * (double)window.getScaledHeight() / (double)window.getHeight();
    
        if (MinecraftClient.getInstance().currentScreen != null) return true;
        else
        {
            // Query Blocking Regions
            for (Vector4i blockingRegion : MOUSE_BLOCKING_REGIONS) if (mouseX >= blockingRegion.x && mouseY >= blockingRegion.y && mouseX <= blockingRegion.z && mouseY <= blockingRegion.w) return true;
            
            // Query Overlays
            for (Screen overlay : overlays) if (overlay instanceof IMouseBlocker blocker && blocker.isMouseBlocked(mouseX, mouseY)) return true;
            
            return false;
        }
    }
    public static void addMouseBlockingRegion(int x1, int y1, int x2, int y2)
    {
        Vector4i box = BOX_POOL.isEmpty() ? new Vector4i() : BOX_POOL.remove();
        box.set(Math.min(x1, x2), Math.min(y1, y2), Math.max(x1, x2), Math.max(y1, y2));
        MOUSE_BLOCKING_REGIONS.add(box);
    }

    public static void registerEvents()
    {
        KeystoneLifecycleEvents.OPEN_WORLD.register(world ->
        {
            overlays.clear();
            addList.clear();
            removeList.clear();
            addUniqueOverlay(KeystoneHotbar.INSTANCE);
            addUniqueOverlay(KeystoneHudOverlay.INSTANCE);
        });
        KeystoneLifecycleEvents.CLOSE_WORLD.register(() ->
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
        if (MinecraftClient.getInstance().currentScreen != null) return;
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
        if (MinecraftClient.getInstance().currentScreen != null) return;
        if (Keystone.isActive())
        {
            MinecraftClient mc = MinecraftClient.getInstance();
            double mouseX = mc.mouse.getX() * mc.getWindow().getScaledWidth() / mc.getWindow().getWidth();
            double mouseY = mc.mouse.getY() * mc.getWindow().getScaledHeight() / mc.getWindow().getHeight();
            mouseScrolled(mouseX, mouseY, offsetX, offsetY);
        }
    }
    public static void mouseDragged(int button, double mouseX, double mouseY, double dragX, double dragY, boolean gui)
    {
        if (MinecraftClient.getInstance().currentScreen != null) return;
        if (Keystone.isActive()) mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    //endregion
    //region Event Forwarding
    private static void tick()
    {
        KeystoneGlobalState.GuiConsumingKeys = MinecraftClient.getInstance().currentScreen != null;
        if (MinecraftClient.getInstance().currentScreen != null) return;

        overlays.forEach(screen ->
        {
            screen.tick();
            for (Element listener : screen.children())
            {
                if (listener instanceof TextFieldWidget)
                {
                    if (((TextFieldWidget) listener).isFocused()) KeystoneGlobalState.GuiConsumingKeys = true;
                }
            }
        });
    }
    public static void resize(MinecraftClient minecraft, int width, int height)
    {
        if (MinecraftClient.getInstance().currentScreen != null) return;
        ScreenViewports.refreshViewports();
        addList.forEach(screen -> screen.resize(minecraft, width, height));
        overlays.forEach(screen -> screen.resize(minecraft, width, height));
    }
    public static void render(MinecraftClient client, DrawContext context, int mouseX, int mouseY, float partialTicks)
    {
        if (Keystone.isEnabled())
        {
            // Add Queued Overlays
            rendering = true;
            overlays.addAll(addList);
            addList.clear();
            
            // Reset Mouse Blocking Regions
            BOX_POOL.addAll(MOUSE_BLOCKING_REGIONS);
            MOUSE_BLOCKING_REGIONS.clear();
    
            if (MinecraftClient.getInstance().currentScreen == null)
            {
                // Render overlays
                for (int i = 0; i < overlays.size(); i++)
                {
                    if (MinecraftClient.getInstance().currentScreen != null && i > 0) continue;
        
                    context.getMatrices().push();
                    context.getMatrices().translate(0, 0, i * 55);
        
                    Screen screen = overlays.get(i);
                    screen.render(context, mouseX, mouseY, partialTicks);

                    context.getMatrices().pop();
                }
    
                // Render tooltips
                context.getMatrices().push();
                context.getMatrices().translate(0, 0, overlays.size() * 55);
                IKeystoneTooltip tooltip = tooltips.poll();
                while (tooltip != null)
                {
                    tooltip.render(context, client.textRenderer, mouseX, mouseY, partialTicks);
                    tooltip = tooltips.poll();
                }
                context.getMatrices().pop();
            }

            // Remove Queued Overlays
            removeList.forEach(overlays::remove);
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
    private static boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount)
    {
        if (MinecraftClient.getInstance().currentScreen != null) return false;

        for (int i = overlays.size() - 1; i >= 0; i--)
        {
            Screen screen = overlays.get(i);
            if (screen.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
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
    //region Helpers
    public static void forEachElement(Set<Element> ignoreSet, Consumer<Element> consumer, boolean acceptLists)
    {
        overlays.forEach(overlay -> overlay.children().forEach(element ->
        {
            if (!ignoreSet.contains(element))
            {
                if (element instanceof ParentElement list)
                {
                    list.children().forEach(consumer);
                    if (acceptLists) consumer.accept(list);
                }
                else consumer.accept(element);
            }
        }));
    }
    public static void forEachClickable(Set<Element> ignoreSet, Consumer<ClickableWidget> consumer, boolean acceptLists)
    {
        forEachElement(ignoreSet, element -> { if (element instanceof ClickableWidget clickable) consumer.accept(clickable); }, acceptLists);
    }
    //endregion
}
