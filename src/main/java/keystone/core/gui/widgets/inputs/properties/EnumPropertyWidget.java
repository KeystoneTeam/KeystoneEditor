package keystone.core.gui.widgets.inputs.properties;

import keystone.api.wrappers.blocks.BlockType;
import keystone.core.gui.widgets.inputs.EnumWidget;
import keystone.core.registries.BlockTypeRegistry;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

import java.util.function.BiConsumer;

public class EnumPropertyWidget<T extends Enum<T> & StringIdentifiable> extends EnumWidget<T>
{
    private BlockType block;
    private final EnumProperty<T> property;

    public EnumPropertyWidget(BlockType block, EnumProperty<T> property, int x, int y, int width, BiConsumer<ClickableWidget, ClickableWidget> addDropdown)
    {
        super(Text.literal(property.getName()), x, y, width, block.getMinecraftBlock().get(property), addDropdown);

        this.block = block;
        this.property = property;
        build();
    }

    @Override
    protected boolean autoBuild() { return false; }
    @Override
    protected boolean isValueAllowed(T value) { return property.getValues().contains(value); }
    @Override
    protected void onSetValue(T value)
    {
        block = BlockTypeRegistry.fromMinecraftBlock(block.getMinecraftBlock().with(property, value));
    }
}
