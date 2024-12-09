package keystone.core.gui.widgets.inputs.properties;

import keystone.core.gui.widgets.inputs.BooleanWidget;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;

public class BooleanPropertyWidget extends BooleanWidget
{
    private BlockState block;
    private final BooleanProperty property;

    public BooleanPropertyWidget(BlockState block, BooleanProperty property, int x, int y, int width)
    {
        super(x, y, width, getFinalHeight(), Text.literal(property.getName()), block.get(property));
        this.block = block;
        this.property = property;
    }
    public static int getFinalHeight() { return 20; }

    @Override
    public int getHeight()
    {
        return getFinalHeight();
    }
    
    @Override
    public void onChanged(boolean checked)
    {
        block = block.with(property, checked);
    }
}
