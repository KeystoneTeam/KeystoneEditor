package keystone.core.gui.screens.filters;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.api.filters.Variable;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import keystone.core.gui.widgets.inputs.Dropdown;
import keystone.core.utils.AnnotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.lang.reflect.Field;

public class EnumVariableWidget extends ButtonNoHotkey
{
    private final Minecraft mc;
    private final FontRenderer font;
    private final FilterSelectionScreen parent;
    private final Variable variable;
    private final Field field;
    private final String name;

    private Enum value;
    private Dropdown<Enum> dropdown;

    public EnumVariableWidget(FilterSelectionScreen parent, Variable variable, Field field, String name, int x, int y, int width) throws IllegalAccessException
    {
        super(x, y + 11, width, 20, new StringTextComponent(name), (button) ->
        {
            EnumVariableWidget paletteWidget = (EnumVariableWidget)button;

            paletteWidget.parent.disableWidgets(paletteWidget.dropdown);
            paletteWidget.dropdown.visible = true;
        });

        this.mc = Minecraft.getInstance();
        this.font = mc.fontRenderer;
        this.parent = parent;
        this.variable = variable;
        this.field = field;
        this.name = name;
        this.value = (Enum)field.get(parent.getFilterInstance());

        Class<? extends Enum> enumClass = field.getType().asSubclass(Enum.class);
        this.dropdown = null;
        this.dropdown = new Dropdown<Enum>(x, y + 11, width, new StringTextComponent(name), entry -> new StringTextComponent(AnnotationUtils.getEnumValueName(enumClass.cast(entry))), (entry, title) ->
        {
            try
            {
                Enum newValue = (Enum)enumClass.cast(entry);
                field.set(parent.getFilterInstance(), newValue);
                value = newValue;
                setMessage(title);
            }
            catch (IllegalAccessException e)
            {
                String error = "Cannot set variable '" + name + "'!";
                Keystone.LOGGER.error(error);
                Minecraft.getInstance().player.sendMessage(new StringTextComponent(error).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
                e.printStackTrace();
            }

            parent.restoreWidgets();
            dropdown.visible = false;
        }, enumClass.getEnumConstants());
        this.dropdown.setSelectedEntry(this.value, false);
        setMessage(this.dropdown.getSelectedEntryTitle());
        parent.addWidget(this.dropdown, true, true);
    }
    public static final int getHeight()
    {
        return 31;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        drawCenteredString(matrixStack, font, name, x + width / 2, y - 11, 0xFFFFFF);
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
    }

    public Object getValue() { return value; }
}
