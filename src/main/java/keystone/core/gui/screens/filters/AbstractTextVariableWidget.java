package keystone.core.gui.screens.filters;

import keystone.api.Keystone;
import keystone.api.filters.Variable;
import keystone.core.gui.widgets.inputs.ParsableTextFieldWidget;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.lang.reflect.Field;

public abstract class AbstractTextVariableWidget<T> extends ParsableTextFieldWidget<T>
{
    protected final FilterSelectionScreen parent;
    protected final Variable variable;
    protected final Field field;

    public AbstractTextVariableWidget(FilterSelectionScreen parent, Variable variable, Field field, String name, int x, int y, int width) throws IllegalAccessException
    {
        super(new StringTextComponent(name), x, y, width, (T)field.get(parent.getFilterInstance()));

        this.parent = parent;
        this.variable = variable;
        this.field = field;
    }

    @Override
    protected boolean onSetValue(T value)
    {
        try
        {
            field.set(parent.getFilterInstance(), value);
            return true;
        }
        catch (IllegalAccessException e)
        {
            String error = "Cannot set filter variable '" + getMessage().toString() + "'!";
            Keystone.LOGGER.error(error);
            mc.player.sendMessage(new StringTextComponent(error).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
            return false;
        }
    }
}
