package keystone.core.gui.widgets.inputs;

import keystone.api.wrappers.blocks.BlockPalette;
import keystone.core.gui.screens.KeystoneOverlay;
import keystone.core.gui.screens.block_selection.BlockPaletteEditScreen;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import keystone.core.utils.BlockUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BlockPaletteWidget extends ButtonNoHotkey
{
    protected final MinecraftClient mc;
    protected final TextRenderer font;
    protected final Consumer<ClickableWidget[]> disableWidgets;
    protected final Runnable restoreWidgets;

    private BlockPalette palette;

    private final List<ItemStack> stacks;

    public BlockPaletteWidget(Text name, int x, int y, int width, BlockPalette value, Consumer<ClickableWidget[]> disableWidgets, Runnable restoreWidgets)
    {
        super(x, y, width, getFinalHeight(), name, (button) ->
        {
            BlockPaletteWidget paletteWidget = (BlockPaletteWidget)button;

            paletteWidget.disableWidgets.accept(null);
            BlockPaletteEditScreen.editBlockPalette(paletteWidget.palette, (palette) ->
            {
                paletteWidget.restoreWidgets.run();
                if (palette == null) return;

                paletteWidget.palette = palette;
                paletteWidget.rebuildStacks();

                paletteWidget.onSetValue(palette);
            });
        });

        this.disableWidgets = disableWidgets;
        this.restoreWidgets = restoreWidgets;

        this.mc = MinecraftClient.getInstance();
        this.font = mc.textRenderer;
        this.palette = value;

        this.stacks = new ArrayList<>();
        rebuildStacks();
    }
    public static int getPaletteOffset() { return 11; }
    public static int getFinalHeight()
    {
        return 31;
    }

    protected void onSetValue(BlockPalette value) {  }

    private void rebuildStacks()
    {
        this.stacks.clear();
        this.palette.forEach((block, weight) -> this.stacks.add(new ItemStack(BlockUtils.getBlockItem(block.getFirst().getMinecraftBlock().getBlock()))));
    }

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

        int x = this.x + 1;
        for (ItemStack stack : this.stacks)
        {
            if (stack.isEmpty()) continue;
            KeystoneOverlay.drawItem(this, mc, stack, x, y + 1);
            x += 20;
        }
    }

    public BlockPalette getPalette() { return palette; }
}
