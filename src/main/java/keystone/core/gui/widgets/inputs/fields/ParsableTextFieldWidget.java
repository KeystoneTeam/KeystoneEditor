package keystone.core.gui.widgets.inputs.fields;

import keystone.api.Keystone;
import keystone.core.gui.widgets.inputs.ParsableTextWidget;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public abstract class ParsableTextFieldWidget<T> extends ParsableTextWidget<T>
{
    protected final Supplier<Object> instance;
    protected final Field field;

    public ParsableTextFieldWidget(Supplier<Object> instance, Field field, String name, int x, int y, int width) throws IllegalAccessException
    {
        super(new StringTextComponent(name), x, y, width, (T)field.get(instance.get()));

        this.instance = instance;
        this.field = field;
    }

    @Override
    protected boolean onSetValue(T value)
    {
        try
        {
            field.set(instance.get(), value);
            return true;
        }
        catch (IllegalAccessException e)
        {
            String error = "Cannot set " + value.getClass().getName() + " field '" + getMessage().toString() + "'!";
            Keystone.LOGGER.error(error);
            mc.player.sendMessage(new StringTextComponent(error).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
            return false;
        }
    }
}
