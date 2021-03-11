package keystone.core.gui.widgets.inputs.properties;

import keystone.api.wrappers.Block;
import keystone.core.gui.widgets.WidgetList;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.Property;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BlockPropertiesWidgetList extends WidgetList
{
    protected final int intendedWidth;
    protected final Consumer<Widget[]> disableWidgets;
    protected final Runnable restoreWidgets;
    protected final BiConsumer<Widget, Boolean> addDropdown;

    public BlockPropertiesWidgetList(Block block, int padding, int width, Consumer<Widget[]> disableWidgets, Runnable restoreWidgets)
    {
        this.intendedWidth = width;
        this.disableWidgets = disableWidgets;
        this.restoreWidgets = restoreWidgets;
        this.addDropdown = this::add;

        int y = 0;
        Collection<Property<?>> properties = block.getMinecraftBlock().getProperties();
        for (Property<?> property : properties)
        {
            y += createVariableEditor(block, property, y) + padding;
        }
    }

    @Override
    public void bake()
    {
        super.bake();
        height += y;
        y = 0;
    }

    private int createVariableEditor(Block block, Property<?> property, int y)
    {
        if (property instanceof BooleanProperty)
        {
            add(new BooleanPropertyWidget(block, (BooleanProperty)property, 0, y, intendedWidth));
            return BooleanPropertyWidget.getHeight();
        }
        if (property instanceof IntegerProperty)
        {
            add(new IntegerPropertyWidget(block, (IntegerProperty)property, 0, y, intendedWidth));
            return IntegerPropertyWidget.getHeight();
        }
        if (property instanceof EnumProperty)
        {
            add(new EnumPropertyWidget(block, (EnumProperty)property, 0, y, intendedWidth, disableWidgets, restoreWidgets, addDropdown));
            return EnumPropertyWidget.getHeight();
        }

        return 0;
    }
}
