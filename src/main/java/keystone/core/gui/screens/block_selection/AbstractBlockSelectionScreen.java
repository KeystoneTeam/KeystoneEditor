package keystone.core.gui.screens.block_selection;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.core.gui.screens.KeystoneOverlay;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.widgets.BlockGridWidget;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public abstract class AbstractBlockSelectionScreen extends KeystoneOverlay
{
    public static final boolean DEBUG_LOG = false;

    private final IForgeRegistry<Block> blockRegistry;

    private BlockGridWidget panel;
    private TextFieldWidget searchBar;

    protected AbstractBlockSelectionScreen(String narrationTitle)
    {
        super(new TranslationTextComponent(narrationTitle));
        blockRegistry = GameRegistry.findRegistry(Block.class);
    }

    public BlockGridWidget createMainPanel()
    {
        return BlockGridWidget.createWithMargins(KeystoneHotbar.getX(), KeystoneHotbar.getX(), KeystoneHotbar.getHeight(), KeystoneHotbar.getHeight(), true, new TranslationTextComponent("keystone.block_selection"), this::onBlockSelected);
    }
    public abstract void onBlockSelected(BlockState block);

    @Override
    public void closeScreen()
    {
        Minecraft.getInstance().displayGuiScreen(null);
    }
    @Override
    public boolean shouldCloseOnEsc()
    {
        return true;
    }
    @Override
    protected void init()
    {
        this.minecraft.keyboardListener.enableRepeatEvents(true);

        this.panel = createMainPanel();
        blockRegistry.forEach(block -> this.panel.addBlock(block, false));
        this.panel.rebuildButtons();
        this.children.add(this.panel);

        this.searchBar = new TextFieldWidget(font, panel.x + 1, panel.y - 13, panel.getWidth() - 1, 12, new TranslationTextComponent("keystone.search"));
        this.searchBar.setMaxStringLength(256);
        this.searchBar.setEnableBackgroundDrawing(false);
        this.searchBar.setText("");
        this.searchBar.setResponder((str) -> this.panel.filter(str));
        this.children.add(this.searchBar);
        this.setFocusedDefault(this.searchBar);
    }
    @Override
    public void tick()
    {
        this.searchBar.tick();
    }
    @Override
    public void onClose()
    {
        this.minecraft.keyboardListener.enableRepeatEvents(false);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height)
    {
        String s = this.searchBar.getText();
        super.resize(minecraft, width, height);
        this.searchBar.setText(s);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        super.render(stack, mouseX, mouseY, partialTicks);
        panel.render(stack, mouseX, mouseY, partialTicks);

        if (panel.getBlockCount() > panel.getButtonsInPanel()) searchBar.setWidth(panel.getWidth() + 3);
        else searchBar.setWidth(panel.getWidth() - 1);
        fill(stack, panel.x, panel.y - 14, panel.x + searchBar.getWidth() + 1, panel.y - 2, 0x80000000);
        searchBar.render(stack, mouseX, mouseY, partialTicks);
    }
}
