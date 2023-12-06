package keystone.core.gui.overlays.block_selection;

import keystone.core.gui.overlays.KeystoneOverlay;
import keystone.core.gui.viewports.ScreenViewports;
import keystone.core.gui.viewports.Viewport;
import keystone.core.gui.widgets.BlockGridWidget;
import keystone.core.modules.filter.blocks.BlockListProvider;
import keystone.core.modules.filter.blocks.BlockTypeProvider;
import keystone.core.modules.filter.blocks.IBlockProvider;
import keystone.core.registries.BlockTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

public abstract class AbstractBlockSelectionScreen extends KeystoneOverlay
{
    public static final boolean DEBUG_LOG = false;

    private BlockGridWidget panel;
    private TextFieldWidget searchBar;

    protected AbstractBlockSelectionScreen(String narrationTitle)
    {
        super(Text.translatable(narrationTitle));
    }

    public void onLeftClick(BlockGridButton button, int mouseButton, IBlockProvider blockProvider) { BlockGridButton.PASS_UNMODIFIED.accept(button, mouseButton, blockProvider); }
    public void onRightClick(BlockGridButton button, int mouseButton, IBlockProvider blockProvider) { BlockGridButton.EDIT_PROPERTIES.accept(button, mouseButton, blockProvider); }
    public boolean onScroll(BlockGridButton button, double delta) { return BlockGridButton.NO_SCROLLING.accept(button, delta); }
    public Viewport getMainPanelViewport() { return ScreenViewports.getViewport(Viewport.MIDDLE, Viewport.MIDDLE).offset(0, 13, 0, 0); }
    public BlockGridWidget createMainPanel()
    {
        return BlockGridWidget.createWithViewport(this, getMainPanelViewport(), false, Text.translatable("keystone.block_selection"), this::onEntrySelected, this::onLeftClick, this::onRightClick, this::onScroll);
    }
    public abstract void onEntrySelected(BlockGridWidget.Entry entry, int mouseButton);

    @Override
    public boolean shouldCloseOnEsc()
    {
        return true;
    }
    @Override
    protected void init()
    {
        // Main Panel
        this.panel = createMainPanel();
        Registries.BLOCK.forEach(block -> this.panel.addBlockProvider(new BlockTypeProvider(BlockTypeRegistry.fromMinecraftBlock(block.getDefaultState())), BlockGridWidget.NAME_TOOLTIP, false));
        Registries.BLOCK.streamTagsAndEntries().forEach(pair -> this.panel.addBlockProvider(new BlockListProvider(pair.getSecond(), null), BlockGridWidget.NAME_TOOLTIP, false));
        this.panel.rebuildButtons();
        addDrawableChild(this.panel);

        this.searchBar = new TextFieldWidget(textRenderer, panel.getX() + 1, panel.getY() - 13, panel.getWidth() - 1, 12, Text.translatable("keystone.search"));
        this.searchBar.setMaxLength(256);
        this.searchBar.setDrawsBackground(false);
        this.searchBar.setText("");
        this.searchBar.setChangedListener((str) -> this.panel.filter(str));
        addDrawableChild(this.searchBar);
        this.setInitialFocus(this.searchBar);
    }

    @Override
    public void resize(MinecraftClient minecraft, int width, int height)
    {
        String s = this.searchBar.getText();
        super.resize(minecraft, width, height);
        this.searchBar.setText(s);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTicks)
    {
        if (panel.getBlockCount() > panel.getButtonsInPanel()) searchBar.setWidth(panel.getWidth() + 3);
        else searchBar.setWidth(panel.getWidth() - 1);
        context.fill(panel.getX(), panel.getY() - 14, panel.getX() + searchBar.getWidth() + 1, panel.getY() - 2, 0x80000000);

        super.render(context, mouseX, mouseY, partialTicks);
    }
}
