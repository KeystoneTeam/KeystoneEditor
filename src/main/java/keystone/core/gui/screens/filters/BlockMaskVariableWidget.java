package keystone.core.gui.screens.filters;

import keystone.api.Keystone;
import keystone.api.filters.Variable;
import keystone.api.wrappers.BlockMask;
import keystone.core.gui.widgets.inputs.BlockMaskWidget;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.lang.reflect.Field;

public class BlockMaskVariableWidget extends BlockMaskWidget
{
    private final FilterSelectionScreen parent;
    private final Variable variable;
    private final Field field;

    public BlockMaskVariableWidget(FilterSelectionScreen parent, Variable variable, Field field, String name, int x, int y, int width) throws IllegalAccessException
    {
        super(new StringTextComponent(name), x, y, width, (BlockMask)field.get(parent.getFilterInstance()), parent::disableWidgets, parent::restoreWidgets);

        this.parent = parent;
        this.variable = variable;
        this.field = field;
    }

    @Override
    protected void onSetValue(BlockMask value)
    {
        try
        {
            field.set(parent.getFilterInstance(), value);
        }
        catch (IllegalAccessException e)
        {
            String error = "Cannot set filter variable '" + getMessage().toString() + "'!";
            Keystone.LOGGER.error(error);
            mc.player.sendMessage(new StringTextComponent(error).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
        }
    }
}
