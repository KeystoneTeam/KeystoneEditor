package keystone.core.gui.screens.block_selection;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.core.gui.widgets.BlockGridWidget;
import keystone.core.registries.BlockTypeRegistry;
import keystone.core.utils.BlockUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class BlockGridButton extends AbstractBlockButton
{
    public static final int SIZE = 18;
    protected final BlockGridWidget parent;

    protected BlockGridButton(BlockGridWidget parent, ItemStack itemStack, BlockState block, int x, int y, IBlockTooltipBuilder tooltipBuilder)
    {
        super(itemStack, block, x, y, SIZE, SIZE, tooltipBuilder);
        this.parent = parent;
    }
    public static BlockGridButton create(BlockGridWidget parent, BlockState block, int count, int x, int y, IBlockTooltipBuilder tooltipBuilder)
    {
        Item item = BlockUtils.getBlockItem(block.getBlock(), parent.getItemRegistry());
        if (item == null) return null;
        else return new BlockGridButton(parent, new ItemStack(item, count), block, x, y, tooltipBuilder);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        active = parent.active;
        visible = parent.visible;
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    protected boolean isValidClickButton(int button)
    {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }
    @Override
    protected void onClicked(int button)
    {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) parent.onBlockClicked(getBlockState());
        else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
        {
            parent.disableWidgets();
            BlockPropertiesScreen.editBlockProperties(BlockTypeRegistry.fromMinecraftBlock(getBlockState()), block ->
            {
                if (block != null) parent.onBlockClicked(block.getMinecraftBlock());
                parent.restoreWidgets();
            });
        }
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        if (active && visible && isHovered())
        {
            if (delta > 0) this.parent.addBlock(this.block, true);
            else this.parent.removeBlock(this.block, true);
            return true;
        }
        return false;
    }
}
