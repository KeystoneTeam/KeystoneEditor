package keystone.core.gui.widgets.inputs.properties;

import keystone.api.wrappers.blocks.Block;
import keystone.core.gui.widgets.inputs.EnumWidget;
import keystone.core.registries.BlockTypeRegistry;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EnumPropertyWidget<T extends Enum<T> & StringIdentifiable> extends EnumWidget<T>
{
    private final Block block;
    private final EnumProperty<T> property;

    public EnumPropertyWidget(Block block, EnumProperty<T> property, int x, int y, int width, Consumer<ClickableWidget[]> disableWidgets, Runnable restoreWidgets, BiConsumer<ClickableWidget, Boolean> addDropdown)
    {
        super(Text.literal(property.getName()), x, y, width, block.blockType().getMinecraftBlock().get(property), disableWidgets, restoreWidgets, addDropdown);

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
        block.setBlockType(BlockTypeRegistry.fromMinecraftBlock(block.blockType().getMinecraftBlock().with(property, value)));
    }
}
