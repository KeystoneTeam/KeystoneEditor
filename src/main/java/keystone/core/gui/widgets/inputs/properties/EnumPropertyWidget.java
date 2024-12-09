package keystone.core.gui.widgets.inputs.properties;

import keystone.core.gui.widgets.inputs.EnumWidget;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

import java.util.function.BiConsumer;

public class EnumPropertyWidget<T extends Enum<T> & StringIdentifiable> extends EnumWidget<T>
{
    private BlockState block;
    private final EnumProperty<T> property;

    public EnumPropertyWidget(BlockState block, EnumProperty<T> property, int x, int y, int width, BiConsumer<ClickableWidget, ClickableWidget> addDropdown)
    {
        super(Text.literal(property.getName()), x, y, width, block.get(property), addDropdown);

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
        block = block.with(property, value);
    }
}
