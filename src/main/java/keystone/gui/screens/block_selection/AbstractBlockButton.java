package keystone.gui.screens.block_selection;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractBlockButton extends Button
{
    private static final Map<String, String> blockToItemMap = new HashMap<>();
    static
    {
        blockToItemMap.put("minecraft:air", "minecraft:barrier");
        blockToItemMap.put("minecraft:water", "minecraft:water_bucket");
        blockToItemMap.put("minecraft:lava", "minecraft:lava_bucket");
    }

    protected final Minecraft mc;
    protected final FontRenderer fontRenderer;
    protected final ItemStack itemStack;
    protected final Block block;

    protected AbstractBlockButton(ItemStack itemStack, Block block, int x, int y, int width, int height)
    {
        super(x, y, width, height, itemStack.getDisplayName(), (button) ->
        {
            if (button instanceof AbstractBlockButton)
            {
                AbstractBlockButton paletteButton = (AbstractBlockButton)button;
                paletteButton.onClicked();
            }
        });

        this.mc = Minecraft.getInstance();
        this.fontRenderer = mc.fontRenderer;
        this.itemStack = itemStack;
        this.block = block;
    }
    protected static Item getBlockItem(Block block, IForgeRegistry<Item> itemRegistry)
    {
        Item item = block.asItem();
        if (blockToItemMap.containsKey(block.getRegistryName().toString())) item = itemRegistry.getValue(new ResourceLocation(blockToItemMap.get(block.getRegistryName().toString())));

        if (item == null || item == Items.AIR)
        {
            if (AbstractBlockSelectionScreen.DEBUG_LOG) Keystone.LOGGER.info("No item for block " + block.getRegistryName().toString());
            return null;
        }
        else return item;
    }

    protected abstract void onClicked();

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        if (isHovered())
        {
            Minecraft mc = Minecraft.getInstance();
            List<IFormattableTextComponent> text = new ArrayList<>();
            text.add(block.getTranslatedName());

            fill(matrixStack, x, y, x + width, y + height, 0x80FFFFFF);
            GuiUtils.drawHoveringText(itemStack, matrixStack, text, mouseX, mouseY, mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight(), -1, mc.fontRenderer);
        }
        drawItem(itemStack, x + (width - 18) / 2, y + (height - 18) / 2);
    }

    private void drawItem(ItemStack stack, int x, int y)
    {
        setBlitOffset(200);
        mc.getItemRenderer().zLevel = 200.0F;
        FontRenderer font = stack.getItem().getFontRenderer(stack);
        if (font == null) font = this.fontRenderer;

        mc.getItemRenderer().renderItemAndEffectIntoGUI(stack, x, y);

        setBlitOffset(0);
        mc.getItemRenderer().zLevel = 0.0F;
    }

    protected BlockState getBlockState()
    {
        // TODO: Add state modifier sub-menu
        return block.getDefaultState();
    }
}
