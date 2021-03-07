package keystone.core.gui.widgets.inputs.fields;

import keystone.api.Keystone;
import keystone.api.variables.Hook;
import keystone.api.wrappers.BlockMask;
import keystone.core.gui.widgets.inputs.BlockMaskWidget;
import keystone.core.utils.AnnotationUtils;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BlockMaskFieldWidget extends BlockMaskWidget
{
    private final Supplier<Object> instance;
    private final Field field;
    private final Hook hook;

    public BlockMaskFieldWidget(Supplier<Object> instance, Field field, Hook hook, String name, int x, int y, int width, Consumer<Widget[]> disableWidgets, Runnable restoreWidgets) throws IllegalAccessException
    {
        super(new StringTextComponent(name), x, y, width, (BlockMask)field.get(instance.get()), disableWidgets, restoreWidgets);

        this.instance = instance;
        this.field = field;
        this.hook = hook;
        AnnotationUtils.runHook(instance.get(), hook);
    }

    @Override
    protected void onSetValue(BlockMask value)
    {
        try
        {
            field.set(instance.get(), value);
            AnnotationUtils.runHook(instance.get(), hook);
        }
        catch (IllegalAccessException e)
        {
            String error = "Cannot set BlockMask field '" + getMessage().getString() + "'!";
            Keystone.LOGGER.error(error);
            mc.player.sendMessage(new StringTextComponent(error).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
        }
    }
}
