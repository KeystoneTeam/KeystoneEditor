package keystone.core.gui.screens.block_selection;

import keystone.core.gui.screens.KeystoneOverlay;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.widgets.BlockGridWidget;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;

public abstract class AbstractBlockSelectionScreen extends KeystoneOverlay
{
    public static final boolean DEBUG_LOG = false;

    private BlockGridWidget panel;
    private TextFieldWidget searchBar;

    protected AbstractBlockSelectionScreen(String narrationTitle)
    {
        super(Text.translatable(narrationTitle));
    }

    public BlockGridWidget createMainPanel()
    {
        return BlockGridWidget.createWithMargins(this, KeystoneHotbar.getX(), KeystoneHotbar.getX(), KeystoneHotbar.getHeight(), KeystoneHotbar.getHeight(), false, Text.translatable("keystone.block_selection"), this::onBlockSelected, this::disableWidgets, this::restoreWidgets, BlockGridWidget.NAME_TOOLTIP);
    }
    public abstract void onBlockSelected(BlockState block);

    @Override
    public boolean shouldCloseOnEsc()
    {
        return true;
    }
    @Override
    protected void init()
    {
        this.client.keyboard.setRepeatEvents(true);

        this.panel = createMainPanel();
        Registry.BLOCK.forEach(block -> this.panel.addBlock(block.getDefaultState(), false));
        this.panel.rebuildButtons();
        addDrawableChild(this.panel);

        this.searchBar = new TextFieldWidget(textRenderer, panel.x + 1, panel.y - 13, panel.getWidth() - 1, 12, Text.translatable("keystone.search"));
        this.searchBar.setMaxLength(256);
        this.searchBar.setDrawsBackground(false);
        this.searchBar.setText("");
        this.searchBar.setChangedListener((str) -> this.panel.filter(str));
        addDrawableChild(this.searchBar);
        this.setInitialFocus(this.searchBar);
    }
    @Override
    public void removed()
    {
        this.client.keyboard.setRepeatEvents(false);
    }

    @Override
    public void resize(MinecraftClient minecraft, int width, int height)
    {
        String s = this.searchBar.getText();
        super.resize(minecraft, width, height);
        this.searchBar.setText(s);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        if (panel.getBlockCount() > panel.getButtonsInPanel()) searchBar.setWidth(panel.getWidth() + 3);
        else searchBar.setWidth(panel.getWidth() - 1);
        fill(stack, panel.x, panel.y - 14, panel.x + searchBar.getWidth() + 1, panel.y - 2, 0x80000000);

        super.render(stack, mouseX, mouseY, partialTicks);
    }
}
