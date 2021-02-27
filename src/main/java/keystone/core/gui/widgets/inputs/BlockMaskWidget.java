package keystone.core.gui.widgets.inputs;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.wrappers.BlockMask;
import keystone.core.gui.screens.block_selection.BlockMaskEditScreen;
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

public class BlockMaskWidget extends ButtonNoHotkey
{
    protected final Minecraft mc;
    protected final FontRenderer font;
    protected final Consumer<Widget[]> disableWidgets;
    protected final Runnable restoreWidgets;

    private BlockMask mask;

    private final IForgeRegistry<Item> itemRegistry;
    private final List<ItemStack> stacks;

    public BlockMaskWidget(ITextComponent name, int x, int y, int width, BlockMask value, Consumer<Widget[]> disableWidgets, Runnable restoreWidgets)
    {
        super(x, y + 11, width, 20, name, (button) ->
        {
            BlockMaskWidget maskWidget = (BlockMaskWidget)button;

            maskWidget.disableWidgets.accept(null);
            BlockMaskEditScreen.editBlockMask(maskWidget.mask, (mask) ->
            {
                maskWidget.restoreWidgets.run();
                if (mask == null) return;

                maskWidget.mask = mask;
                maskWidget.rebuildStacks();

                maskWidget.onSetValue(maskWidget.mask);
            });
        });

        this.restoreWidgets = restoreWidgets;
        this.disableWidgets = disableWidgets;

        this.mc = Minecraft.getInstance();
        this.font = mc.fontRenderer;
        this.mask = value;

        this.itemRegistry = GameRegistry.findRegistry(Item.class);
        this.stacks = new ArrayList<>();
        rebuildStacks();
    }
    public static final int getHeight()
    {
        return 31;
    }

    protected void onSetValue(BlockMask value) {  }

    private void rebuildStacks()
    {
        this.stacks.clear();
        this.mask.forEach(block -> this.stacks.add(new ItemStack(BlockUtils.getBlockItem(block.getMinecraftBlock().getBlock(), itemRegistry))));
    }

    @Override
    public int getHeightRealms()
    {
        return getHeight();
    }
    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        drawCenteredString(matrixStack, font, getMessage(), x + width / 2, y - 11, 0xFFFFFF);
        fill(matrixStack, x, y, x + width, y + height, 0x80FFFFFF);

        if (mask.isBlacklist()) fill(matrixStack, x + width - 4, y, x + width, y + 4, 0xFF000000);
        else fill(matrixStack, x + width - 4, y, x + width, y + 4, 0xFFFFFFFF);

        int x = this.x + 1;
        for (ItemStack stack : this.stacks)
        {
            if (stack.isEmpty()) continue;
            drawItem(stack, x, this.y + 1);
            x += 20;
        }
    }
    private void drawItem(ItemStack stack, int x, int y)
    {
        setBlitOffset(200);
        this.mc.getItemRenderer().zLevel = 200.0F;
        FontRenderer font = stack.getItem().getFontRenderer(stack);
        if (font == null) font = this.font;

        mc.getItemRenderer().renderItemAndEffectIntoGUI(stack, x, y);

        setBlitOffset(0);
        mc.getItemRenderer().zLevel = 0.0F;
    }

    public BlockMask getMask() { return mask; }
}
