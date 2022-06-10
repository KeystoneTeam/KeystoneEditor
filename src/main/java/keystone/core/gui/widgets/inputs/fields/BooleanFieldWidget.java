package keystone.core.gui.widgets.inputs.fields;

import keystone.api.Keystone;
import keystone.api.variables.Hook;
import keystone.core.utils.AnnotationUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public class BooleanFieldWidget extends CheckboxWidget
{
    private final Supplier<Object> instance;
    private final Field field;
    private final Hook hook;
    private final String name;

    public BooleanFieldWidget(Supplier<Object> instance, Field field, Hook hook, String name, int x, int y, int width) throws IllegalAccessException
    {
        super(x, y, width, getFinalHeight(), Text.literal(name), (boolean)field.get(instance.get()), true);

        this.instance = instance;
        this.field = field;
        this.hook = hook;
        this.name = name;
        AnnotationUtils.runHook(instance.get(), hook);
    }
    public static int getFinalHeight() { return 20; }

    @Override
    public int getHeight()
    {
        return getFinalHeight();
    }
    @Override
    public void onPress()
    {
        super.onPress();
        try
        {
            field.set(instance.get(), this.isChecked());
            AnnotationUtils.runHook(instance.get(), hook);
        }
        catch (IllegalArgumentException | IllegalAccessException e)
        {
            String error = "Cannot set Boolean field '" + name + "'!";
            Keystone.LOGGER.error(error);
            MinecraftClient.getInstance().player.sendMessage(Text.literal(error).styled(style -> style.withColor(Formatting.RED)), false);
        }
    }
}
