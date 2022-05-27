package keystone.core.gui.screens.filters;

import keystone.api.Keystone;
import keystone.api.KeystoneDirectories;
import keystone.api.filters.KeystoneFilter;
import keystone.core.events.keystone.KeystoneHotbarEvents;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.screens.KeystoneOverlay;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.widgets.TextDisplayWidget;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import keystone.core.gui.widgets.buttons.TextClickButton;
import keystone.core.gui.widgets.inputs.Dropdown;
import keystone.core.gui.widgets.inputs.fields.FieldWidgetList;
import keystone.core.modules.filter.FilterDirectoryManager;
import keystone.core.modules.filter.FilterModule;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterSelectionScreen extends KeystoneOverlay
{
    private static final int PADDING = 5;
    private static FilterSelectionScreen open;
    private static File selectedFilterFile;
    private static boolean dirtied;

    private FilterModule filterModule;
    private FilterDirectoryManager filterManager;
    private int panelMinY;
    private int panelMaxX;
    private int panelMaxY;

    private ButtonWidget selectFilterButton;
    private Dropdown<File> dropdown;
    private FieldWidgetList filterVariablesList;

    protected FilterSelectionScreen()
    {
        super(Text.literal("keystone.screen.filterPanel"));
        this.filterModule = Keystone.getModule(FilterModule.class);
        this.filterManager = filterModule.getFilterDirectoryManager();
    }
    public static void open()
    {
        if (open == null)
        {
            open = new FilterSelectionScreen();
            KeystoneOverlayHandler.addOverlay(open);
        }
    }
    public static void registerEvents()
    {
        KeystoneHotbarEvents.CHANGED.register(FilterSelectionScreen::onHotbarChanged);
    }
    public static void dirty()
    {
        dirtied = true;
    }

    //region Static Event Handlers
    public static void onHotbarChanged(KeystoneHotbarSlot previous, KeystoneHotbarSlot slot)
    {
        if (slot == KeystoneHotbarSlot.FILTER) open();
        else if (open != null) open.close();
    }
    //endregion

    //region Screen Overrides
    @Override
    public void removed()
    {
        open = null;
    }
    @Override
    public void init()
    {
        if (selectedFilterFile == null) selectedFilterFile = filterManager.getInstalledFilters()[0];

        // Build Filter Variable Widgets
        panelMaxX = Math.min(KeystoneHotbar.getX() - 5, 280);
        int maxPanelHeight = client.getWindow().getScaledHeight() - 50 - 2 * PADDING;
        filterVariablesList = new FieldWidgetList(Text.translatable("keystone.filter_panel.filterVariables"), this::getFilter, 0, 0, panelMaxX, maxPanelHeight, PADDING, this::disableWidgets, this::restoreWidgets);

        // Error Display
        KeystoneFilter selectedFilter = filterManager.getFilter(selectedFilterFile);
        if (!selectedFilter.isCompiledSuccessfully())
        {
            TextDisplayWidget compileErrorDisplay = new TextDisplayWidget(0, filterVariablesList.getNextWidgetY(), filterVariablesList.getWidth() - 2 * PADDING, textRenderer)
                    .setPadding(0);

            String error = selectedFilter.getCompilerException().getLocalizedMessage();
            String splitRegex = "Line [0-9]+, Column [0-9]+:";
            String[] tokens = error.split(splitRegex, 2);
            if (tokens.length == 1) compileErrorDisplay.addText(Formatting.RED, error);
            else
            {
                Pattern pattern = Pattern.compile(splitRegex);
                Matcher matcher = pattern.matcher(error);
                if (matcher.find())
                {
                    compileErrorDisplay.addText(Formatting.RED, (tokens[0] + matcher.group()).trim());
                    compileErrorDisplay.addText(Formatting.RED, "  " + tokens[1].trim());
                }
                else compileErrorDisplay.addText(Formatting.RED, error);
            }
            filterVariablesList.add(compileErrorDisplay);
        }

        // Calculate Panel Size
        filterVariablesList.bake();
        int centerHeight = height / 2;
        int halfPanelHeight = 25 + PADDING + filterVariablesList.getHeight() / 2;
        panelMinY = centerHeight - halfPanelHeight;
        panelMaxY = centerHeight + halfPanelHeight;

        // Select Filter Button
        TextClickButton filterLabel = new TextClickButton(5, panelMinY + 11, Text.translatable("keystone.filter_panel.select"), 0x8080FF, button -> Util.getOperatingSystem().open(KeystoneDirectories.getFilterDirectory()));
        addDrawableChild(filterLabel);
        int selectButtonX = filterLabel.x + filterLabel.getWidth();
        this.selectFilterButton = new ButtonNoHotkey(selectButtonX, panelMinY + 5, panelMaxX - selectButtonX - 5, 20, Text.literal("!ERROR!"), (button) ->
        {
            disableWidgets(this.dropdown);
            this.dropdown.visible = true;
        });

        // Filter selection dropdown
        this.dropdown = new Dropdown<>(selectFilterButton.x, selectFilterButton.y, selectFilterButton.getWidth(), Text.literal("keystone.tool.filter.dropdown"),
                filterFile ->
                {
                    KeystoneFilter filter = filterManager.getFilter(filterFile);
                    if (filter.isCompiledSuccessfully()) return Text.literal(filter.getName());
                    else return Text.literal(filter.getName()).styled(style -> style.withColor(Formatting.RED));
                },
                (filterFile, title) ->
                {
                    restoreWidgets();
                    selectedFilterFile = filterFile;

                    this.selectFilterButton.setMessage(title);
                    this.init(client, width, height);
                }, filterManager.getInstalledFilters());

        if (selectedFilterFile != null)
        {
            for (int i = 0; i < dropdown.size(); i++)
            {
                File filterFile = dropdown.getEntry(i);
                if (filterFile.equals(selectedFilterFile))
                {
                    dropdown.setSelectedEntry(filterFile, false);
                    break;
                }
            }
        }

        // Run Filter Button
        int buttonWidth = textRenderer.getWidth(Text.translatable("keystone.filter_panel.runFilter").getString()) + 10;
        int panelCenter = panelMaxX / 2;
        ButtonNoHotkey runFilterButton = new ButtonNoHotkey(panelCenter - buttonWidth / 2, panelMaxY - 25, buttonWidth, 20, Text.literal("keystone.filter_panel.runFilter"), button -> runFilter());

        // Add buttons
        this.selectFilterButton.setMessage(this.dropdown.getSelectedEntryTitle());
        addDrawableChild(selectFilterButton);
        addDrawableChild(runFilterButton);
        addDrawableChild(dropdown);

        // Create Filter Variables
        filterVariablesList.offset(0, panelMinY + ((panelMaxY - panelMinY) / 2) - (filterVariablesList.getHeight() / 2));
        addDrawableChild(filterVariablesList);
        filterManager.getFilter(selectedFilterFile).undirtyEditor();
    }
    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        if (selectedFilterFile != null)
        {
            if (filterManager.getFilter(selectedFilterFile) == null)
            {
                selectedFilterFile = filterManager.getInstalledFilters()[0];
                init(client, width, height);
            }
        }
        if (dirtied)
        {
            init(client, width, height);
            dirtied = false;
        }

        fill(stack, 0, panelMinY, panelMaxX, panelMaxY, 0x80000000);

        super.render(stack, mouseX, mouseY, partialTicks);
        this.dropdown.render(stack, mouseX, mouseY, partialTicks);
    }
    //endregion
    //region Getters
    public KeystoneFilter getFilter() { return filterManager.getFilter(selectedFilterFile); }
    //endregion
    //region Helpers
    private void runFilter()
    {
        for (Element element : children()) if (element instanceof TextFieldWidget textField) textField.changeFocus(false);
        if (selectedFilterFile != null) filterModule.runFilter(filterManager.getFilter(selectedFilterFile));
    }
    //endregion
}
