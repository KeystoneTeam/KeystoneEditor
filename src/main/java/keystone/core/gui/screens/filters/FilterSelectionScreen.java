package keystone.core.gui.screens.filters;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.api.filters.KeystoneFilter;
import keystone.core.events.KeystoneHotbarEvent;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.screens.KeystoneOverlay;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import keystone.core.gui.widgets.inputs.Dropdown;
import keystone.core.gui.widgets.inputs.fields.FieldWidgetList;
import keystone.core.modules.filter.FilterDirectoryManager;
import keystone.core.modules.filter.FilterModule;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.File;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
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

    private Button selectFilterButton;
    private Dropdown<File> dropdown;
    private FieldWidgetList filterVariablesList;

    protected FilterSelectionScreen()
    {
        super(new TranslationTextComponent("keystone.screen.filterPanel"));
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
    public static void dirty()
    {
        dirtied = true;
    }

    //region Static Event Handlers
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onHotbarChanged(final KeystoneHotbarEvent event)
    {
        if (event.isCanceled()) return;

        if (event.slot == KeystoneHotbarSlot.FILTER) open();
        else if (open != null) open.onClose();
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

        // Calculate panel size
        panelMaxX = Math.min(KeystoneHotbar.getX() - 5, 280);
        int maxPanelHeight = minecraft.getWindow().getGuiScaledHeight() - 50 - 2 * PADDING;
        filterVariablesList = new FieldWidgetList(new TranslationTextComponent("keystone.filter_panel.filterVariables"), this::getFilter, 0, 0, panelMaxX, maxPanelHeight, PADDING, this::disableWidgets, this::restoreWidgets);
        filterVariablesList.bake();
        int centerHeight = height / 2;
        int halfPanelHeight = 25 + PADDING + filterVariablesList.getHeight() / 2;
        panelMinY = centerHeight - halfPanelHeight;
        panelMaxY = centerHeight + halfPanelHeight;

        // Select Filter Button
        int selectButtonX = 5 + this.font.width(new TranslationTextComponent("keystone.filter_panel.select").getString());
        this.selectFilterButton = new ButtonNoHotkey(selectButtonX, panelMinY + 5, panelMaxX - selectButtonX - 5, 20, new StringTextComponent("!ERROR!"), (button) ->
        {
            disableWidgets(this.dropdown);
            this.dropdown.visible = true;
        });

        // Filter selection dropdown
        this.dropdown = new Dropdown<>(selectFilterButton.x, selectFilterButton.y, selectFilterButton.getWidth(), new TranslationTextComponent("keystone.tool.filter.dropdown"),
                filterFile ->
                {
                    KeystoneFilter filter = filterManager.getFilter(filterFile);
                    if (filter.isCompiledSuccessfully()) return new StringTextComponent(filter.getName());
                    else return new StringTextComponent(filter.getName()).withStyle(TextFormatting.RED);
                },
                (filterFile, title) ->
                {
                    restoreWidgets();
                    selectedFilterFile = filterFile;
                    this.selectFilterButton.setMessage(title);
                    this.init(minecraft, width, height);
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
        int buttonWidth = font.width(new TranslationTextComponent("keystone.filter_panel.runFilter").getString()) + 10;
        int panelCenter = panelMaxX / 2;
        ButtonNoHotkey runFilterButton = new ButtonNoHotkey(panelCenter - buttonWidth / 2, panelMaxY - 25, buttonWidth, 20, new TranslationTextComponent("keystone.filter_panel.runFilter"), button -> runFilter());

        // Add buttons
        this.selectFilterButton.setMessage(this.dropdown.getSelectedEntryTitle());
        addButton(selectFilterButton);
        addButton(runFilterButton);
        this.children.add(dropdown);

        // Create Filter Variables
        filterVariablesList.offset(0, panelMinY + ((panelMaxY - panelMinY) / 2) - (filterVariablesList.getHeight() / 2));
        addButton(filterVariablesList);

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
                init(minecraft, width, height);
            }
        }
        if (dirtied)
        {
            init(minecraft, width, height);
            dirtied = false;
        }

        fill(stack, 0, panelMinY, panelMaxX, panelMaxY, 0x80000000);

        drawString(stack, font, new TranslationTextComponent("keystone.filter_panel.select"), 5, panelMinY + 11, 0x8080FF);
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
        for (Widget widget : buttons) if (widget instanceof TextFieldWidget) ((TextFieldWidget) widget).setFocus(false);
        if (selectedFilterFile != null) filterModule.runFilter(filterManager.getFilter(selectedFilterFile));
    }
    //endregion
}
