package keystone.core.gui.widgets.inputs.fields;

import keystone.api.Keystone;
import keystone.api.variables.Hook;
import keystone.api.variables.Variable;
import keystone.api.wrappers.blocks.BlockMask;
import keystone.api.wrappers.blocks.BlockPalette;
import keystone.core.gui.widgets.WidgetList;
import keystone.core.utils.AnnotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FieldWidgetList extends WidgetList
{
    protected final Supplier<Object> instance;
    protected final int intendedWidth;
    protected final Consumer<Widget[]> disableWidgets;
    protected final Runnable restoreWidgets;
    protected final BiConsumer<Widget, Boolean> addDropdown;

    protected int nextWidgetY;

    public FieldWidgetList(ITextComponent label, Supplier<Object> instance, int x, int y, int width, int maxHeight, int padding, Consumer<Widget[]> disableWidgets, Runnable restoreWidgets)
    {
        super(x, y, width, maxHeight, padding, label);

        this.instance = instance;
        this.intendedWidth = width - 2 * padding;
        this.disableWidgets = disableWidgets;
        this.restoreWidgets = restoreWidgets;
        this.addDropdown = this::add;
        this.nextWidgetY = 0;

        Field[] fields = instance.get().getClass().getDeclaredFields();
        for (Field field : fields)
        {
            Variable variable = field.getAnnotation(Variable.class);
            Hook hook = field.getAnnotation(Hook.class);
            if (variable == null) continue;
            String variableName = AnnotationUtils.getFieldName(variable, field);

            try
            {
                field.setAccessible(true);
                nextWidgetY += createVariableEditor(field.getType(), field, hook, variableName, nextWidgetY) + padding;
            }
            catch (SecurityException e)
            {
                String error = "Could not create editor for field '" + variableName + "'!";
                Keystone.LOGGER.error(error);
                Minecraft.getInstance().player.sendMessage(new StringTextComponent(error).withStyle(TextFormatting.RED), Util.NIL_UUID);
                e.printStackTrace();
            }
            catch (IllegalAccessException e)
            {
                String error = "Could not access field '" + variableName + "'!";
                Keystone.LOGGER.error(error);
                Minecraft.getInstance().player.sendMessage(new StringTextComponent(error).withStyle(TextFormatting.RED), Util.NIL_UUID);
                e.printStackTrace();
            }
        }
    }

    public int getNextWidgetY() { return this.nextWidgetY; }

    private int createVariableEditor(Class<?> type, Field field, Hook hook, String name, int y) throws IllegalAccessException
    {
        //region Block Palette
        if (type == BlockPalette.class)
        {
            add(new BlockPaletteFieldWidget(instance, field, hook, name, 0, y, intendedWidth, disableWidgets, restoreWidgets));
            return BlockPaletteFieldWidget.getFinalHeight();
        }
        //endregion
        //region Block Mask
        if (type == BlockMask.class)
        {
            add(new BlockMaskFieldWidget(instance, field, hook, name, 0, y, intendedWidth, disableWidgets, restoreWidgets));
            return BlockMaskFieldWidget.getFinalHeight();
        }
        //endregion
        //region Float
        else if (type == float.class)
        {
            add(new FloatFieldWidget(instance, field, hook, name, 0, y, intendedWidth));
            return ParsableTextFieldWidget.getFinalHeight();
        }
        //endregion
        //region Integer
        else if (type == int.class)
        {
            add(new IntegerFieldWidget(instance, field, hook, name, 0, y, intendedWidth));
            return ParsableTextFieldWidget.getFinalHeight();
        }
        //endregion
        //region String
        else if (type == String.class)
        {
            add(new StringFieldWidget(instance, field, hook, name, 0, y, intendedWidth));
            return ParsableTextFieldWidget.getFinalHeight();
        }
        //endregion
        //region Boolean
        else if (type == boolean.class)
        {
            add(new BooleanFieldWidget(instance, field, hook, name, 0, y, intendedWidth));
            return BooleanFieldWidget.getFinalHeight();
        }
        //endregion
        //region Enum
        else if (Enum.class.isAssignableFrom(type))
        {
            add(new EnumFieldWidget(instance, field, hook, name, 0, y, intendedWidth, disableWidgets, restoreWidgets, addDropdown));
            return EnumFieldWidget.getFinalHeight();
        }
        //endregion

        return 0;
    }
}
