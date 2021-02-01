package keystone.gui.screens.filters;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.api.filters.FilterVariable;
import keystone.api.wrappers.Block;
import keystone.api.wrappers.BlockPalette;
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

public class BlockPaletteVariableWidget extends Button
{
    private final Minecraft mc;
    private final FontRenderer font;
    private final FilterSelectionScreen parent;
    private final FilterVariable variable;
    private final Field field;
    private final String name;
    private final BlockPalette palette;

    private final IForgeRegistry<Item> itemRegistry;
    private final List<ItemStack> stacks;

    public BlockPaletteVariableWidget(FilterSelectionScreen parent, FilterVariable variable, Field field, String name, int x, int y, int width, BlockPalette palette)
    {
        super(x, y, width, getHeight(), new StringTextComponent(name), (button) ->
        {
            BlockPaletteVariableWidget paletteWidget = (BlockPaletteVariableWidget)button;

            paletteWidget.parent.disableWidgets();
            SingleBlockSelectionScreen.promptBlockStateChoice(block ->
            {
                paletteWidget.palette.with(new Block(block));
                paletteWidget.rebuildStacks();
                paletteWidget.parent.restoreWidgets();

                try
                {
                    paletteWidget.field.set(paletteWidget.parent.getFilterInstance(), paletteWidget.palette);
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
        this.palette = palette;

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
        this.palette.forEach((block, weight) -> this.stacks.add(new ItemStack(BlockUtils.getBlockItem(block.getMinecraftBlock().getBlock(), itemRegistry))));
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        drawCenteredString(matrixStack, font, name, x + width / 2, y, 0xFFFFFF);
        fill(matrixStack, x, y + 11, x + width, y + height, 0x80FFFFFF);

        int x = this.x + 1;
        for (ItemStack stack : this.stacks)
        {
            drawItem(stack, x, this.y + 12);
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

    public BlockPalette getPalette() { return palette; }
}
