package keystone.core.gui.overlays.block_selection;

import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.viewports.ScreenViewports;
import keystone.core.gui.viewports.Viewport;
import keystone.core.gui.widgets.BlockGridWidget;
import net.minecraft.block.BlockState;
import net.minecraft.text.Text;

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
    public static SingleBlockSelectionScreen promptBlockStateChoice(Consumer<BlockState> callback)
    {
        SingleBlockSelectionScreen prompt = new SingleBlockSelectionScreen(callback);
        KeystoneOverlayHandler.addOverlay(prompt);
        return prompt;
    }

    @Override
    public void removed()
    {
        if (!ranCallback)
        {
            callback.accept(null);
            ranCallback = true;
        }
        super.removed();
    }

    @Override
    public BlockGridWidget createMainPanel()
    {
        return BlockGridWidget.createWithViewport(this, ScreenViewports.getViewport(Viewport.MIDDLE, Viewport.LEFT, Viewport.MIDDLE, Viewport.RIGHT).offset(75, 13, -75, 0), false, Text.translatable("keystone.block_selection"), this::onEntrySelected, this::onLeftClick, this::onRightClick, this::onScroll);
    }
    @Override
    public void onEntrySelected(BlockGridWidget.Entry entry, int mouseButton)
    {
        callback.accept(entry.state());
        ranCallback = true;
        close();
    }
}
