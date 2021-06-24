package keystone.core.gui.widgets.inputs.properties;

import keystone.api.wrappers.Block;
import keystone.core.gui.widgets.inputs.EnumWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.state.EnumProperty;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.text.StringTextComponent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EnumPropertyWidget<T extends Enum<T> & IStringSerializable> extends EnumWidget<T>
{
    private final Block block;
    private final EnumProperty<T> property;

    public EnumPropertyWidget(Block block, EnumProperty<T> property, int x, int y, int width, Consumer<Widget[]> disableWidgets, Runnable restoreWidgets, BiConsumer<Widget, Boolean> addDropdown)
    {
        super(new StringTextComponent(property.getName()), x, y, width, block.getMinecraftBlock().getValue(property), disableWidgets, restoreWidgets, addDropdown);

        this.block = block;
        this.property = property;

        build();
    }

    @Override
    protected boolean autoBuild() { return false; }
    @Override
    protected boolean isValueAllowed(T value) { return property.getPossibleValues().contains(value); }
    @Override
    protected void onSetValue(T value)
    {
        block.setMinecraftBlock(block.getMinecraftBlock().setValue(property, value));
    }
}
