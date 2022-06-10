package keystone.core.gui.screens.block_selection;

import keystone.core.gui.widgets.BlockGridWidget;
import keystone.core.registries.BlockTypeRegistry;
import keystone.core.utils.BlockUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class BlockGridButton extends AbstractBlockButton
{
    public interface ClickConsumer
    {
        void accept(BlockGridButton button, int mouseButton, BlockState state);
    }
    public static ClickConsumer PASS_UNMODIFIED = (button, mouseButton, state) -> button.parent.onEntryClicked(new BlockGridWidget.Entry(state, button.getTooltipBuilder()), mouseButton);
    public static ClickConsumer EDIT_PROPERTIES = (button, mouseButton, state) ->
    {
        button.parent.disableWidgets();
        BlockPropertiesScreen.editBlockProperties(BlockTypeRegistry.fromMinecraftBlock(state), block ->
        {
            if (block != null) button.parent.onEntryClicked(new BlockGridWidget.Entry(block.getMinecraftBlock(), button.getTooltipBuilder()), mouseButton);
            button.parent.restoreWidgets();
        });
    };

    public static final int SIZE = 18;
    protected final BlockGridWidget parent;

    private ClickConsumer leftClickConsumer;
    private ClickConsumer rightClickConsumer;

    protected BlockGridButton(Screen screen, BlockGridWidget parent, ItemStack itemStack, BlockState block, int x, int y, ClickConsumer leftClickConsumer, ClickConsumer rightClickConsumer, IBlockTooltipBuilder tooltipBuilder)
    {
        super(screen, itemStack, block, x, y, SIZE, SIZE, tooltipBuilder);
        this.parent = parent;
        this.leftClickConsumer = leftClickConsumer;
        this.rightClickConsumer = rightClickConsumer;
    }
    public static BlockGridButton create(Screen screen, BlockGridWidget parent, BlockState block, int count, int x, int y, ClickConsumer leftClickMutator, ClickConsumer rightClickMutator, IBlockTooltipBuilder tooltipBuilder)
    {
        Item item = BlockUtils.getBlockItem(block.getBlock());
        if (item == null) return null;
        else return new BlockGridButton(screen, parent, new ItemStack(item, count), block, x, y, leftClickMutator, rightClickMutator, tooltipBuilder);
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
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) leftClickConsumer.accept(this, button, getBlockState());
        else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) rightClickConsumer.accept(this, button, getBlockState());
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        if (active && visible && isHovered())
        {
            if (delta > 0) this.parent.addBlock(this.block, getTooltipBuilder(), true);
            else this.parent.removeBlock(this.block, getTooltipBuilder(), true);
            return true;
        }
        return false;
    }
}
