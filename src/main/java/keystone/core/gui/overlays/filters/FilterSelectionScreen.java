package keystone.core.gui.overlays.filters;

import keystone.api.Keystone;
import keystone.api.KeystoneDirectories;
import keystone.api.filters.KeystoneFilter;
import keystone.core.events.keystone.KeystoneHotbarEvents;
import keystone.core.gui.IKeystoneTooltip;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.WidgetDisabler;
import keystone.core.gui.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.overlays.KeystonePanel;
import keystone.core.gui.viewports.ScreenViewports;
import keystone.core.gui.viewports.Viewport;
import keystone.core.gui.widgets.TextDisplayWidget;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import keystone.core.gui.widgets.buttons.TextClickButton;
import keystone.core.gui.widgets.groups.Margins;
import keystone.core.gui.widgets.inputs.Dropdown;
import keystone.core.gui.widgets.inputs.fields.FieldWidgetList;
import keystone.core.modules.filter.FilterDirectoryManager;
import keystone.core.modules.filter.FilterModule;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterSelectionScreen extends KeystonePanel
{
    private static final int PADDING = 5;
    private static FilterSelectionScreen open;
    private static File selectedFilterFile;
    private static boolean dirtied;

    private FilterModule filterModule;
    private FilterDirectoryManager filterManager;

    private ButtonWidget selectFilterButton;
    private Dropdown<File> dropdown;
    private FieldWidgetList filterVariablesList;
    private WidgetDisabler widgetDisabler;

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
    protected Viewport createViewport()
    {
        Viewport dock = ScreenViewports.getViewport(Viewport.MIDDLE, Viewport.LEFT);
        return dock.setHeight(40 + 4 * PADDING + filterVariablesList.getHeight());
    }
    @Override
    protected void init()
    {
        if (widgetDisabler != null) widgetDisabler.restoreAll();

        if (selectedFilterFile == null) selectedFilterFile = filterManager.getInstalledFilters()[0];
        Viewport dock = ScreenViewports.getViewport(Viewport.MIDDLE, Viewport.LEFT); //ScreenViewports.getViewport(Viewport.BOTTOM, Viewport.LEFT, Viewport.MIDDLE, Viewport.LEFT);

        // Build Filter Variable Widgets
        int maxPanelHeight = dock.getHeight() - 40 - 4 * PADDING;
        filterVariablesList = new FieldWidgetList(this, Text.translatable("keystone.filter_panel.filterVariables"), this::getFilter, 0, 0, dock.getWidth(), maxPanelHeight, PADDING);

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

        // Finalize Size
        filterVariablesList.bake();
        setupViewport();

        // Select Filter Button
        TextClickButton filterLabel = new TextClickButton(getViewport().getMinX() + 5, getViewport().getMinY() + 11, Text.translatable("keystone.filter_panel.select"), 0x8080FF, button -> Util.getOperatingSystem().open(KeystoneDirectories.getFilterDirectory().toFile()));
        addDrawableChild(filterLabel);
        int selectButtonX = filterLabel.x + filterLabel.getWidth();
        this.selectFilterButton = new ButtonNoHotkey(selectButtonX, getViewport().getMinY() + 5, getViewport().getMaxX() - selectButtonX - 5, 20, Text.literal("!ERROR!"), (button) ->
        {
            widgetDisabler.disableAll();
            this.dropdown.visible = true;
            this.dropdown.active = true;
        });

        // Filter selection dropdown
        List<Dropdown.Option<File>> dropdownOptions = new ArrayList<>();
        for (File filterFile : filterManager.getInstalledFilters())
        {
            KeystoneFilter filter = filterManager.getFilter(filterFile);
            if (filter == null) continue;
            
            MutableText label = Text.literal(filter.getName());
            if (!filter.isCompiledSuccessfully()) label = label.styled(style -> style.withColor(Formatting.RED));
            dropdownOptions.add(new Dropdown.Option<>(filterFile, label, false));
        }

        this.dropdown = new Dropdown<>(selectFilterButton.x, selectFilterButton.y, selectFilterButton.getWidth(), Text.translatable("keystone.tool.filter.dropdown"),
                option ->
                {
                    widgetDisabler.restoreAll();
                    selectedFilterFile = option.value();

                    this.selectFilterButton.setMessage(title);
                    this.init(client, width, height);
                }, dropdownOptions);
        this.widgetDisabler = new WidgetDisabler(this.dropdown);

        if (selectedFilterFile != null)
        {
            for (int i = 0; i < dropdown.layoutControlledWidgetCount(); i++)
            {
                File filterFile = dropdown.getValue(i);
                if (filterFile.equals(selectedFilterFile))
                {
                    dropdown.setSelectedOption(filterFile, false);
                    break;
                }
            }
        }

        // Run Filter Button
        int buttonWidth = textRenderer.getWidth(Text.translatable("keystone.filter_panel.runFilter").getString()) + 10;
        int panelCenter = getViewport().getMinX() + getViewport().getWidth() / 2;
        ButtonNoHotkey runFilterButton = new ButtonNoHotkey(panelCenter - buttonWidth / 2, getViewport().getMaxY() - 25, buttonWidth, 20, Text.translatable("keystone.filter_panel.runFilter"), button -> runFilter());
        runFilterButton.setTooltip(IKeystoneTooltip.createSimple(this, Text.translatable("keystone.filter_panel.runFilter.tooltip")));

        // Add buttons
        this.selectFilterButton.setMessage(this.dropdown.getSelectedOption().label());
        addDrawableChild(selectFilterButton);
        addDrawableChild(runFilterButton);
        addDrawableChild(dropdown);

        // Create Filter Variables
        filterVariablesList.move(getViewport().getMinX(), getViewport().getMinY() + (getViewport().getHeight() / 2) - (filterVariablesList.getHeight() / 2));
        filterVariablesList.setMargins(new Margins(5, 0));
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

        fillPanel(stack, 0x80000000);
        super.render(stack, mouseX, mouseY, partialTicks);
        this.dropdown.render(stack, mouseX, mouseY, partialTicks);
    }
    //endregion
    //region Getters
    public static FilterSelectionScreen getOpenInstance() { return open; }
    public KeystoneFilter getFilter() { return filterManager.getFilter(selectedFilterFile); }
    //endregion
    //region Helpers
    private void runFilter()
    {
        for (Element element : children()) if (element instanceof TextFieldWidget textField) textField.changeFocus(false);
        if (selectedFilterFile != null) filterModule.runFilters(filterManager.getFilter(selectedFilterFile));
    }
    //endregion
}
