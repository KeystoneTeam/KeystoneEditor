package keystone.gui.block_palette;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.gui.AbstractKeystoneOverlay;
import keystone.gui.KeystoneOverlayHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

public class BlockPaletteOverlay extends AbstractKeystoneOverlay
{
    public static final boolean DEBUG_LOG = false;
    private static Stack<Consumer<BlockState>> callbackStack = new Stack<>();

    private final IForgeRegistry<Block> blockRegistry;
    private final IForgeRegistry<Item> itemRegistry;
    private final List<BlockPaletteButton> buttons = new ArrayList<>();
    private int blockCount;

    private BlockPaletteButton highlightedButton;
    private int buttonsPerRow;
    private int buttonsPerColumn;
    private int buttonsInPanel;
    private double scrollOffset;

    private BlockPaletteOverlay()
    {
        super(300, 100, 1320, 880);
        blockRegistry = GameRegistry.findRegistry(Block.class);
        itemRegistry = GameRegistry.findRegistry(Item.class);
    }
    public static void promptBlockStateChoice(Consumer<BlockState> callback)
    {
        callbackStack.push(callback);
        KeystoneOverlayHandler.addOverlay(new BlockPaletteOverlay());
    }

    public IForgeRegistry<Item> getItemRegistry() { return itemRegistry; }
    public void setHighlightedButton(BlockPaletteButton button) { this.highlightedButton = button; }

    private void rebuildButtons()
    {
        buttons.clear();

        int x = this.x + 10;
        int y = this.y + 10;
        buttonsPerRow = (this.width - 20) / 20 + 1;
        buttonsPerColumn = (this.height - 20) / 20 + 1;
        Keystone.LOGGER.info(buttonsPerRow + ", " + buttonsPerColumn);
        buttonsInPanel = buttonsPerRow * buttonsPerColumn;

        blockCount = 0;
        for (Block block : blockRegistry)
        {
            // Create button instance
            BlockPaletteButton button = BlockPaletteButton.create(this, block, x, y);
            if (button == null) continue;
            else blockCount++;

            // Add button if inside panel
            if (blockCount <= (int)scrollOffset * buttonsPerRow) continue;
            if (y <= this.y + this.height - 10) buttons.add(button);

            // Update rendering coordinates
            x += 20;
            if (x >= this.x + this.width - 10)
            {
                x = this.x + 10;
                y += 20;
            }
        }
    }

    @Override
    protected void onWindowSizeChange()
    {
        scrollOffset = 0;
        rebuildButtons();
    }
    @Override
    protected void render(MatrixStack stack)
    {
        if (buttons.size() == 0) rebuildButtons();
        highlightedButton = null;
        
        fill(stack, x, y, x + width, y + height, 0x80000000);
        buttons.forEach(button -> button.render(stack));

        // If more buttons than can fit, draw scrollbar
        if (blockCount > buttonsInPanel)
        {
            fill(stack, x + width, y, x + width + 4, y + height, 0x80000000);

            double rows = Math.ceil(blockCount / (double)buttonsPerRow);
            double normalizedStart = scrollOffset / rows;
            double normalizedEnd = normalizedStart + buttonsPerColumn / rows;
            int handleStart = (int)Math.floor(y + normalizedStart * height);
            int handleEnd = (int)Math.ceil(y + normalizedEnd * height);
            fill(stack, x + width + 2, handleStart, x + width + 4, handleEnd, 0xFF808080);
        }
    }
    @Override
    protected void onMouseInput(final InputEvent.MouseInputEvent event)
    {
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.getAction() == GLFW.GLFW_PRESS)
        {
            if (highlightedButton != null && callbackStack.size() > 0) callbackStack.pop().accept(highlightedButton.onClick());
            KeystoneOverlayHandler.removeOverlay(this);
        }
    }
    @Override
    protected void onMouseScroll(InputEvent.MouseScrollEvent event)
    {
        int rows = (int)Math.ceil(blockCount / (double)buttonsPerRow);

        scrollOffset -= event.getScrollDelta();
        if (scrollOffset < 0) scrollOffset = 0;
        else if (scrollOffset + buttonsPerColumn > rows) scrollOffset = rows - buttonsPerColumn;

        rebuildButtons();
    }
}
