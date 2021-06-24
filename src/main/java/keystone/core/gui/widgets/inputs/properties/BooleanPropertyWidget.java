package keystone.core.gui.widgets.inputs.properties;

import keystone.api.wrappers.Block;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.text.StringTextComponent;

public class BooleanPropertyWidget extends CheckboxButton
{
    private final Block block;
    private final BooleanProperty property;

    public BooleanPropertyWidget(Block block, BooleanProperty property, int x, int y, int width)
    {
        super(x, y, width, getFinalHeight(), new StringTextComponent(property.getName()), block.getMinecraftBlock().getValue(property), true);

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
    public void onPress()
    {
        super.onPress();
        block.setMinecraftBlock(block.getMinecraftBlock().setValue(property, this.selected()));
    }
}
