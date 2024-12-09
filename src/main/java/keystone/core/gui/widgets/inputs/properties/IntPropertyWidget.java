package keystone.core.gui.widgets.inputs.properties;

import keystone.core.gui.widgets.inputs.IntegerWidget;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;

public class IntPropertyWidget extends IntegerWidget
{
    private BlockState block;
    private final IntProperty property;

    public IntPropertyWidget(BlockState block, IntProperty property, int x, int y, int width)
    {
        super(Text.literal(property.getName()), x, y, width, block.get(property));

        this.block = block;
        this.property = property;

        min = Integer.MAX_VALUE;
        max = Integer.MIN_VALUE;
        for (Integer value : property.getValues())
        {
            if (value < min) min = value;
            if (value > max) max = value;
        }
    }

    @Override
    protected boolean onSetValue(Integer value)
    {
        value = Math.max(Math.min(value, max), min);
        block = block.with(property, value);
        return true;
    }
}
