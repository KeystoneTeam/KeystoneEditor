package keystone.core.gui.widgets.inputs.fields;

import keystone.api.Keystone;
import keystone.api.variables.Hook;
import keystone.core.gui.widgets.inputs.ParsableTextWidget;
import keystone.core.utils.AnnotationUtils;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public abstract class ParsableTextFieldWidget<T> extends ParsableTextWidget<T>
{
    protected final Supplier<Object> instance;
    protected final Field field;
    private final Hook hook;

    public ParsableTextFieldWidget(Supplier<Object> instance, Field field, Hook hook, String name, int x, int y, int width) throws IllegalAccessException
    {
        super(new LiteralText(name), x, y, width, (T)field.get(instance.get()));

        this.instance = instance;
        this.field = field;
        this.hook = hook;
        AnnotationUtils.runHook(instance.get(), hook);
    }

    @Override
    protected boolean onSetValue(T value)
    {
        try
        {
            field.set(instance.get(), value);
            AnnotationUtils.runHook(instance.get(), hook);
            return true;
        }
        catch (IllegalAccessException e)
        {
            String error = "Cannot set " + value.getClass().getName() + " field '" + getMessage().getString() + "'!";
            Keystone.LOGGER.error(error);
            mc.player.sendMessage(new LiteralText(error).styled(style -> style.withColor(Formatting.RED)), false);
            return false;
        }
    }
}
