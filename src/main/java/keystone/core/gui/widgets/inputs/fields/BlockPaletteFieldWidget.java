package keystone.core.gui.widgets.inputs.fields;

import keystone.api.Keystone;
import keystone.api.variables.Hook;
import keystone.api.wrappers.blocks.BlockPalette;
import keystone.core.gui.widgets.inputs.BlockPaletteWidget;
import keystone.core.utils.AnnotationUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public class BlockPaletteFieldWidget extends BlockPaletteWidget
{
    private final Supplier<Object> instance;
    private final Field field;
    private final Hook hook;

    public BlockPaletteFieldWidget(Screen screen, Supplier<Object> instance, Field field, String name, int x, int y, int width) throws IllegalAccessException
    {
        super(Text.literal(name), x, y, width, (BlockPalette)field.get(instance.get()));

        this.instance = instance;
        this.field = field;
        this.hook = field.getAnnotation(Hook.class);
        AnnotationUtils.runHook(instance.get(), hook);
        setTooltip(AnnotationUtils.getFieldTooltip(screen, field));
    }

    @Override
    protected void onSetValue(BlockPalette value)
    {
        try
        {
            field.set(instance.get(), value);
            AnnotationUtils.runHook(instance.get(), hook);
        }
        catch (IllegalAccessException e)
        {
            String error = "Could not set BlockPalette field '" + getMessage().getString() + "'!";
            Keystone.LOGGER.error(error);
            MinecraftClient.getInstance().player.sendMessage(Text.literal(error).styled(style -> style.withColor(Formatting.RED)), false);
            e.printStackTrace();
        }
    }
}
