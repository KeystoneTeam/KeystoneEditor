package keystone.core.gui.widgets.inputs.fields;

import keystone.api.Keystone;
import keystone.api.variables.Hook;
import keystone.core.gui.widgets.inputs.ParsableTextWidget;
import keystone.core.utils.AnnotationUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

public abstract class ParsableTextFieldWidget<T> extends ParsableTextWidget<T>
{
    protected final Supplier<Object> instance;
    protected final Field field;
    private final Hook hook;

    public ParsableTextFieldWidget(Screen screen, Supplier<Object> instance, Field field, String name, int x, int y, int width) throws IllegalAccessException
    {
        super(Text.literal(name), x, y, width, getNonNull(field, instance));

        this.instance = instance;
        this.field = field;
        this.hook = field.getAnnotation(Hook.class);
        AnnotationUtils.runHook(instance.get(), field, hook);
        setTooltip(AnnotationUtils.getFieldTooltip(screen, field));
    }
    
    private static <T> T getNonNull(Field field, Supplier<Object> instance) throws IllegalAccessException
    {
        T value = (T)field.get(instance.get());
        if (value == null)
        {
            try
            {
                Constructor<T> constructor = (Constructor<T>) field.getType().getDeclaredConstructor();
                value = constructor.newInstance();
                field.set(instance.get(), value);
            }
            catch (NoSuchMethodException | InstantiationException | InvocationTargetException e)
            {
                Keystone.LOGGER.error("@Variable '" + field.getName() + "' has a default value of null when its type lacks a zero-parameter constructor!");
                MinecraftClient.getInstance().player.sendMessage(Text.of("@Variable '" + field.getName() + "' has a default value of null when its type lacks a zero-parameter constructor!"));
            }
        }
        return value;
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
