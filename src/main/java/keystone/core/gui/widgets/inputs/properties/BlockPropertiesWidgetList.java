package keystone.core.gui.widgets.inputs.properties;

import keystone.api.wrappers.blocks.Block;
import keystone.core.gui.widgets.WidgetList;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BlockPropertiesWidgetList extends WidgetList
{
    protected final int intendedWidth;
    protected final Consumer<ClickableWidget[]> disableWidgets;
    protected final Runnable restoreWidgets;
    protected final BiConsumer<ClickableWidget, Boolean> addDropdown;

    public BlockPropertiesWidgetList(Block block, int x, int y, int width, int maxHeight, int padding, Consumer<ClickableWidget[]> disableWidgets, Runnable restoreWidgets)
    {
        super(x, y, width, maxHeight, padding, Text.translatable("keystone.block_properties.propertiesPanel"));

        this.intendedWidth = width - 2 * padding;
        this.disableWidgets = disableWidgets;
        this.restoreWidgets = restoreWidgets;
        this.addDropdown = this::add;

        int propertyY = 0;
        Collection<Property<?>> properties = block.blockType().getMinecraftBlock().getProperties();
        for (Property<?> property : properties)
        {
            propertyY += createVariableEditor(block, property, propertyY) + padding;
        }
    }

    private int createVariableEditor(Block block, Property<?> property, int y)
    {
        if (property instanceof BooleanProperty)
        {
            add(new BooleanPropertyWidget(block, (BooleanProperty)property, 0, y, intendedWidth));
            return BooleanPropertyWidget.getFinalHeight();
        }
        if (property instanceof IntProperty)
        {
            add(new IntPropertyWidget(block, (IntProperty)property, 0, y, intendedWidth));
            return IntPropertyWidget.getFinalHeight();
        }
        if (property instanceof EnumProperty)
        {
            add(new EnumPropertyWidget(block, (EnumProperty)property, 0, y, intendedWidth, disableWidgets, restoreWidgets, addDropdown));
            return EnumPropertyWidget.getFinalHeight();
        }

        return 0;
    }
}
