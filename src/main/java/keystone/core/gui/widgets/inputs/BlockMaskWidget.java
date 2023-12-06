package keystone.core.gui.widgets.inputs;

import keystone.api.wrappers.blocks.BlockMask;
import keystone.core.gui.WidgetDisabler;
import keystone.core.gui.overlays.KeystoneOverlay;
import keystone.core.gui.overlays.block_selection.BlockMaskEditScreen;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.concurrent.atomic.AtomicInteger;

public class BlockMaskWidget extends ButtonNoHotkey
{
    protected final MinecraftClient mc;
    protected final TextRenderer font;
    protected final WidgetDisabler widgetDisabler;
    private BlockMask mask;
    
    public BlockMaskWidget(Text name, int x, int y, int width, BlockMask value)
    {
        super(x, y, width, getFinalHeight(), name, (button) ->
        {
            BlockMaskWidget maskWidget = (BlockMaskWidget)button;

            maskWidget.widgetDisabler.disableAll();
            BlockMaskEditScreen.editBlockMask(maskWidget.mask, (mask) ->
            {
                maskWidget.widgetDisabler.restoreAll();
                if (mask == null) return;

                maskWidget.mask = mask;
                maskWidget.onSetValue(maskWidget.mask);
            });
        });

        this.mc = MinecraftClient.getInstance();
        this.font = mc.textRenderer;
        this.widgetDisabler = new WidgetDisabler();
        this.mask = value;
    }
    
    public static int getMaskOffset() { return 11; }
    public static final int getFinalHeight()
    {
        return 31;
    }

    protected void onSetValue(BlockMask value) {  }

    @Override
    public int getHeight()
    {
        return getFinalHeight();
    }
    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float partialTicks)
    {
        int y = this.getY() + getMaskOffset();
        context.drawCenteredTextWithShadow(font, getMessage(), getX() + width / 2, y - getMaskOffset(), 0xFFFFFF);
        context.fill(getX(), y, getX() + width, y + height - getMaskOffset(), 0x80FFFFFF);

        if (mask.isBlacklist()) context.fill(getX() + width - 4, y, getX() + width, y + 4, 0xFF000000);
        else context.fill(getX() + width - 4, y, getX() + width, y + 4, 0xFFFFFFFF);
        
        // Render IBlockProvider Displays
        AtomicInteger x = new AtomicInteger(this.getX() + 1);
        this.mask.forEach(variant ->
        {
            KeystoneOverlay.drawItem(context, variant.getDisplayItem(), x.get(), y + 1);
            x.addAndGet(20);
        }, anyVariant ->
        {
            KeystoneOverlay.drawItem(context, anyVariant.getDisplayItem(), x.get(), y + 1);
            x.addAndGet(20);
        });
        
        renderTooltip(context, mouseX, mouseY);
    }

    public BlockMask getMask() { return mask; }
}
