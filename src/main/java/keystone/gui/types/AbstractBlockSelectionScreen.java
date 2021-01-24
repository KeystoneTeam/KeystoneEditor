package keystone.gui.types;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.core.KeystoneMod;
import keystone.gui.block_selection.BlockSelectionButton;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.item.Item;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public abstract class AbstractBlockSelectionScreen extends Screen
{
    public static final boolean DEBUG_LOG = false;

    private final IForgeRegistry<Block> blockRegistry;
    private final IForgeRegistry<Item> itemRegistry;
    private int blockCount;

    private final int idealLeftMargin;
    private final int idealRightMargin;
    private final int idealTopMargin;
    private final int idealBottomMargin;

    private int panelOffsetX;
    private int panelOffsetY;
    private int panelWidth;
    private int panelHeight;
    private int buttonsPerRow;
    private int buttonsPerColumn;
    private int buttonsInPanel;
    private double scrollOffset;

    private TextFieldWidget searchBar;
    private boolean clearedSearch;

    protected AbstractBlockSelectionScreen(String narrationTitle) { this(narrationTitle, 75, 75, 25, 25); }
    protected AbstractBlockSelectionScreen(String narrationTitle, int idealLeftMargin, int idealRightMargin, int idealTopMargin, int idealBottomMargin)
    {
        super(new TranslationTextComponent(narrationTitle));

        blockRegistry = GameRegistry.findRegistry(Block.class);
        itemRegistry = GameRegistry.findRegistry(Item.class);

        this.idealLeftMargin = idealLeftMargin;
        this.idealRightMargin = idealRightMargin;
        this.idealTopMargin = idealTopMargin;
        this.idealBottomMargin = idealBottomMargin;
    }

    public IForgeRegistry<Item> getItemRegistry() { return itemRegistry; }
    public abstract void onBlockSelected(BlockState block);

    @Override
    protected void init()
    {
        this.minecraft.keyboardListener.enableRepeatEvents(true);

        scrollOffset = 0;

        panelWidth = width - idealLeftMargin - idealRightMargin;
        panelWidth -= panelWidth % BlockSelectionButton.SIZE;
        panelHeight = height - idealTopMargin - idealBottomMargin;
        panelHeight -= panelHeight % BlockSelectionButton.SIZE;

        panelOffsetX = (int)Math.floor((width - panelWidth) * (idealLeftMargin / (float)(idealLeftMargin + idealRightMargin)));
        panelOffsetY = (int)Math.floor((height - panelHeight) * (idealTopMargin / (float)(idealTopMargin + idealBottomMargin)));

        buttonsPerRow = (panelWidth - BlockSelectionButton.SIZE) / BlockSelectionButton.SIZE;
        buttonsPerColumn = (panelHeight - BlockSelectionButton.SIZE) / BlockSelectionButton.SIZE;
        buttonsInPanel = buttonsPerRow * buttonsPerColumn;

        this.searchBar = new TextFieldWidget(font, panelOffsetX + 1, panelOffsetY - 13, panelWidth - 1, 12, new TranslationTextComponent("keystone.search"));
        this.searchBar.setMaxStringLength(256);
        this.searchBar.setEnableBackgroundDrawing(false);
        this.searchBar.setText("");
        this.searchBar.setResponder((str) -> rebuildButtons());
        this.children.add(this.searchBar);
        this.setFocusedDefault(this.searchBar);

        rebuildButtons();
    }
    @Override
    public void tick()
    {
        this.searchBar.tick();
        if (!clearedSearch && !searchBar.getText().isEmpty())
        {
            searchBar.setText("");
            clearedSearch = true;
        }
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

    public void rebuildButtons()
    {
        this.buttons.removeIf(widget -> widget instanceof BlockSelectionButton);
        this.children.removeIf(widget -> widget instanceof BlockSelectionButton);

        int x = panelOffsetX + BlockSelectionButton.SIZE / 2;
        int y = panelOffsetY + BlockSelectionButton.SIZE / 2;

        blockCount = 0;
        int skipCount = (int)scrollOffset * buttonsPerRow;
        for (Block block : blockRegistry)
        {
            if (block.getRegistryName().getNamespace().equals(KeystoneMod.MODID)) continue;

            // Check if block matches search
            String searchText = this.searchBar.getText().toLowerCase().trim();
            String blockName = block.getTranslatedName().getString().toLowerCase().trim();
            if (!searchText.isEmpty() && !blockName.contains(searchText)) continue;

            // Create button instance
            BlockSelectionButton button = BlockSelectionButton.create(this, block, x, y);
            if (button == null) continue;
            else blockCount++;

            // Add button if inside panel
            if (skipCount > 0)
            {
                skipCount--;
                continue;
            }
            if (y < panelOffsetY + panelHeight - BlockSelectionButton.SIZE / 2) addButton(button);

            // Update rendering coordinates
            x += BlockSelectionButton.SIZE;
            if (x >= panelOffsetX + panelWidth - BlockSelectionButton.SIZE / 2)
            {
                x = panelOffsetX + BlockSelectionButton.SIZE / 2;
                y += BlockSelectionButton.SIZE;
            }
        }
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        if (!clearedSearch) return;

        // Draw button panel
        fill(stack, panelOffsetX, panelOffsetY, panelOffsetX + panelWidth, panelOffsetY + panelHeight, 0x80000000);
        for (Widget button : this.buttons) button.render(stack, mouseX, mouseY, partialTicks);

        // If more buttons than can fit, draw scrollbar
        if (blockCount > buttonsInPanel)
        {
            searchBar.setWidth(panelWidth + 3);
            fill(stack, panelOffsetX + panelWidth, panelOffsetY, panelOffsetX + panelWidth + 4, panelOffsetY + panelHeight, 0x80000000);

            double rows = Math.ceil(blockCount / (double)buttonsPerRow);
            double normalizedStart = scrollOffset / rows;
            double normalizedEnd = normalizedStart + buttonsPerColumn / rows;
            int handleStart = (int)Math.floor(panelOffsetY + normalizedStart * panelHeight);
            int handleEnd = (int)Math.ceil(panelOffsetY + normalizedEnd * panelHeight);
            fill(stack, panelOffsetX + panelWidth + 2, handleStart, panelOffsetX + panelWidth + 4, handleEnd, 0xFF808080);
        }
        else searchBar.setWidth(panelWidth - 1);

        // Draw search box
        fill(stack, panelOffsetX, panelOffsetY - 14, panelOffsetX + searchBar.getWidth() + 1, panelOffsetY - 2, 0x80000000);
        searchBar.render(stack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        int rows = (int)Math.ceil(blockCount / (double)buttonsPerRow);

        scrollOffset -= delta;
        if (scrollOffset < 0) scrollOffset = 0;
        else if (scrollOffset + buttonsPerColumn > rows) scrollOffset = rows - buttonsPerColumn;

        rebuildButtons();
        return true;
    }
}
