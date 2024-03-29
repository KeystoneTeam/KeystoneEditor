package keystone.core.gui.widgets.inputs.fields;

import keystone.api.Keystone;
import keystone.api.variables.Hook;
import keystone.core.gui.widgets.inputs.BooleanWidget;
import keystone.core.utils.AnnotationUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public class BooleanFieldWidget extends BooleanWidget
{
    private final Supplier<Object> instance;
    private final Field field;
    private final Hook hook;
    private final String name;

    public BooleanFieldWidget(Screen screen, Supplier<Object> instance, Field field, String name, int x, int y, int width) throws IllegalAccessException
    {
        super(x, y, width, getFinalHeight(), Text.literal(name), (boolean)field.get(instance.get()), true);

        this.instance = instance;
        this.field = field;
        this.name = name;
        this.hook = field.getAnnotation(Hook.class);
        AnnotationUtils.runHook(instance.get(), field, hook);
        setTooltip(AnnotationUtils.getFieldTooltip(screen, field));
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
            AnnotationUtils.runHook(instance.get(), field, hook);
        }
        catch (IllegalArgumentException | IllegalAccessException e)
        {
            String error = "Cannot set Boolean field '" + name + "'!";
            Keystone.LOGGER.error(error);
            MinecraftClient.getInstance().player.sendMessage(Text.literal(error).styled(style -> style.withColor(Formatting.RED)), false);
        }
    }
}
