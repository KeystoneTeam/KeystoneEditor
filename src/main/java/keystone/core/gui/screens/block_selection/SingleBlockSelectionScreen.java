package keystone.core.gui.screens.block_selection;

import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.widgets.BlockGridWidget;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Consumer;

public class SingleBlockSelectionScreen extends AbstractBlockSelectionScreen
{
    private boolean ranCallback = false;
    private final Consumer<BlockState> callback;

    protected SingleBlockSelectionScreen(Consumer<BlockState> callback)
    {
        super("keystone.screen.blockSelection");
        this.callback = callback;
    }
    public static void promptBlockStateChoice(Consumer<BlockState> callback)
    {
        KeystoneOverlayHandler.addOverlay(new SingleBlockSelectionScreen(callback));
    }

    @Override
    public void onClose()
    {
        if (!ranCallback)
        {
            callback.accept(null);
            ranCallback = true;
        }
        super.onClose();
    }

    @Override
    public BlockGridWidget createMainPanel()
    {
        return BlockGridWidget.createWithMargins(75, 75, 25, KeystoneHotbar.getHeight(), false, new TranslationTextComponent("keystone.block_selection"), this::onBlockSelected, this::disableWidgets, this::restoreWidgets);
    }
    @Override
    public void onBlockSelected(BlockState block)
    {
        callback.accept(block);
        ranCallback = true;
        closeScreen();
    }
}
