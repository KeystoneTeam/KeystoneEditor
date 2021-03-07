package keystone.core.gui.screens.filters;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import keystone.api.Keystone;
import keystone.api.filters.KeystoneFilter;
import keystone.core.events.KeystoneHotbarEvent;
import keystone.core.filters.FilterCompiler;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.screens.KeystoneOverlay;
import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import keystone.core.gui.widgets.inputs.Dropdown;
import keystone.core.gui.widgets.inputs.fields.*;
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
    private static KeystoneFilter selectedFilter;
    private static KeystoneFilter filterInstance;

    private int panelMinY;
    private int panelMaxX;
    private int panelMaxY;

    private KeystoneFilter[] compiledFilters;

    private Button selectFilterButton;
    private Dropdown<KeystoneFilter> dropdown;
    private FieldWidgetList filterVariablesList;

    protected FilterSelectionScreen()
    {
        super(new TranslationTextComponent("keystone.screen.filterPanel"));
    }
    public static void open()
    {
        if (open == null)
        {
            open = new FilterSelectionScreen();
            KeystoneOverlayHandler.addOverlay(open);
        }
    }

    //region Static Event Handlers
    @SubscribeEvent(priority = EventPriority.LOW)
    public static final void onHotbarChanged(final KeystoneHotbarEvent event)
    {
        if (event.isCanceled()) return;

        if (event.slot == KeystoneHotbarSlot.FILTER) open();
        else if (open != null) open.closeScreen();
    }
    //endregion

    //region Screen Overrides
    @Override
    public void onClose()
    {
        open = null;
        compiledFilters = null;
    }
    @Override
    public void init()
    {
        // Compile filters
        boolean recompiled = false;
        if (compiledFilters == null)
        {
            recompiled = true;
            File[] filterFiles = FilterCompiler.getInstalledFilters();
            compiledFilters = new KeystoneFilter[filterFiles.length];
            for (int i = 0; i < filterFiles.length; i++) compiledFilters[i] = FilterCompiler.compileFilter(filterFiles[i].getPath());
        }

        // Calculate panel size
        if (filterInstance == null)
        {
            selectedFilter = compiledFilters[0];
            recreateFilterInstance();
        }
        else if (recompiled)
        {
            for (KeystoneFilter filter : compiledFilters)
            {
                if (filter.getName().equals(selectedFilter.getName()))
                {
                    selectedFilter = filter;
                    break;
                }
            }
            recreateFilterInstance();
        }
        panelMaxX = KeystoneHotbar.getX() - 5;
        filterVariablesList = new FieldWidgetList(this::getFilterInstance, PADDING, panelMaxX - 10, this::disableWidgets, this::restoreWidgets);
        filterVariablesList.bake();
        int centerHeight = height / 2;
        int halfPanelHeight = 25 + PADDING + filterVariablesList.getHeight() / 2;
        panelMinY = centerHeight - halfPanelHeight;
        panelMaxY = centerHeight + halfPanelHeight;

        // Select Filter Button
        int selectButtonX = 5 + this.font.getStringWidth(new TranslationTextComponent("keystone.filter_panel.select").getString());
        this.selectFilterButton = new ButtonNoHotkey(selectButtonX, panelMinY + 5, panelMaxX - selectButtonX - 5, 20, new StringTextComponent("!ERROR!"), (button) ->
        {
            disableWidgets(this.dropdown);
            this.dropdown.visible = true;
        });

        // Filter selection dropdown
        this.dropdown = new Dropdown<>(selectFilterButton.x, selectFilterButton.y, selectFilterButton.getWidth(), new TranslationTextComponent("keystone.tool.filter.dropdown"),
                filter ->
                {
                    if (filter.isCompiledSuccessfully()) return new StringTextComponent(filter.getName());
                    else return new StringTextComponent(filter.getName()).mergeStyle(TextFormatting.RED);
                },
                (filter, title) ->
                {
                    restoreWidgets();
                    selectedFilter = filter;
                    this.selectFilterButton.setMessage(title);
                    recreateFilterInstance();

                    this.init(minecraft, width, height);
                }, compiledFilters);
        if (selectedFilter != null)
        {
            for (int i = 0; i < dropdown.size(); i++)
            {
                KeystoneFilter filter = dropdown.getEntry(i);
                if (filter.getName().equals(selectedFilter.getName()))
                {
                    dropdown.setSelectedEntry(filter, false);
                    break;
                }
            }
        }

        // Run Filter Button
        int buttonWidth = font.getStringWidth(new TranslationTextComponent("keystone.filter_panel.runFilter").getString()) + 10;
        int panelCenter = panelMaxX / 2;
        ButtonNoHotkey runFilterButton = new ButtonNoHotkey(panelCenter - buttonWidth / 2, panelMaxY - 25, buttonWidth, 20, new TranslationTextComponent("keystone.filter_panel.runFilter"), button -> runFilter());

        // Add buttons
        this.selectFilterButton.setMessage(this.dropdown.getSelectedEntryTitle());
        addButton(selectFilterButton);
        addButton(runFilterButton);
        this.children.add(dropdown);

        // Create Filter Variables
        filterVariablesList.offset(5, panelMinY + ((panelMaxY - panelMinY) / 2) - (filterVariablesList.getHeight() / 2));
        filterVariablesList.addWidgets(this::addButton);
        filterVariablesList.addQueuedWidgets(this::addButton);

        filterInstance.undirtyEditor();
    }
    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        if (filterInstance != null && filterInstance.isEditorDirtied()) init(minecraft, width, height);

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        fill(stack, 0, panelMinY, panelMaxX, panelMaxY, 0x80000000);

        drawString(stack, font, new TranslationTextComponent("keystone.filter_panel.select"), 5, panelMinY + 11, 0x8080FF);
        super.render(stack, mouseX, mouseY, partialTicks);
        this.dropdown.render(stack, mouseX, mouseY, partialTicks);

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }
    //endregion
    //region Getters
    public KeystoneFilter getFilterInstance() { return filterInstance; }
    //endregion
    //region Helpers
    private void recreateFilterInstance()
    {
        if (selectedFilter == null) filterInstance = null;
        else
        {
            try
            {
                filterInstance = selectedFilter.getClass().newInstance().setName(selectedFilter.getName());
                if (selectedFilter.isCompiledSuccessfully()) filterInstance.compiledSuccessfully();
            }
            catch (InstantiationException | IllegalAccessException e) { e.printStackTrace(); }
        }
    }
    private void runFilter()
    {
        for (Widget widget : buttons) if (widget instanceof TextFieldWidget) ((TextFieldWidget) widget).setFocused2(false);

        if (filterInstance != null) Keystone.runFilter(filterInstance);
        else
        {
            String error = "Could not create instance of filter '" + selectedFilter.getName() + "'!";
            Keystone.LOGGER.error(error);
            minecraft.player.sendMessage(new StringTextComponent(error).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
        }
    }
    //endregion
}
