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
        super(x, y, width, getHeight(), new StringTextComponent(property.getName()), block.getMinecraftBlock().get(property), true);

        this.block = block;
        this.property = property;
    }
    public static int getHeight() { return 20; }

    @Override
    public int getHeightRealms()
    {
        return getHeight();
    }
    @Override
    public void onPress()
    {
        super.onPress();
        block.setMinecraftBlock(block.getMinecraftBlock().with(property, this.isChecked()));
    }
}
