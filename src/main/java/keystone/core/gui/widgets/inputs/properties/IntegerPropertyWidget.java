package keystone.core.gui.widgets.inputs.properties;

import keystone.api.wrappers.Block;
import keystone.core.gui.widgets.inputs.IntegerWidget;
import net.minecraft.state.IntegerProperty;
import net.minecraft.util.text.StringTextComponent;

public class IntegerPropertyWidget extends IntegerWidget
{
    private final Block block;
    private final IntegerProperty property;

    public IntegerPropertyWidget(Block block, IntegerProperty property, int x, int y, int width)
    {
        super(new StringTextComponent(property.getName()), x, y, width, block.getMinecraftBlock().get(property));

        this.block = block;
        this.property = property;

        min = Integer.MAX_VALUE;
        max = Integer.MIN_VALUE;
        for (Integer value : property.getAllowedValues())
        {
            if (value < min) min = value;
            if (value > max) max = value;
        }
    }

    @Override
    protected boolean onSetValue(Integer value)
    {
        block.setMinecraftBlock(block.getMinecraftBlock().with(property, value));
        return true;
    }
}
