package keystone.core.gui.widgets;

import keystone.api.utils.StringUtils;
import keystone.core.KeystoneMod;
import keystone.core.gui.screens.block_selection.AbstractBlockButton;
import keystone.core.gui.screens.block_selection.BlockGridButton;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BlockGridWidget extends ClickableWidget
{
    public static final AbstractBlockButton.IBlockTooltipBuilder NAME_TOOLTIP = (block, count, tooltip) -> tooltip.add(block.getBlock().getName());
    public static final AbstractBlockButton.IBlockTooltipBuilder NAME_AND_PROPERTIES_TOOLTIP = (block, count, tooltip) ->
    {
        tooltip.add(block.getBlock().getName());
        block.getProperties().forEach(property ->
        {
            if (property instanceof BooleanProperty)
            {
                BooleanProperty booleanProperty = (BooleanProperty)property;
                if (block.get(booleanProperty)) tooltip.addAll(Text.literal(StringUtils.snakeCaseToTitleCase(property.getName())).getWithStyle(Style.EMPTY.withColor(Formatting.GRAY)));
            }
            else tooltip.addAll(Text.literal(StringUtils.snakeCaseToTitleCase(property.getName()) + ": " + StringUtils.snakeCaseToTitleCase(block.get(property).toString())).getWithStyle(Style.EMPTY.withColor(Formatting.GRAY)));
        });
    };

    private final boolean allowMultiples;
    private final Consumer<BlockState> callback;
    private final Consumer<ClickableWidget[]> disableWidgets;
    private final Runnable restoreWidgets;
    private final AbstractBlockButton.IBlockTooltipBuilder tooltipBuilder;
    private final Screen screen;

    private int blockCount;

    private int buttonsPerRow;
    private int buttonsPerColumn;
    private int buttonsInPanel;
    private double scrollOffset;

    private List<BlockState> blocks;
    private Map<BlockState, Integer> blockCounts;
    private List<BlockGridButton> buttons;
    private Predicate<BlockState> filter;

    //region Creation
    private BlockGridWidget(Screen screen, int x, int y, int width, int height, boolean allowMultiples, Text title, Consumer<BlockState> callback, Consumer<ClickableWidget[]> disableWidgets, Runnable restoreWidgets, AbstractBlockButton.IBlockTooltipBuilder tooltipBuilder)
    {
        super(x, y, width, height, title);
        this.allowMultiples = allowMultiples;
        this.callback = callback;
        this.disableWidgets = disableWidgets;
        this.restoreWidgets = restoreWidgets;
        this.tooltipBuilder = tooltipBuilder;
        this.screen = screen;

        buttonsPerRow = (width - BlockGridButton.SIZE) / BlockGridButton.SIZE;
        buttonsPerColumn = (height - BlockGridButton.SIZE) / BlockGridButton.SIZE;
        buttonsInPanel = buttonsPerRow * buttonsPerColumn;
        scrollOffset = 0;

        blocks = new ArrayList<>();
        blockCounts = new HashMap<>();
        buttons = new ArrayList<>();
    }
    public static BlockGridWidget createWithMargins(Screen screen, int idealLeftMargin, int idealRightMargin, int idealTopMargin, int idealBottomMargin, boolean allowMultiples, Text title, Consumer<BlockState> callback, Consumer<ClickableWidget[]> disableWidgets, Runnable restoreWidgets, AbstractBlockButton.IBlockTooltipBuilder tooltipBuilder)
    {
        Window window = MinecraftClient.getInstance().getWindow();

        int panelWidth = window.getScaledWidth() - idealLeftMargin - idealRightMargin;
        panelWidth -= panelWidth % BlockGridButton.SIZE;
        int panelHeight = window.getScaledHeight() - idealTopMargin - idealBottomMargin;
        panelHeight -= panelHeight % BlockGridButton.SIZE;

        int panelOffsetX = (int)Math.floor((window.getScaledWidth() - panelWidth) * (idealLeftMargin / (float)(idealLeftMargin + idealRightMargin)));
        int panelOffsetY = (int)Math.floor((window.getScaledHeight() - panelHeight) * (idealTopMargin / (float)(idealTopMargin + idealBottomMargin)));

        return new BlockGridWidget(screen, panelOffsetX, panelOffsetY, panelWidth, panelHeight, allowMultiples, title, callback, disableWidgets, restoreWidgets, tooltipBuilder);
    }
    public static BlockGridWidget create(Screen screen, int x, int y, int width, int height, boolean allowMultiples, Text title, Consumer<BlockState> callback, Consumer<ClickableWidget[]> disableWidgets, Runnable restoreWidgets, AbstractBlockButton.IBlockTooltipBuilder tooltipBuilder)
    {
        width -= width % BlockGridButton.SIZE;
        height -= height % BlockGridButton.SIZE;
        return new BlockGridWidget(screen, x, y, width, height, allowMultiples, title, callback, disableWidgets, restoreWidgets, tooltipBuilder);
    }
    //endregion

    public void onBlockClicked(BlockState block)
    {
        if (callback != null) callback.accept(block);
    }

    //region Widget Overrides
    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
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

        // Draw button panel
        fill(stack, x, y, x + width, y + height, 0x80000000);
        for (ClickableWidget button : this.buttons) button.render(stack, mouseX, mouseY, partialTicks);
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        for (BlockGridButton blockButton : buttons) if (blockButton.mouseClicked(mouseX, mouseY, button)) return true;
        return false;
    }
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        for (BlockGridButton blockButton : buttons) if (blockButton.mouseReleased(mouseX, mouseY, button)) return true;
        return false;
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        if (isHovered())
        {
            int rows = (int)Math.ceil(blockCount / (double)buttonsPerRow);

            scrollOffset -= delta;
            if (scrollOffset < 0) scrollOffset = 0;
            else if (scrollOffset + buttonsPerColumn > rows) scrollOffset = rows - buttonsPerColumn;

            rebuildButtons();
            return true;
        }
        else return false;
    }
    @Override
    public void playDownSound(SoundManager manager) {}
    @Override
    public void appendNarrations(NarrationMessageBuilder builder)
    {

    }
    //endregion
    //region Editing
    public void addBlock(BlockState block) { addBlock(block, true); }
    public void addBlock(BlockState block, boolean rebuildButtons)
    {
        if (!blockCounts.containsKey(block))
        {
            blockCounts.put(block, 1);
            blocks.add(block);
        }
        else if (allowMultiples) blockCounts.put(block, blockCounts.get(block) + 1);

        if (rebuildButtons) rebuildButtons();
    }
    public void removeBlock(BlockState block) { removeBlock(block, true); }
    public void removeBlock(BlockState block, boolean rebuildButtons)
    {
        Integer count = blockCounts.get(block);
        if (count != null)
        {
            count--;
            if (count > 0) blockCounts.put(block, count);
            else
            {
                blockCounts.remove(block);
                blocks.remove(block);
            }

            if (rebuildButtons) rebuildButtons();
        }
    }
    public void filter(String searchString)
    {
        if (searchString == null || searchString.isEmpty()) filter((Predicate<BlockState>)null);
        else
        {
            String filterString = searchString.toLowerCase().trim();
            filter(block ->
            {
                String blockName = block.getBlock().getName().getString().toLowerCase().trim();
                return blockName.contains(filterString);
            });
        }
    }
    public void filter(Predicate<BlockState> filter)
    {
        this.filter = filter;
        rebuildButtons();
    }
    //endregion
    //region Helpers
    public void disableWidgets(ClickableWidget... widgets)
    {
        disableWidgets.accept(widgets);
    }
    public void restoreWidgets()
    {
        restoreWidgets.run();
    }
    public void rebuildButtons()
    {
        this.buttons.clear();

        int x = this.x + BlockGridButton.SIZE / 2;
        int y = this.y + BlockGridButton.SIZE / 2;

        blockCount = 0;
        int skipCount = (int)scrollOffset * buttonsPerRow;
        for (BlockState block : this.blocks)
        {
            Integer count = blockCounts.get(block);

            // Check if block isn't from Keystone and matches filter
            if (Registry.BLOCK.getId(block.getBlock()).getNamespace().equals(KeystoneMod.MODID)) continue;
            if (filter != null && !filter.test(block)) continue;

            // Create button instance
            BlockGridButton button = BlockGridButton.create(screen, this, block, count, x, y, tooltipBuilder);
            if (button == null) continue;
            else blockCount++;

            // Add button if inside panel
            if (skipCount > 0)
            {
                skipCount--;
                continue;
            }
            if (y < this.y + height - BlockGridButton.SIZE / 2) this.buttons.add(button);

            // Update rendering coordinates
            x += BlockGridButton.SIZE;
            if (x >= this.x + width - BlockGridButton.SIZE / 2)
            {
                x = this.x + BlockGridButton.SIZE / 2;
                y += BlockGridButton.SIZE;
            }
        }
    }
    //endregion
    //region Getters
    public int getBlockCount()
    {
        return blockCount;
    }
    public int getButtonsPerRow()
    {
        return buttonsPerRow;
    }
    public int getButtonsPerColumn()
    {
        return buttonsPerColumn;
    }
    public int getButtonsInPanel()
    {
        return buttonsInPanel;
    }
    //endregion
}
