package keystone.core.gui.overlays.block_selection;

import keystone.core.gui.widgets.BlockGridWidget;
import keystone.core.modules.filter.providers.IBlockProvider;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;

public class BlockGridButton extends AbstractBlockButton
{
    public static final int SIZE = 18;
    
    //region Consumer Types
    public interface ClickConsumer
    {
        void accept(BlockGridButton button, int mouseButton, IBlockProvider blockProvider);
    }
    public interface ScrollConsumer
    {
        boolean accept(BlockGridButton button, double delta);
    }
    //endregion
    //region Default Consumers
    public static ClickConsumer PASS_UNMODIFIED = (button, mouseButton, blockProvider) -> button.parent.onEntryClicked(new BlockGridWidget.Entry(blockProvider, button.getTooltipBuilder()), mouseButton);
    public static ClickConsumer EDIT_PROPERTIES = (button, mouseButton, blockProvider) ->
    {
        PASS_UNMODIFIED.accept(button, mouseButton, blockProvider);
        
        // TODO: Implement EDIT_PROPERTIES
//        button.parent.disableWidgets();
//        blockProvider.openEditPropertiesScreen();
    };
    public static ScrollConsumer NO_SCROLLING = (button, delta) -> false;
    public static ScrollConsumer CHANGE_AMOUNT = (button, delta) ->
    {
        button.count += delta;
        if (delta > 0) button.parent.addBlockProvider(button.blockProvider, button.getTooltipBuilder(), true);
        else button.parent.removeBlockProvider(button.blockProvider, button.getTooltipBuilder(), true);
        return true;
    };
    //endregion
    
    protected final BlockGridWidget parent;
    private final ClickConsumer leftClickConsumer;
    private final ClickConsumer rightClickConsumer;
    private final ScrollConsumer scrollConsumer;
    
    protected BlockGridButton(Screen screen, BlockGridWidget parent, IBlockProvider blockProvider, int count, int x, int y, ClickConsumer leftClickConsumer, ClickConsumer rightClickConsumer, ScrollConsumer scrollConsumer, IBlockTooltipBuilder tooltipBuilder)
    {
        super(screen, blockProvider, x, y, SIZE, SIZE, tooltipBuilder);
        this.parent = parent;
        this.leftClickConsumer = leftClickConsumer;
        this.rightClickConsumer = rightClickConsumer;
        this.scrollConsumer = scrollConsumer;
        this.count = count;
    }
    public static BlockGridButton create(Screen screen, BlockGridWidget parent, IBlockProvider blockProvider, int count, int x, int y, ClickConsumer leftClickMutator, ClickConsumer rightClickMutator, ScrollConsumer scrollConsumer, IBlockTooltipBuilder tooltipBuilder)
    {
        return new BlockGridButton(screen, parent, blockProvider, count, x, y, leftClickMutator, rightClickMutator, scrollConsumer, tooltipBuilder);
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
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) leftClickConsumer.accept(this, button, getBlockProvider());
        else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) rightClickConsumer.accept(this, button, getBlockProvider());
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        if (active && visible && isHovered()) return scrollConsumer.accept(this, delta);
        return false;
    }
}
