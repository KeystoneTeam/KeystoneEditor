package keystone.core.gui.widgets.inputs.properties;

import keystone.api.wrappers.blocks.Block;
import keystone.core.gui.widgets.WidgetList;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.Property;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BlockPropertiesWidgetList extends WidgetList
{
    protected final int intendedWidth;
    protected final Consumer<Widget[]> disableWidgets;
    protected final Runnable restoreWidgets;
    protected final BiConsumer<Widget, Boolean> addDropdown;

    public BlockPropertiesWidgetList(Block block, int x, int y, int width, int maxHeight, int padding, Consumer<Widget[]> disableWidgets, Runnable restoreWidgets)
    {
        super(x, y, width, maxHeight, padding, new TranslationTextComponent("keystone.block_properties.propertiesPanel"));

        this.intendedWidth = width - 2 * padding;
        this.disableWidgets = disableWidgets;
        this.restoreWidgets = restoreWidgets;
        this.addDropdown = this::add;

        int propertyY = 0;
        Collection<Property<?>> properties = block.getMinecraftBlock().getProperties();
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
        if (property instanceof IntegerProperty)
        {
            add(new IntegerPropertyWidget(block, (IntegerProperty)property, 0, y, intendedWidth));
            return IntegerPropertyWidget.getFinalHeight();
        }
        if (property instanceof EnumProperty)
        {
            add(new EnumPropertyWidget(block, (EnumProperty)property, 0, y, intendedWidth, disableWidgets, restoreWidgets, addDropdown));
            return EnumPropertyWidget.getFinalHeight();
        }

        return 0;
    }
}
