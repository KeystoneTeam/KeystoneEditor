package keystone.core.gui.widgets.inputs.fields;

import keystone.api.Keystone;
import keystone.api.variables.Hook;
import keystone.core.gui.widgets.inputs.EnumWidget;
import keystone.core.utils.AnnotationUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class EnumFieldWidget<T extends Enum<T>> extends EnumWidget<T>
{
    private final Supplier<Object> instance;
    private final Field field;
    private final Hook hook;

    public EnumFieldWidget(Screen screen, Supplier<Object> instance, Field field, String name, int x, int y, int width, BiConsumer<ClickableWidget, ClickableWidget> addDropdown) throws IllegalAccessException
    {
        super(Text.literal(name), x, y, width, (T)field.get(instance.get()), addDropdown);

        this.instance = instance;
        this.field = field;
        this.hook = field.getAnnotation(Hook.class);
        AnnotationUtils.runHook(instance.get(), field, hook);
        setTooltip(AnnotationUtils.getFieldTooltip(screen, field));
    }

    @Override
    protected void onSetValue(Enum value)
    {
        try
        {
            field.set(instance.get(), value);
            AnnotationUtils.runHook(instance.get(), field, hook);
        }
        catch (IllegalAccessException e)
        {
            String error = "Cannot set Enum field '" + getMessage().getString() + "'!";
            Keystone.LOGGER.error(error);
            MinecraftClient.getInstance().player.sendMessage(Text.literal(error).styled(style -> style.withColor(Formatting.RED)), false);
            e.printStackTrace();
        }
    }
}
