package keystone.core.gui.widgets.inputs.properties;

import keystone.api.wrappers.blocks.Block;
import keystone.core.gui.widgets.inputs.IntegerWidget;
import keystone.core.registries.BlockTypeRegistry;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;

public class IntPropertyWidget extends IntegerWidget
{
    private final Block block;
    private final IntProperty property;

    public IntPropertyWidget(Block block, IntProperty property, int x, int y, int width)
    {
        super(Text.literal(property.getName()), x, y, width, block.blockType().getMinecraftBlock().get(property));

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
        block.setBlockType(BlockTypeRegistry.fromMinecraftBlock(block.blockType().getMinecraftBlock().with(property, value)));
        return true;
    }
}
