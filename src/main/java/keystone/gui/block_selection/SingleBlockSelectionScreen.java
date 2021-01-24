package keystone.gui.block_selection;

import keystone.gui.types.AbstractBlockSelectionScreen;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;

import java.util.function.Consumer;

public class SingleBlockSelectionScreen extends AbstractBlockSelectionScreen
{
    private final Consumer<BlockState> callback;

    protected SingleBlockSelectionScreen(Consumer<BlockState> callback)
    {
        super("keystone.block_selection.title");
        this.callback = callback;
    }
    public static void promptBlockStateChoice(Consumer<BlockState> callback)
    {
        Minecraft.getInstance().displayGuiScreen(new SingleBlockSelectionScreen(callback));
    }

    @Override
    public void onBlockSelected(BlockState block)
    {
        closeScreen();
        callback.accept(block);
    }
}
