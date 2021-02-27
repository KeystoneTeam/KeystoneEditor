package keystone.core.gui.widgets.inputs.fields;

import keystone.api.Keystone;
import keystone.api.wrappers.BlockPalette;
import keystone.core.gui.widgets.inputs.BlockPaletteWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BlockPaletteFieldWidget extends BlockPaletteWidget
{
    private final Supplier<Object> instance;
    private final Field field;

    public BlockPaletteFieldWidget(Supplier<Object> instance, Field field, String name, int x, int y, int width, Consumer<Widget[]> disableWidgets, Runnable restoreWidgets) throws IllegalAccessException
    {
        super(new StringTextComponent(name), x, y, width, (BlockPalette)field.get(instance.get()), disableWidgets, restoreWidgets);

        this.instance = instance;
        this.field = field;
    }

    @Override
    protected void onSetValue(BlockPalette value)
    {
        try
        {
            field.set(instance.get(), value);
        }
        catch (IllegalAccessException e)
        {
            String error = "Could not set BlockPalette field '" + getMessage().getString() + "'!";
            Keystone.LOGGER.error(error);
            Minecraft.getInstance().player.sendMessage(new StringTextComponent(error).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
            e.printStackTrace();
        }
    }
}
