package keystone.core.gui.widgets.inputs;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.wrappers.BlockMask;
import keystone.core.gui.screens.KeystoneOverlay;
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
        this.font = mc.font;
        this.mask = value;

        this.itemRegistry = GameRegistry.findRegistry(Item.class);
        this.stacks = new ArrayList<>();
        rebuildStacks();
    }
    public static final int getFinalHeight()
    {
        return 31;
    }

    protected void onSetValue(BlockMask value) {  }

    private void rebuildStacks()
    {
        this.stacks.clear();
        this.mask.forEach(block ->
        {
            ItemStack stack = new ItemStack(BlockUtils.getBlockItem(block.getMinecraftBlock().getBlock(), itemRegistry));
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
        drawCenteredString(matrixStack, font, getMessage(), x + width / 2, y - 11, 0xFFFFFF);
        fill(matrixStack, x, y, x + width, y + height, 0x80FFFFFF);

        if (mask.isBlacklist()) fill(matrixStack, x + width - 4, y, x + width, y + 4, 0xFF000000);
        else fill(matrixStack, x + width - 4, y, x + width, y + 4, 0xFFFFFFFF);

        int x = this.x + 1;
        for (ItemStack stack : this.stacks)
        {
            if (stack.isEmpty()) continue;
            KeystoneOverlay.drawItem(this, mc, stack, x, this.y + 1);
            x += 20;
        }
    }

    public BlockMask getMask() { return mask; }
}
