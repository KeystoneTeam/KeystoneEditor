package keystone.core.gui.widgets.inputs;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.wrappers.blocks.BlockPalette;
import keystone.core.gui.screens.KeystoneOverlay;
import keystone.core.gui.screens.block_selection.BlockPaletteEditScreen;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import keystone.core.utils.BlockUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BlockPaletteWidget extends ButtonNoHotkey
{
    protected final Minecraft mc;
    protected final FontRenderer font;
    protected final Consumer<Widget[]> disableWidgets;
    protected final Runnable restoreWidgets;

    private BlockPalette palette;

    private final IForgeRegistry<Item> itemRegistry;
    private final List<ItemStack> stacks;

    public BlockPaletteWidget(ITextComponent name, int x, int y, int width, BlockPalette value, Consumer<Widget[]> disableWidgets, Runnable restoreWidgets)
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

        this.mc = Minecraft.getInstance();
        this.font = mc.font;
        this.palette = value;

        this.itemRegistry = GameRegistry.findRegistry(Item.class);
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
        this.palette.forEach((block, weight) -> this.stacks.add(new ItemStack(BlockUtils.getBlockItem(block.getFirst().getMinecraftBlock().getBlock(), itemRegistry))));
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

        drawCenteredString(matrixStack, font, getMessage(), x + width / 2, y - getPaletteOffset(), 0xFFFFFF);
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
