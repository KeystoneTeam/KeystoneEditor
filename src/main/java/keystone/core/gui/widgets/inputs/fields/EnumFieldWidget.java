package keystone.core.gui.widgets.inputs.fields;

import keystone.api.Keystone;
import keystone.api.variables.Hook;
import keystone.core.gui.widgets.inputs.EnumWidget;
import keystone.core.utils.AnnotationUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EnumFieldWidget<T extends Enum<T>> extends EnumWidget<T>
{
    private final Supplier<Object> instance;
    private final Field field;
    private final Hook hook;

    public EnumFieldWidget(Supplier<Object> instance, Field field, Hook hook, String name, int x, int y, int width, Consumer<ClickableWidget[]> disableWidgets, Runnable restoreWidgets, BiConsumer<ClickableWidget, Boolean> addDropdown) throws IllegalAccessException
    {
        super(Text.literal(name), x, y, width, (T)field.get(instance.get()), disableWidgets, restoreWidgets, addDropdown);

        this.instance = instance;
        this.field = field;
        this.hook = hook;
        AnnotationUtils.runHook(instance.get(), hook);
    }

    @Override
    protected void onSetValue(Enum value)
    {
        try
        {
            field.set(instance.get(), value);
            AnnotationUtils.runHook(instance.get(), hook);
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
