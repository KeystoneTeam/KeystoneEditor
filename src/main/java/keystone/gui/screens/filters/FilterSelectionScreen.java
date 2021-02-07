package keystone.gui.screens.filters;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import keystone.api.Keystone;
import keystone.api.filters.KeystoneFilter;
import keystone.api.filters.Variable;
import keystone.api.wrappers.BlockMask;
import keystone.api.wrappers.BlockPalette;
import keystone.core.filters.FilterCompiler;
import keystone.core.utils.AnnotationUtils;
import keystone.gui.KeystoneOverlayHandler;
import keystone.gui.screens.hotbar.KeystoneHotbar;
import keystone.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.gui.widgets.ButtonNoHotkey;
import keystone.gui.widgets.Dropdown;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterSelectionScreen extends Screen
{
    private static final int PADDING = 5;
    private static boolean open;
    private static KeystoneFilter selectedFilter;
    private static KeystoneFilter filterInstance;

    private int panelMinY;
    private int panelMaxX;
    private int panelMaxY;
    private int totalVariableHeight;

    private KeystoneFilter[] compiledFilters;
    private boolean restoreWidgets = false;
    private Map<Widget, Boolean> widgetsActive = new HashMap<>();
    private List<Widget> addWidgetQueue = new ArrayList<>();
    private List<Boolean> addButtonQueue = new ArrayList<>();

    private Button selectFilterButton;
    private Dropdown<KeystoneFilter> dropdown;

    protected FilterSelectionScreen()
    {
        super(new TranslationTextComponent("keystone.filter_panel.title"));
    }
    public static void openScreen()
    {
        if (!open)
        {
            KeystoneOverlayHandler.addOverlay(new FilterSelectionScreen());
            open = true;
        }
    }

    //region Screen Overrides
    @Override
    public void closeScreen()
    {
        KeystoneOverlayHandler.removeOverlay(this);
    }
    @Override
    public void onClose()
    {
        open = false;
        compiledFilters = null;
    }
    @Override
    public void init()
    {
        addWidgetQueue.clear();
        addButtonQueue.clear();

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
        recalculateVariablesHeight();

        int centerHeight = height / 2;
        int halfPanelHeight = 25 + PADDING + totalVariableHeight / 2;
        panelMinY = centerHeight - halfPanelHeight;
        panelMaxX = KeystoneHotbar.getX() - 5;
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
        rebuildFilterVariables();

        // Add queued buttons
        for (int i = 0; i < addWidgetQueue.size(); i++)
        {
            Widget widget = addWidgetQueue.get(i);
            boolean button = addButtonQueue.get(i);
            if (button) addButton(widget);
            else this.children.add(widget);
        }
        this.addWidgetQueue.clear();
        this.addButtonQueue.clear();
    }
    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        if (restoreWidgets)
        {
            for (Map.Entry<Widget, Boolean> entry : widgetsActive.entrySet()) entry.getKey().active = entry.getValue();
            restoreWidgets = false;
        }

        fill(stack, 0, panelMinY, panelMaxX, panelMaxY, 0x80000000);

        drawString(stack, font, new TranslationTextComponent("keystone.filter_panel.select"), 5, panelMinY + 11, 0x8080FF);
        super.render(stack, mouseX, mouseY, partialTicks);
        this.dropdown.render(stack, mouseX, mouseY, partialTicks);

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }
    @Override
    public void tick()
    {
        if (KeystoneHotbar.getSelectedSlot() != KeystoneHotbarSlot.FILTER) closeScreen();
        else for (Widget widget : buttons) if (widget instanceof TextFieldWidget) ((TextFieldWidget) widget).tick();
    }
    //endregion
    //region Filter Variables
    private void updateTotalVariableHeight(Class<?> type)
    {
        if (type == BlockPalette.class) totalVariableHeight += BlockPaletteVariableWidget.getHeight();
        else if (type == BlockMask.class) totalVariableHeight += BlockMaskVariableWidget.getHeight();
        else if (type == float.class) totalVariableHeight += AbstractTextVariableWidget.getHeight();
        else if (type == int.class) totalVariableHeight += AbstractTextVariableWidget.getHeight();
        else if (type == String.class) totalVariableHeight += AbstractTextVariableWidget.getHeight();
        else if (type == boolean.class) totalVariableHeight += BooleanVariableWidget.getHeight();
        else if (Enum.class.isAssignableFrom(type)) totalVariableHeight += EnumVariableWidget.getHeight();
    }
    private int createVariableEditor(Class<?> type, Field field, Variable variable, String variableName, int y) throws IllegalAccessException
    {
        //region Block Palette
        if (type == BlockPalette.class)
        {
            addButton(new BlockPaletteVariableWidget(this, variable, field, variableName, 5, y, panelMaxX - 10));
            return BlockPaletteVariableWidget.getHeight();
        }
        //endregion
        //region Block Mask
        if (type == BlockMask.class)
        {
            addButton(new BlockMaskVariableWidget(this, variable, field, variableName, 5, y, panelMaxX - 10));
            return BlockMaskVariableWidget.getHeight();
        }
        //endregion
        //region Float
        else if (type == float.class)
        {
            addButton(new FloatVariableWidget(this, variable, field, variableName, 5, y, panelMaxX - 10));
            return AbstractTextVariableWidget.getHeight();
        }
        //endregion
        //region Integer
        else if (type == int.class)
        {
            addButton(new IntegerVariableWidget(this, variable, field, variableName, 5, y, panelMaxX - 10));
            return AbstractTextVariableWidget.getHeight();
        }
        //endregion
        //region String
        else if (type == String.class)
        {
            addButton(new StringVariableWidget(this, variable, field, variableName, 5, y, panelMaxX - 10));
            return AbstractTextVariableWidget.getHeight();
        }
        //endregion
        //region Boolean
        else if (type == boolean.class)
        {
            addButton(new BooleanVariableWidget(this, variable, field, variableName, 5, y, panelMaxX - 10));
            return BooleanVariableWidget.getHeight();
        }
        //endregion
        //region Enum
        else if (Enum.class.isAssignableFrom(type))
        {
            addButton(new EnumVariableWidget(this, variable, field, variableName, 5, y, panelMaxX - 10));
            return EnumVariableWidget.getHeight();
        }
        //endregion

        return 0;
    }
    //endregion
    //region Widgets
    public void addWidget(Widget widget, boolean button, boolean queueAddition)
    {
        if (queueAddition)
        {
            addWidgetQueue.add(widget);
            addButtonQueue.add(button);
        }
        else
        {
            if (button) addButton(widget);
            else this.children.add(widget);
        }
    }
    public void disableWidgets(Widget... keepActive)
    {
        this.widgetsActive.clear();
        for (Widget widget : this.buttons)
        {
            widgetsActive.put(widget, widget.active);
            widget.active = false;
        }
        for (Widget widget : keepActive)
        {
            widgetsActive.put(widget, true);
            widget.active = true;
        }
    }
    public void restoreWidgets()
    {
        this.restoreWidgets = true;
    }
    private void recalculateVariablesHeight()
    {
        totalVariableHeight = 0;
        if (filterInstance == null) return;

        Field[] fields = filterInstance.getClass().getDeclaredFields();
        for (Field field : fields)
        {
            Variable variable = field.getAnnotation(Variable.class);
            if (variable == null) continue;
            String variableName = AnnotationUtils.getFieldName(variable, field);

            try
            {
                field.setAccessible(true);
                updateTotalVariableHeight(field.getType());
                totalVariableHeight += PADDING;
            }
            catch (SecurityException e)
            {
                String error = "Could not create editor for BlockPalette variable '" + variableName + "'!";
                Keystone.LOGGER.error(error);
                Minecraft.getInstance().player.sendMessage(new StringTextComponent(error).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
                e.printStackTrace();
            }
        }
        if (totalVariableHeight > 0) totalVariableHeight -= PADDING;
    }
    private void rebuildFilterVariables()
    {
        if (filterInstance == null) return;

        Field[] fields = filterInstance.getClass().getDeclaredFields();
        int y = panelMinY + ((panelMaxY - panelMinY) / 2) - (totalVariableHeight / 2);
        for (Field field : fields)
        {
            Variable variable = field.getAnnotation(Variable.class);
            if (variable == null) continue;
            String variableName = AnnotationUtils.getFieldName(variable, field);

            try
            {
                field.setAccessible(true);
                y += createVariableEditor(field.getType(), field, variable, variableName, y) + PADDING;
            }
            catch (SecurityException e)
            {
                String error = "Could not create editor for BlockPalette variable '" + variableName + "'!";
                Keystone.LOGGER.error(error);
                Minecraft.getInstance().player.sendMessage(new StringTextComponent(error).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
                e.printStackTrace();
            }
            catch (IllegalAccessException e)
            {
                String error = "Could not access BlockPalette variable '" + variableName + "'!";
                Keystone.LOGGER.error(error);
                Minecraft.getInstance().player.sendMessage(new StringTextComponent(error).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
                e.printStackTrace();
            }
        }
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
