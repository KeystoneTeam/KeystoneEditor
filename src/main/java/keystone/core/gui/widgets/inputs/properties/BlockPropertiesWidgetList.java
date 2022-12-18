package keystone.core.gui.widgets.inputs.properties;

import keystone.api.wrappers.blocks.Block;
import keystone.core.gui.widgets.groups.VerticalLayoutGroup;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.function.BiConsumer;

public class BlockPropertiesWidgetList extends VerticalLayoutGroup
{
    protected final int intendedWidth;
    protected final BiConsumer<ClickableWidget, ClickableWidget> addDropdown;

    public BlockPropertiesWidgetList(Block block, int x, int y, int width, int maxHeight, int padding)
    {
        super(x, y, width, maxHeight, padding, Text.translatable("keystone.block_selection_panel.propertiesPanel"));

        this.intendedWidth = width - 2 * padding;
        this.addDropdown = this::addPinnedWidget;

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
            add(new EnumPropertyWidget(block, (EnumProperty)property, 0, y, intendedWidth, addDropdown));
            return EnumPropertyWidget.getFinalHeight();
        }

        return 0;
    }
}
