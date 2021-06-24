package keystone.core.gui.widgets.inputs.fields;

import keystone.api.Keystone;
import keystone.api.variables.Hook;
import keystone.core.gui.widgets.inputs.EnumWidget;
import keystone.core.utils.AnnotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EnumFieldWidget<T extends Enum<T>> extends EnumWidget<T>
{
    private final Supplier<Object> instance;
    private final Field field;
    private final Hook hook;

    public EnumFieldWidget(Supplier<Object> instance, Field field, Hook hook, String name, int x, int y, int width, Consumer<Widget[]> disableWidgets, Runnable restoreWidgets, BiConsumer<Widget, Boolean> addDropdown) throws IllegalAccessException
    {
        super(new StringTextComponent(name), x, y, width, (T)field.get(instance.get()), disableWidgets, restoreWidgets, addDropdown);

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
            Minecraft.getInstance().player.sendMessage(new StringTextComponent(error).withStyle(TextFormatting.RED), Util.NIL_UUID);
            e.printStackTrace();
        }
    }
}
