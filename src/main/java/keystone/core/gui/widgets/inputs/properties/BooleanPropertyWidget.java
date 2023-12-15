package keystone.core.gui.widgets.inputs.properties;

import keystone.api.wrappers.blocks.Block;
import keystone.core.gui.widgets.inputs.BooleanWidget;
import keystone.core.registries.BlockTypeRegistry;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;

public class BooleanPropertyWidget extends BooleanWidget
{
    private final Block block;
    private final BooleanProperty property;

    public BooleanPropertyWidget(Block block, BooleanProperty property, int x, int y, int width)
    {
        super(x, y, width, getFinalHeight(), Text.literal(property.getName()), block.blockType().getMinecraftBlock().get(property));
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
        block.setBlockType(BlockTypeRegistry.fromMinecraftBlock(block.blockType().getMinecraftBlock().with(property, checked)));
    }
}
