package keystone.core.gui.widgets.inputs;

import keystone.api.wrappers.blocks.BlockPalette;
import keystone.core.gui.WidgetDisabler;
import keystone.core.gui.overlays.KeystoneOverlay;
import keystone.core.gui.overlays.block_selection.BlockPaletteEditScreen;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.concurrent.atomic.AtomicInteger;

public class BlockPaletteWidget extends ButtonNoHotkey
{
    protected final MinecraftClient mc;
    protected final TextRenderer font;
    protected final WidgetDisabler widgetDisabler;
    private BlockPalette palette;
    
    public BlockPaletteWidget(Text name, int x, int y, int width, BlockPalette value)
    {
        super(x, y, width, getFinalHeight(), name, (button) ->
        {
            BlockPaletteWidget paletteWidget = (BlockPaletteWidget)button;

            paletteWidget.widgetDisabler.disableAll();
            BlockPaletteEditScreen.editBlockPalette(paletteWidget.palette, (palette) ->
            {
                paletteWidget.widgetDisabler.restoreAll();
                if (palette == null) return;

                paletteWidget.palette = palette;
                paletteWidget.onSetValue(palette);
            });
        });

        this.mc = MinecraftClient.getInstance();
        this.font = mc.textRenderer;
        this.widgetDisabler = new WidgetDisabler();
        this.palette = value;
    }
    
    public static int getPaletteOffset() { return 11; }
    public static int getFinalHeight()
    {
        return 31;
    }

    protected void onSetValue(BlockPalette value) {  }

    @Override
    public int getHeight()
    {
        return getFinalHeight();
    }
    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        int y = this.y + getPaletteOffset();

        drawCenteredText(matrixStack, font, getMessage(), x + width / 2, y - getPaletteOffset(), 0xFFFFFF);
        fill(matrixStack, x, y, x + width, y + height - getPaletteOffset(), 0x80FFFFFF);

        // Render IBlockProvider Displays
        AtomicInteger x = new AtomicInteger(this.x + 1);
        this.palette.forEach(((provider, integer) ->
        {
            ItemStack display = provider.getDisplayItem();
            display.setCount(integer);
            KeystoneOverlay.drawItem(this, mc, display, x.get(), y + 1);
            x.addAndGet(20);
        }));
    
        renderTooltip(matrixStack, mouseX, mouseY);
    }
    
    public BlockPalette getPalette() { return palette; }
}
