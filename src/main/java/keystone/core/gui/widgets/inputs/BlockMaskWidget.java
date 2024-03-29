package keystone.core.gui.widgets.inputs;

import keystone.api.wrappers.blocks.BlockMask;
import keystone.core.gui.WidgetDisabler;
import keystone.core.gui.overlays.KeystoneOverlay;
import keystone.core.gui.overlays.block_selection.BlockMaskEditScreen;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import keystone.core.utils.BlockUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class BlockMaskWidget extends ButtonNoHotkey
{
    protected final MinecraftClient mc;
    protected final TextRenderer font;
    protected final WidgetDisabler widgetDisabler;
    private BlockMask mask;
    
    private final List<ItemStack> stacks;

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
                maskWidget.rebuildStacks();

                maskWidget.onSetValue(maskWidget.mask);
            });
        });

        this.mc = MinecraftClient.getInstance();
        this.font = mc.textRenderer;
        this.widgetDisabler = new WidgetDisabler();

        this.mask = value;
        this.stacks = new ArrayList<>();
        rebuildStacks();
    }
    
    public static int getMaskOffset() { return 11; }
    public static final int getFinalHeight()
    {
        return 31;
    }

    protected void onSetValue(BlockMask value) {  }

    private void rebuildStacks()
    {
        this.stacks.clear();
        this.mask.forEach(variant ->
        {
            ItemStack stack = new ItemStack(BlockUtils.getBlockItem(variant.getMinecraftBlock().getBlock()));
            if (!stack.isEmpty()) this.stacks.add(stack);
        }, anyVariant ->
        {
            ItemStack stack = new ItemStack(BlockUtils.getBlockItem(anyVariant));
            if (!stack.isEmpty()) this.stacks.add(stack);
        });
    }

    @Override
    public int getHeight()
    {
        return getFinalHeight();
    }
    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        int y = this.y + getMaskOffset();
        drawCenteredText(matrixStack, font, getMessage(), x + width / 2, y - getMaskOffset(), 0xFFFFFF);
        fill(matrixStack, x, y, x + width, y + height - getMaskOffset(), 0x80FFFFFF);

        if (mask.isBlacklist()) fill(matrixStack, x + width - 4, y, x + width, y + 4, 0xFF000000);
        else fill(matrixStack, x + width - 4, y, x + width, y + 4, 0xFFFFFFFF);

        int x = this.x + 1;
        for (ItemStack stack : this.stacks)
        {
            if (stack.isEmpty()) continue;
            KeystoneOverlay.drawItem(this, mc, stack, x, y + 1);
            x += 20;
        }
        
        renderTooltip(matrixStack, mouseX, mouseY);
    }

    public BlockMask getMask() { return mask; }
}
