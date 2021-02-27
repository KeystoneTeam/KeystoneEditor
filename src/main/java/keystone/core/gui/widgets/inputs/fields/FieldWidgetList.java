package keystone.core.gui.widgets.inputs.fields;

import keystone.api.Keystone;
import keystone.api.filters.Variable;
import keystone.api.wrappers.BlockMask;
import keystone.api.wrappers.BlockPalette;
import keystone.core.gui.widgets.WidgetList;
import keystone.core.utils.AnnotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FieldWidgetList extends WidgetList
{
    protected final Supplier<Object> instance;
    protected final int intendedWidth;
    protected final Consumer<Widget[]> disableWidgets;
    protected final Runnable restoreWidgets;

    public FieldWidgetList(Supplier<Object> instance, int padding, int width, Consumer<Widget[]> disableWidgets, Runnable restoreWidgets)
    {
        this.instance = instance;
        this.intendedWidth = width;
        this.disableWidgets = disableWidgets;
        this.restoreWidgets = restoreWidgets;

        int y = 0;
        Field[] fields = instance.get().getClass().getDeclaredFields();
        for (Field field : fields)
        {
            Variable variable = field.getAnnotation(Variable.class);
            if (variable == null) continue;
            String variableName = AnnotationUtils.getFieldName(variable, field);

            try
            {
                field.setAccessible(true);
                y += createVariableEditor(field.getType(), field, variableName, y) + padding;
            }
            catch (SecurityException e)
            {
                String error = "Could not create editor for field '" + variableName + "'!";
                Keystone.LOGGER.error(error);
                Minecraft.getInstance().player.sendMessage(new StringTextComponent(error).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
                e.printStackTrace();
            }
            catch (IllegalAccessException e)
            {
                String error = "Could not access field '" + variableName + "'!";
                Keystone.LOGGER.error(error);
                Minecraft.getInstance().player.sendMessage(new StringTextComponent(error).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
                e.printStackTrace();
            }
        }
    }

    @Override
    public void bake()
    {
        super.bake();
        height += y;
        y = 0;
    }

    private int createVariableEditor(Class<?> type, Field field, String name, int y) throws IllegalAccessException
    {
        //region Block Palette
        if (type == BlockPalette.class)
        {
            add(new BlockPaletteFieldWidget(instance, field, name, 0, y, intendedWidth, disableWidgets, restoreWidgets));
            return BlockPaletteFieldWidget.getHeight();
        }
        //endregion
        //region Block Mask
        if (type == BlockMask.class)
        {
            add(new BlockMaskFieldWidget(instance, field, name, 0, y, intendedWidth, disableWidgets, restoreWidgets));
            return BlockMaskFieldWidget.getHeight();
        }
        //endregion
        //region Float
        else if (type == float.class)
        {
            add(new FloatFieldWidget(instance, field, name, 0, y, intendedWidth));
            return ParsableTextFieldWidget.getHeight();
        }
        //endregion
        //region Integer
        else if (type == int.class)
        {
            add(new IntegerFieldWidget(instance, field, name, 0, y, intendedWidth));
            return ParsableTextFieldWidget.getHeight();
        }
        //endregion
        //region String
        else if (type == String.class)
        {
            add(new StringFieldWidget(instance, field, name, 0, y, intendedWidth));
            return ParsableTextFieldWidget.getHeight();
        }
        //endregion
        //region Boolean
        else if (type == boolean.class)
        {
            add(new BooleanFieldWidget(instance, field, name, 0, y, intendedWidth));
            return BooleanFieldWidget.getHeight();
        }
        //endregion
        //region Enum
        else if (Enum.class.isAssignableFrom(type))
        {
            add(new EnumFieldWidget(instance, field, name, 0, y, intendedWidth, disableWidgets, restoreWidgets, this::add));
            return EnumFieldWidget.getHeight();
        }
        //endregion

        return 0;
    }
}
