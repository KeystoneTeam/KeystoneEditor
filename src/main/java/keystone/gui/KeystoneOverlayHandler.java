package keystone.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.gui.block_palette.BlockPaletteOverlay;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
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

    static
    {
        addOverlay(new AbstractKeystoneOverlay(16, 290, 200, 500)
        {
            @Override
            public void render(MatrixStack stack)
            {
                if (isMouseInBox(normalizedPosition, normalizedSize)) fill(stack, x, y, x + width, y + height, 0x80FFFFFF);
                else fill(stack, x, y, x + width, y + height, 0x80000000);
                drawItem(new ItemStack(Blocks.STONE), x + width / 2 - 9, y + height / 2 - 9);
            }
        });

        // TEST
        BlockPaletteOverlay.promptBlockStateChoice(block -> Keystone.LOGGER.info(block.getBlock().getRegistryName().toString()));
    }

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
            MouseOverGUI = false;

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
}
