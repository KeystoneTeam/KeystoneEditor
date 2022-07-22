package keystone.core.gui.widgets;

import keystone.api.utils.StringUtils;
import keystone.core.KeystoneMod;
import keystone.core.gui.WidgetDisabler;
import keystone.core.gui.overlays.block_selection.AbstractBlockButton;
import keystone.core.gui.overlays.block_selection.BlockGridButton;
import keystone.core.gui.viewports.Viewport;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class BlockGridWidget extends ClickableWidget
{
    public record Entry(BlockState state, AbstractBlockButton.IBlockTooltipBuilder tooltipBuilder)
    {
        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return state.equals(entry.state) && tooltipBuilder.equals(entry.tooltipBuilder);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(state, tooltipBuilder);
        }
    }

    public static final AbstractBlockButton.IBlockTooltipBuilder NAME_TOOLTIP = (block, count, tooltip) -> tooltip.add(block.getBlock().getName());
    public static final AbstractBlockButton.IBlockTooltipBuilder NAME_AND_PROPERTIES_TOOLTIP = (block, count, tooltip) ->
    {
        NAME_TOOLTIP.buildTooltip(block, count, tooltip);
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
    public static final AbstractBlockButton.IBlockTooltipBuilder ANY_VARIANT_TOOLTIP = (block, count, tooltip) ->
    {
        NAME_TOOLTIP.buildTooltip(block, count, tooltip);
        tooltip.add(Text.translatable("keystone.block_selection_panel.anyVariant").styled(style -> style.withColor(Formatting.GRAY)));
    };

    private final boolean allowMultiples;
    private final BiConsumer<Entry, Integer> callback;
    private final WidgetDisabler widgetDisabler;
    private final BlockGridButton.ClickConsumer leftClickConsumer;
    private final BlockGridButton.ClickConsumer rightClickConsumer;
    private final Screen screen;

    private int blockCount;

    private int buttonsPerRow;
    private int buttonsPerColumn;
    private int buttonsInPanel;
    private double scrollOffset;

    private int buttonsX;
    private int buttonsY;
    private int buttonsWidth;
    private int buttonsHeight;

    private List<Entry> entries;
    private Map<Entry, Integer> blockCounts;
    private List<BlockGridButton> buttons;
    private Predicate<BlockState> filter;

    //region Creation
    private BlockGridWidget(Screen screen, int x, int y, int width, int height, boolean allowMultiples, Text title, BiConsumer<Entry, Integer> callback, BlockGridButton.ClickConsumer leftClickConsumer, BlockGridButton.ClickConsumer rightClickConsumer)
    {
        super(x, y, width, height, title);
        this.allowMultiples = allowMultiples;
        this.callback = callback;
        this.widgetDisabler = new WidgetDisabler();
        this.leftClickConsumer = leftClickConsumer;
        this.rightClickConsumer = rightClickConsumer;
        this.screen = screen;

        buttonsPerRow = (width - BlockGridButton.SIZE) / BlockGridButton.SIZE;
        buttonsPerColumn = (height - BlockGridButton.SIZE) / BlockGridButton.SIZE;
        buttonsInPanel = buttonsPerRow * buttonsPerColumn;
        scrollOffset = 0;

        buttonsWidth = width;
        buttonsWidth -= width % BlockGridButton.SIZE;
        buttonsHeight = height;
        buttonsHeight -= buttonsHeight % BlockGridButton.SIZE;

        buttonsX = x + (width - buttonsWidth) / 2;
        buttonsY = y + (height - buttonsHeight) / 2;

        entries = new ArrayList<>();
        blockCounts = new HashMap<>();
        buttons = new ArrayList<>();
    }
    public static BlockGridWidget createWithViewport(Screen screen, Viewport idealViewport, boolean allowMultiples, Text title, BiConsumer<Entry, Integer> callback, BlockGridButton.ClickConsumer leftClickConsumer, BlockGridButton.ClickConsumer rightClickConsumer)
    {
        return new BlockGridWidget(screen, idealViewport.getMinX(), idealViewport.getMinY(), idealViewport.getWidth(), idealViewport.getHeight(), allowMultiples, title, callback, leftClickConsumer, rightClickConsumer);
    }
    public static BlockGridWidget create(Screen screen, int x, int y, int width, int height, boolean allowMultiples, Text title, BiConsumer<Entry, Integer> callback, BlockGridButton.ClickConsumer leftClickConsumer, BlockGridButton.ClickConsumer rightClickConsumer)
    {
        width -= width % BlockGridButton.SIZE;
        height -= height % BlockGridButton.SIZE;
        return new BlockGridWidget(screen, x, y, width, height, allowMultiples, title, callback, leftClickConsumer, rightClickConsumer);
    }
    //endregion

    public void onEntryClicked(Entry entry, int mouseButton)
    {
        if (callback != null) callback.accept(entry, mouseButton);
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
    public void addBlock(BlockState block, AbstractBlockButton.IBlockTooltipBuilder tooltipBuilder) { addBlock(block, tooltipBuilder, true); }
    public void addBlock(BlockState block, AbstractBlockButton.IBlockTooltipBuilder tooltipBuilder, boolean rebuildButtons)
    {
        Entry entry = new Entry(block, tooltipBuilder);
        if (!blockCounts.containsKey(entry))
        {
            blockCounts.put(entry, 1);
            entries.add(entry);
        }
        else if (allowMultiples) blockCounts.put(entry, blockCounts.get(entry) + 1);

        if (rebuildButtons) rebuildButtons();
    }
    public void removeBlock(BlockState block, AbstractBlockButton.IBlockTooltipBuilder tooltipBuilder) { removeBlock(block, tooltipBuilder, true); }
    public void removeBlock(BlockState block, AbstractBlockButton.IBlockTooltipBuilder tooltipBuilder, boolean rebuildButtons)
    {
        Entry entry = new Entry(block, tooltipBuilder);
        Integer count = blockCounts.get(entry);
        if (count != null)
        {
            count--;
            if (count > 0) blockCounts.put(entry, count);
            else
            {
                blockCounts.remove(entry);
                entries.remove(entry);
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
    public void disableWidgets()
    {
        widgetDisabler.disableAll();
    }
    public void restoreWidgets()
    {
        widgetDisabler.restoreAll();
    }
    public void rebuildButtons()
    {
        this.buttons.clear();

        int x = buttonsX + BlockGridButton.SIZE / 2;
        int y = buttonsY + BlockGridButton.SIZE / 2;

        blockCount = 0;
        int skipCount = (int)scrollOffset * buttonsPerRow;
        for (Entry entry : this.entries)
        {
            Integer count = blockCounts.get(entry);

            // Check if block isn't from Keystone and matches filter
            if (Registry.BLOCK.getId(entry.state.getBlock()).getNamespace().equals(KeystoneMod.MODID)) continue;
            if (filter != null && !filter.test(entry.state)) continue;

            // Create button instance
            BlockGridButton button = BlockGridButton.create(screen, this, entry.state, count, x, y, leftClickConsumer, rightClickConsumer, entry.tooltipBuilder);
            if (button == null) continue;
            else blockCount++;

            // Add button if inside panel
            if (skipCount > 0)
            {
                skipCount--;
                continue;
            }
            if (y < buttonsY + buttonsHeight - BlockGridButton.SIZE / 2) this.buttons.add(button);

            // Update rendering coordinates
            x += BlockGridButton.SIZE;
            if (x >= buttonsX + buttonsWidth - BlockGridButton.SIZE / 2)
            {
                x = buttonsX + BlockGridButton.SIZE / 2;
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
