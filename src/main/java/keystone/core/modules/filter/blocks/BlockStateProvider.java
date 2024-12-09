package keystone.core.modules.filter.blocks;

import keystone.api.utils.StringUtils;
import keystone.core.gui.overlays.block_selection.BlockGridButton;
import keystone.core.gui.overlays.block_selection.BlockPropertiesScreen;
import keystone.core.gui.widgets.BlockGridWidget;
import keystone.core.utils.BlockUtils;
import keystone.core.utils.RegistryLookups;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BlockStateProvider implements IBlockProvider
{
    private BlockState blockState;
    
    private BlockStateProvider()
    {
        this.blockState = null;
    }
    public BlockStateProvider(BlockState blockState)
    {
        this.blockState = blockState;
    }

    @Override public int size() { return 1; }
    @Override public BlockState get() { return blockState; }
    @Override public BlockState getFirst() { return blockState; }
    @Override public void forEach(Consumer<BlockState> consumer) { consumer.accept(blockState); }
    @Override public IBlockProvider clone() { return new BlockStateProvider(blockState); }
    
    @Override public boolean containsState(BlockState blockType) { return this.blockState.equals(blockType); }
    @Override public boolean containsBlock(BlockState block) { return this.blockState.getBlock().equals(block.getBlock()); }
    
    @Override public NbtCompound write() { return NbtHelper.fromBlockState(blockState); }
    @Override public void read(NbtCompound nbt) { blockState = NbtHelper.toBlockState(RegistryLookups.registryLookup(RegistryKeys.BLOCK), nbt); }
    
    @Override public Text getName() { return this.blockState.getBlock().getName(); }
    @Override
    public List<Text> getProperties()
    {;
        List<Text> properties = new ArrayList<>();
        blockState.getProperties().forEach(property ->
        {
            if (property instanceof BooleanProperty booleanProperty)
            {
                if (blockState.get(booleanProperty)) properties.addAll(Text.literal(StringUtils.snakeCaseToTitleCase(property.getName())).getWithStyle(Style.EMPTY.withColor(Formatting.GRAY)));
            }
            else properties.addAll(Text.literal(StringUtils.snakeCaseToTitleCase(property.getName()) + ": " + StringUtils.snakeCaseToTitleCase(blockState.get(property).toString())).getWithStyle(Style.EMPTY.withColor(Formatting.GRAY)));
        });
        return properties;
    }
    @Override
    public ItemStack getDisplayItem()
    {
        Item item = BlockUtils.getBlockItem(blockState.getBlock());
        return new ItemStack(item);
    }
    @Override
    public void openEditPropertiesScreen(BlockGridButton gridButton, int mouseButton)
    {
        BlockPropertiesScreen.editBlockProperties(blockState, block ->
        {
            if (block != null) gridButton.getParent().onEntryClicked(new BlockGridWidget.Entry(block, gridButton.getTooltipBuilder()), mouseButton);
            gridButton.getParent().restoreWidgets();
        });
    }
    
    @Override
    public int hashCode()
    {
        return blockState.hashCode();
    }
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockStateProvider blockStateProvider = (BlockStateProvider) o;
        return blockState.equals(blockStateProvider.get());
    }
}