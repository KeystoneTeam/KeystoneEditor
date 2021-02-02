package keystone.gui.screens.filters;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.api.filters.Variable;
import keystone.api.wrappers.Block;
import keystone.api.wrappers.BlockMask;
import keystone.core.utils.BlockUtils;
import keystone.gui.screens.block_selection.SingleBlockSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class BlockMaskVariableWidget extends Button
{
    private final Minecraft mc;
    private final FontRenderer font;
    private final FilterSelectionScreen parent;
    private final Variable variable;
    private final Field field;
    private final String name;

    private final BlockMask mask;

    private final IForgeRegistry<Item> itemRegistry;
    private final List<ItemStack> stacks;

    public BlockMaskVariableWidget(FilterSelectionScreen parent, Variable variable, Field field, String name, int x, int y, int width) throws IllegalAccessException
    {
        super(x, y + 11, width, 20, new StringTextComponent(name), (button) ->
        {
            BlockMaskVariableWidget paletteWidget = (BlockMaskVariableWidget)button;

            paletteWidget.parent.disableWidgets();
            SingleBlockSelectionScreen.promptBlockStateChoice(block ->
            {
                if (block != null) paletteWidget.mask.with(new Block(block));
                paletteWidget.rebuildStacks();
                paletteWidget.parent.restoreWidgets();

                try
                {
                    paletteWidget.field.set(paletteWidget.parent.getFilterInstance(), paletteWidget.mask);
                }
                catch (IllegalAccessException e)
                {
                    String error = "Could not set BlockPalette variable '" + paletteWidget.name + "'!";
                    Keystone.LOGGER.error(error);
                    Minecraft.getInstance().player.sendMessage(new StringTextComponent(error).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
                    e.printStackTrace();
                }
            });
        });

        this.mc = Minecraft.getInstance();
        this.font = mc.fontRenderer;
        this.parent = parent;
        this.variable = variable;
        this.field = field;
        this.name = name;
        this.mask = (BlockMask) field.get(parent.getFilterInstance());

        this.itemRegistry = GameRegistry.findRegistry(Item.class);
        this.stacks = new ArrayList<>();
        rebuildStacks();
    }
    public static final int getHeight()
    {
        return 31;
    }

    private void rebuildStacks()
    {
        this.stacks.clear();
        this.mask.forEach(block -> this.stacks.add(new ItemStack(BlockUtils.getBlockItem(block.getMinecraftBlock().getBlock(), itemRegistry))));
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        drawCenteredString(matrixStack, font, name, x + width / 2, y - 11, 0xFFFFFF);
        fill(matrixStack, x, y, x + width, y + height, 0x80FFFFFF);

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
