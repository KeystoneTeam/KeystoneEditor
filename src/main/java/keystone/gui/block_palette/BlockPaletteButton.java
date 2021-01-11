package keystone.gui.block_palette;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockPaletteButton
{
    private static final Map<String, String> blockToItemMap = new HashMap<>();
    static
    {
        blockToItemMap.put("minecraft:air", "minecraft:barrier");
        blockToItemMap.put("minecraft:water", "minecraft:water_bucket");
        blockToItemMap.put("minecraft:lava", "minecraft:lava_bucket");
    }

    private final BlockPaletteOverlay parent;
    private final ItemStack itemStack;
    private final Block block;
    private final int x;
    private final int y;

    private BlockPaletteButton(BlockPaletteOverlay parent, ItemStack itemStack, Block block, int x, int y)
    {
        this.parent = parent;
        this.itemStack = itemStack;
        this.block = block;
        this.x = x;
        this.y = y;
    }
    public static BlockPaletteButton create(BlockPaletteOverlay parent, Block block, int x, int y)
    {
        Item item = block.asItem();
        if (blockToItemMap.containsKey(block.getRegistryName().toString())) item = parent.getItemRegistry().getValue(new ResourceLocation(blockToItemMap.get(block.getRegistryName().toString())));

        if (item == null || item == Items.AIR)
        {
            if (BlockPaletteOverlay.DEBUG_LOG) Keystone.LOGGER.info("No item for block " + block.getRegistryName().toString());
            return null;
        }
        else return new BlockPaletteButton(parent, new ItemStack(item), block, x, y);
    }

    public void render(MatrixStack stack)
    {
        if (parent.isMouseInBox(x - 2, y - 2, 18, 18))
        {
            Minecraft mc = Minecraft.getInstance();
            int mouseX = (int)(mc.mouseHelper.getMouseX() / mc.getMainWindow().getGuiScaleFactor());
            int mouseY = (int)(mc.mouseHelper.getMouseY() / mc.getMainWindow().getGuiScaleFactor());
            List<IFormattableTextComponent> text = new ArrayList<>();
            text.add(block.getTranslatedName());

            AbstractGui.fill(stack, x - 2, y - 2, x + 18, y + 18, 0x80FFFFFF);
            GuiUtils.drawHoveringText(itemStack, stack, text, mouseX, mouseY, mc.getMainWindow().getScaledWidth(), mc.getMainWindow().getScaledHeight(), -1, mc.fontRenderer);
            parent.setHighlightedButton(this);
        }
        parent.drawItem(itemStack, x, y);
    }
    public BlockState onClick()
    {
        // TODO: Add state modifier sub-menu
        return block.getDefaultState();
    }
}
