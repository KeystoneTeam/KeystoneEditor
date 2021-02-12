package keystone.core.gui.screens.filters;

import keystone.api.Keystone;
import keystone.api.filters.Variable;
import keystone.api.wrappers.BlockPalette;
import keystone.core.gui.widgets.inputs.BlockPaletteWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.lang.reflect.Field;

public class BlockPaletteVariableWidget extends BlockPaletteWidget
{
    private final FilterSelectionScreen parent;
    private final Variable variable;
    private final Field field;

    public BlockPaletteVariableWidget(FilterSelectionScreen parent, Variable variable, Field field, String name, int x, int y, int width) throws IllegalAccessException
    {
        super(new StringTextComponent(name), x, y, width, (BlockPalette)field.get(parent.getFilterInstance()), parent::disableWidgets, parent::restoreWidgets);

        this.parent = parent;
        this.variable = variable;
        this.field = field;
    }

    @Override
    protected void onSetValue(BlockPalette value)
    {
        try
        {
            field.set(parent.getFilterInstance(), value);
        }
        catch (IllegalAccessException e)
        {
            String error = "Could not set BlockPalette variable '" + getMessage().getString() + "'!";
            Keystone.LOGGER.error(error);
            Minecraft.getInstance().player.sendMessage(new StringTextComponent(error).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
            e.printStackTrace();
        }
    }
}
