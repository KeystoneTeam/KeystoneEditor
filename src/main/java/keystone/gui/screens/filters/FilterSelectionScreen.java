package keystone.gui.screens.filters;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.api.filters.FilterVariable;
import keystone.api.filters.KeystoneFilter;
import keystone.api.utils.StringUtils;
import keystone.core.filters.FilterCompiler;
import keystone.gui.KeystoneOverlayHandler;
import keystone.gui.screens.hotbar.HotbarButton;
import keystone.gui.screens.hotbar.KeystoneHotbar;
import keystone.gui.screens.hotbar.KeystoneHotbarSlot;
import keystone.gui.widgets.Dropdown;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class FilterSelectionScreen extends Screen
{
    private static boolean open;

    private int panelMinY;
    private int panelMaxX;
    private int panelMaxY;

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

    @Override
    public void closeScreen()
    {
        KeystoneOverlayHandler.removeOverlay(this);
    }
    @Override
    public void onClose()
    {
        open = false;
    }

    @Override
    public void init()
    {
        panelMinY = 37;
        panelMaxX = (int)Math.floor(KeystoneHotbar.getX() * HotbarButton.SCALE) - 5;
        panelMaxY = (int)Math.floor(KeystoneHotbar.getY() * HotbarButton.SCALE) - 5;

        // Select Filter Button
        int selectButtonX = 1 + this.font.getStringWidth(new TranslationTextComponent("keystone.filter_panel.select").getString());
        this.selectFilterButton = new Button(selectButtonX, panelMinY + 1, panelMaxX - selectButtonX - 1, 20, new StringTextComponent("!ERROR!"), (button) ->
        {
            button.active = false;
            this.dropdown.visible = true;
        });

        // Filter selection dropdown
        File[] filterFiles = FilterCompiler.getInstalledFilters();
        KeystoneFilter[] filters = new KeystoneFilter[filterFiles.length];
        for (int i = 0; i < filterFiles.length; i++) filters[i] = FilterCompiler.compileFilter(filterFiles[i].getPath());

        this.dropdown = new Dropdown<>(selectFilterButton.x, selectFilterButton.y, selectFilterButton.getWidth(), panelMaxY - panelMinY, new TranslationTextComponent("keystone.tool.filter.dropdown"),
                filter ->
                {
                    if (filter.isCompiledSuccessfully()) return new StringTextComponent(filter.getName());
                    else return new StringTextComponent(filter.getName()).mergeStyle(TextFormatting.RED);
                },
                (filter, title) ->
                {
                    this.selectFilterButton.active = true;
                    this.selectFilterButton.setMessage(title);
                    this.rebuildFilterVariables();
                }, filters);

        // Run Filter Button
        int buttonWidth = font.getStringWidth(new TranslationTextComponent("keystone.filter_panel.runFilter").getString()) + 10;
        int panelCenter = panelMaxX / 2;
        Button runFilterButton = new Button(panelCenter - buttonWidth / 2, panelMaxY - 21, buttonWidth, 20, new TranslationTextComponent("keystone.filter_panel.runFilter"),
                (button) -> Keystone.runFilter(this.dropdown.getSelectedEntry()));

        // Add event listeners
        this.selectFilterButton.setMessage(this.dropdown.getSelectedEntryTitle());
        addButton(selectFilterButton);
        addButton(runFilterButton);
        addButton(dropdown);
    }
    private void rebuildFilterVariables()
    {
        KeystoneFilter filter = this.dropdown.getSelectedEntry();
        Field[] fields = filter.getClass().getDeclaredFields();
        for (Field field : fields)
        {
            Annotation[] annotations = field.getAnnotations();
            FilterVariable filterVariable = field.getAnnotation(FilterVariable.class);
            if (filterVariable != null)
            {
                String variableName = filterVariable.name() != "" ? filterVariable.name() : StringUtils.addSpacesToSentence(StringUtils.titleCase(field.getName()));
                Keystone.LOGGER.info(variableName);
            }
        }
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        fill(stack, 0, panelMinY, panelMaxX, panelMaxY, 0x80000000);

        drawString(stack, font, new TranslationTextComponent("keystone.filter_panel.select"), 1, panelMinY + 6, 0x8080FF);
        super.render(stack, mouseX, mouseY, partialTicks);
    }
    @Override
    public void tick()
    {
        if (KeystoneHotbar.getSelectedSlot() != KeystoneHotbarSlot.FILTER) closeScreen();
    }
}
