package keystone.gui;

import keystone.api.Keystone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeystoneOverlayHandler
{
    public static boolean MouseOverGUI = false;

    private static List<AbstractKeystoneOverlay> overlays = new ArrayList<>();
    private static List<AbstractKeystoneOverlay> removeList = new ArrayList<>();

    public static void addOverlay(AbstractKeystoneOverlay overlay) { overlays.add(overlay); }
    public static void removeOverlay(AbstractKeystoneOverlay overlay) { removeList.add(overlay); }

    @SubscribeEvent
    public static void onPreRenderGui(final RenderGameOverlayEvent.Pre event)
    {
        if (Keystone.isActive() && event.getType() == RenderGameOverlayEvent.ElementType.VIGNETTE) event.setCanceled(true);
    }
    @SubscribeEvent
    public static void onRenderGui(final RenderGameOverlayEvent.Post event)
    {
        if (Keystone.isActive())
        {
            if (event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE) return;
            MouseOverGUI = Minecraft.getInstance().currentScreen != null;

            overlays.forEach(overlay -> overlay.doRender(event.getMatrixStack()));
            removeList.forEach(remove -> overlays.remove(remove));
            removeList.clear();
        }
    }
    @SubscribeEvent
    public static void onMouseInput(final InputEvent.MouseInputEvent event)
    {
        if (Keystone.isActive())
        {
            overlays.forEach(overlay -> overlay.onMouseInput(event));
            removeList.forEach(remove -> overlays.remove(remove));
            removeList.clear();
        }
    }
    @SubscribeEvent
    public static void onMouseScroll(final InputEvent.MouseScrollEvent event)
    {
        if (Keystone.isActive())
        {
            overlays.forEach(overlay -> overlay.onMouseScroll(event));
            removeList.forEach(remove -> overlays.remove(remove));
            removeList.clear();
        }
    }
}
