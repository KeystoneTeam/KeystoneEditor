package keystone.core.gui.widgets;

import keystone.core.gui.WidgetDisabler;
import keystone.core.gui.overlays.block_selection.AbstractBlockButton;
import keystone.core.gui.overlays.block_selection.BlockGridButton;
import keystone.core.gui.viewports.Viewport;
import keystone.core.modules.filter.blocks.BlockListProvider;
import keystone.core.modules.filter.blocks.BlockTypeProvider;
import keystone.core.modules.filter.blocks.IBlockProvider;
import keystone.core.registries.BlockTypeRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.RegistryEntryList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class BlockGridWidget extends ClickableWidget
{
    public record Entry(IBlockProvider provider, AbstractBlockButton.IBlockTooltipBuilder tooltipBuilder)
    {
        public Entry(BlockState state, AbstractBlockButton.IBlockTooltipBuilder tooltipBuilder)
        {
            this(new BlockTypeProvider(BlockTypeRegistry.fromMinecraftBlock(state)), tooltipBuilder);
        }
        public Entry(RegistryEntryList<Block> tag, Map<String, String> vagueProperties, AbstractBlockButton.IBlockTooltipBuilder tooltipBuilder)
        {
            this(new BlockListProvider(tag, vagueProperties), tooltipBuilder);
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return provider.equals(entry.provider) && tooltipBuilder.equals(entry.tooltipBuilder);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(provider, tooltipBuilder);
        }
    }

    public static final AbstractBlockButton.IBlockTooltipBuilder NAME_TOOLTIP = (blockProvider, count, tooltip) -> tooltip.add(blockProvider.getName());
    public static final AbstractBlockButton.IBlockTooltipBuilder NAME_AND_PROPERTIES_TOOLTIP = (blockProvider, count, tooltip) ->
    {
        NAME_TOOLTIP.buildTooltip(blockProvider, count, tooltip);
        tooltip.addAll(blockProvider.getProperties());
    };
    public static final AbstractBlockButton.IBlockTooltipBuilder ANY_VARIANT_TOOLTIP = (blockProvider, count, tooltip) ->
    {
        NAME_TOOLTIP.buildTooltip(blockProvider, count, tooltip);
        tooltip.add(Text.translatable("keystone.block_selection_panel.anyVariant").styled(style -> style.withColor(Formatting.GRAY)));
    };

    private final boolean allowMultiples;
    private final BiConsumer<Entry, Integer> callback;
    private final WidgetDisabler widgetDisabler;
    private final BlockGridButton.ClickConsumer leftClickConsumer;
    private final BlockGridButton.ClickConsumer rightClickConsumer;
    private final BlockGridButton.ScrollConsumer scrollConsumer;
    private final Screen screen;

    private int blockCount;

    private final int buttonsPerRow;
    private final int buttonsPerColumn;
    private final int buttonsInPanel;
    private double scrollOffset;

    private final int buttonsX;
    private final int buttonsY;
    private int buttonsWidth;
    private int buttonsHeight;

    private final List<Entry> entries;
    private final Map<Entry, Integer> blockCounts;
    private final List<BlockGridButton> buttons;
    
    private Predicate<IBlockProvider> filter;
    private BiConsumer<Entry, Integer> countChangedCallback;

    //region Creation
    private BlockGridWidget(Screen screen, int x, int y, int width, int height, boolean allowMultiples, Text title, BiConsumer<Entry, Integer> callback, BlockGridButton.ClickConsumer leftClickConsumer, BlockGridButton.ClickConsumer rightClickConsumer, BlockGridButton.ScrollConsumer scrollConsumer)
    {
        super(x, y, width, height, title);
        this.allowMultiples = allowMultiples;
        this.callback = callback;
        this.widgetDisabler = new WidgetDisabler();
        this.leftClickConsumer = leftClickConsumer;
        this.rightClickConsumer = rightClickConsumer;
        this.scrollConsumer = scrollConsumer;
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
    public static BlockGridWidget createWithViewport(Screen screen, Viewport idealViewport, boolean allowMultiples, Text title, BiConsumer<Entry, Integer> callback, BlockGridButton.ClickConsumer leftClickConsumer, BlockGridButton.ClickConsumer rightClickConsumer, BlockGridButton.ScrollConsumer scrollConsumer)
    {
        return new BlockGridWidget(screen, idealViewport.getMinX(), idealViewport.getMinY(), idealViewport.getWidth(), idealViewport.getHeight(), allowMultiples, title, callback, leftClickConsumer, rightClickConsumer, scrollConsumer);
    }
    public static BlockGridWidget create(Screen screen, int x, int y, int width, int height, boolean allowMultiples, Text title, BiConsumer<Entry, Integer> callback, BlockGridButton.ClickConsumer leftClickConsumer, BlockGridButton.ClickConsumer rightClickConsumer, BlockGridButton.ScrollConsumer scrollConsumer)
    {
        width -= width % BlockGridButton.SIZE;
        height -= height % BlockGridButton.SIZE;
        return new BlockGridWidget(screen, x, y, width, height, allowMultiples, title, callback, leftClickConsumer, rightClickConsumer, scrollConsumer);
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
            // Try to scroll buttons
            for (BlockGridButton blockButton : buttons) if (blockButton.mouseScrolled(mouseX, mouseY, delta)) return true;
    
            // Scroll panel
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
    public void addBlockProvider(IBlockProvider blockProvider, AbstractBlockButton.IBlockTooltipBuilder tooltipBuilder) { addBlockProvider(blockProvider, tooltipBuilder, 1, true); }
    public void addBlockProvider(IBlockProvider blockProvider, AbstractBlockButton.IBlockTooltipBuilder tooltipBuilder, int amount) { addBlockProvider(blockProvider, tooltipBuilder, amount, true); }
    public void addBlockProvider(IBlockProvider blockProvider, AbstractBlockButton.IBlockTooltipBuilder tooltipBuilder, boolean rebuildButtons) { addBlockProvider(blockProvider, tooltipBuilder, 1, rebuildButtons); }
    public void addBlockProvider(IBlockProvider blockProvider, AbstractBlockButton.IBlockTooltipBuilder tooltipBuilder, int amount, boolean rebuildButtons)
    {
        // Increase Block Count
        Entry entry = new Entry(blockProvider, tooltipBuilder);
        if (!blockCounts.containsKey(entry))
        {
            blockCounts.put(entry, allowMultiples ? amount : 1);
            entries.add(entry);
        }
        else if (allowMultiples) blockCounts.put(entry, blockCounts.get(entry) + amount);
        
        // Trigger Callback and Rebuild
        if (countChangedCallback != null) countChangedCallback.accept(entry, blockCounts.get(entry));
        if (rebuildButtons) rebuildButtons();
    }
    public void removeBlockProvider(IBlockProvider blockProvider, AbstractBlockButton.IBlockTooltipBuilder tooltipBuilder) { removeBlockProvider(blockProvider, tooltipBuilder, true); }
    public void removeBlockProvider(IBlockProvider blockProvider, AbstractBlockButton.IBlockTooltipBuilder tooltipBuilder, int amount) { removeBlockProvider(blockProvider, tooltipBuilder, amount, true); }
    public void removeBlockProvider(IBlockProvider blockProvider, AbstractBlockButton.IBlockTooltipBuilder tooltipBuilder, boolean rebuildButtons) { removeBlockProvider(blockProvider, tooltipBuilder, 1, true); }
    public void removeBlockProvider(IBlockProvider blockProvider, AbstractBlockButton.IBlockTooltipBuilder tooltipBuilder, int amount, boolean rebuildButtons)
    {
        // Decrease Block Count
        Entry entry = new Entry(blockProvider, tooltipBuilder);
        Integer count = blockCounts.get(entry);
        if (count != null)
        {
            count -= amount;
            if (count > 0) blockCounts.put(entry, count);
            else
            {
                blockCounts.remove(entry);
                entries.remove(entry);
            }
    
            // Trigger Callback and Rebuild
            if (countChangedCallback != null) countChangedCallback.accept(entry, Math.max(0, count));
            if (rebuildButtons) rebuildButtons();
        }
    }
    public void filter(String searchString)
    {
        if (searchString == null || searchString.isEmpty()) filter((Predicate<IBlockProvider>)null);
        else
        {
            String filterString = searchString.toLowerCase().trim();
    
            // Tag Search
            if (filterString.startsWith("#"))
            {
                filter(blockProvider ->
                {
                    if (blockProvider instanceof BlockListProvider)
                    {
                        String providerName = blockProvider.getName().getString().toLowerCase().trim();
                        return providerName.contains(filterString.substring(1));
                    }
                    else return false;
                });
            }
            
            // Block Search
            else
            {
                filter(blockProvider ->
                {
                    if (!(blockProvider instanceof BlockListProvider))
                    {
                        String providerName = blockProvider.getName().getString().toLowerCase().trim();
                        return providerName.contains(filterString);
                    }
                    else return false;
                });
            }
        }
    }
    public void filter(Predicate<IBlockProvider> filter)
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

            // TODO: Ignore blocks added by Keystone
            // Check if the block provider matches filter
            if (filter != null && !filter.test(entry.provider)) continue;

            // Create button instance
            BlockGridButton button = BlockGridButton.create(screen, this, entry.provider, count, x, y, leftClickConsumer, rightClickConsumer, scrollConsumer, entry.tooltipBuilder);
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
    //region Setters
    public void setCountChangedCallback(BiConsumer<Entry, Integer> callback)
    {
        this.countChangedCallback = callback;
    }
    //endregion
}
