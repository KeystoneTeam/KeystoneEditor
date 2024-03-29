package keystone.core.gui.widgets.inputs.fields;

import keystone.api.Keystone;
import keystone.api.variables.Hook;
import keystone.core.gui.widgets.inputs.ParsableTextWidget;
import keystone.core.utils.AnnotationUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public abstract class ParsableTextFieldWidget<T> extends ParsableTextWidget<T>
{
    protected final Supplier<Object> instance;
    protected final Field field;
    private final Hook hook;

    public ParsableTextFieldWidget(Screen screen, Supplier<Object> instance, Field field, String name, int x, int y, int width) throws IllegalAccessException
    {
        super(Text.literal(name), x, y, width, (T)field.get(instance.get()));

        this.instance = instance;
        this.field = field;
        this.hook = field.getAnnotation(Hook.class);
        AnnotationUtils.runHook(instance.get(), field, hook);
        setTooltip(AnnotationUtils.getFieldTooltip(screen, field));
    }

    @Override
    protected boolean onSetValue(T value)
    {
        try
        {
            field.set(instance.get(), value);
            AnnotationUtils.runHook(instance.get(), field, hook);
            return true;
        }
        catch (IllegalAccessException e)
        {
            String error = "Cannot set " + value.getClass().getName() + " field '" + getMessage().getString() + "'!";
            Keystone.LOGGER.error(error);
            mc.player.sendMessage(Text.literal(error).styled(style -> style.withColor(Formatting.RED)), false);
            return false;
        }
    }
}
