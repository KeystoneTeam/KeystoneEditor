package keystone.core.gui.widgets.inputs.fields;

import keystone.api.Keystone;
import keystone.api.variables.Hook;
import keystone.api.wrappers.Biome;
import keystone.core.gui.widgets.inputs.BiomeWidget;
import keystone.core.utils.AnnotationUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class BiomeFieldWidget extends BiomeWidget
{
    private final Supplier<Object> instance;
    private final Field field;
    private final Hook hook;

    public BiomeFieldWidget(Screen screen, Supplier<Object> instance, Field field, String name, int x, int y, int width, BiConsumer<ClickableWidget, Boolean> addDropdown) throws IllegalAccessException
    {
        super(Text.literal(name), x, y, width, (Biome)field.get(instance.get()), addDropdown);
        
        this.instance = instance;
        this.field = field;
        this.hook = field.getAnnotation(Hook.class);
        AnnotationUtils.runHook(instance.get(), field, hook);
        setTooltip(AnnotationUtils.getFieldTooltip(screen, field));
    }

    @Override
    protected void onSetValue(Biome value)
    {
        try
        {
            field.set(instance.get(), value);
            AnnotationUtils.runHook(instance.get(), field, hook);
        }
        catch (IllegalAccessException e)
        {
            String error = "Cannot set Biome field '" + getMessage().getString() + "'!";
            Keystone.LOGGER.error(error);
            MinecraftClient.getInstance().player.sendMessage(Text.literal(error).styled(style -> style.withColor(Formatting.RED)), false);
            e.printStackTrace();
        }
    }
}
