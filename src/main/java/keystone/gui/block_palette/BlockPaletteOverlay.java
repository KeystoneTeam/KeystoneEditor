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
    private static Stack<Consumer<BlockState>> callbackStack = new Stack<>();

    private final IForgeRegistry<Block> blockRegistry;
    private final IForgeRegistry<Item> itemRegistry;
    private final List<BlockPaletteButton> buttons = new ArrayList<>();
    private BlockPaletteButton highlightedButton;

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
        for (Block block : blockRegistry)
        {
            BlockPaletteButton button = BlockPaletteButton.create(this, block, x, y);
            if (button == null) continue;
            else buttons.add(button);

            // Update rendering coordinates
            x += 20;
            if (x >= this.x + this.width - 10)
            {
                x = this.x + 10;
                y += 20;
            }
            if (y >= this.y + this.height - 10) break;
        }
    }

    @Override
    protected void onWindowSizeChange()
    {
        rebuildButtons();
    }
    @Override
    protected void render(MatrixStack stack)
    {
        if (buttons.size() == 0) rebuildButtons();
        highlightedButton = null;

        fill(stack, x, y, x + width, y + height, 0x80000000);
        buttons.forEach(button -> button.render(stack));
    }
    @Override
    protected void onMouseInput(final InputEvent.MouseInputEvent event)
    {
        if (Keystone.isActive() && event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.getAction() == GLFW.GLFW_PRESS)
        {
            if (highlightedButton != null && callbackStack.size() > 0) callbackStack.pop().accept(highlightedButton.onClick());
            KeystoneOverlayHandler.removeOverlay(this);
        }
    }
}
