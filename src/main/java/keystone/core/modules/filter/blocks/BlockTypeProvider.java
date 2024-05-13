package keystone.core.modules.filter.blocks;

import keystone.api.utils.StringUtils;
import keystone.api.wrappers.blocks.BlockType;
import keystone.core.gui.overlays.block_selection.BlockGridButton;
import keystone.core.gui.overlays.block_selection.BlockPropertiesScreen;
import keystone.core.gui.widgets.BlockGridWidget;
import keystone.core.registries.BlockTypeRegistry;
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

public class BlockTypeProvider implements IBlockProvider
{
    private BlockType blockType;
    
    private BlockTypeProvider()
    {
        this.blockType = null;
    }
    public BlockTypeProvider(BlockType blockType)
    {
        this.blockType = blockType;
    }

    @Override public BlockType get() { return blockType; }
    @Override public BlockType getFirst() { return blockType; }
    @Override public void forEach(Consumer<BlockType> consumer) { consumer.accept(blockType); }
    @Override public IBlockProvider clone() { return new BlockTypeProvider(blockType); }
    
    @Override public boolean containsState(BlockType blockType) { return this.blockType.equals(blockType); }
    @Override public boolean containsBlock(BlockType block) { return this.blockType.getMinecraftBlock().getBlock().equals(block.getMinecraftBlock().getBlock()); }
    
    @Override public NbtCompound write() { return NbtHelper.fromBlockState(blockType.getMinecraftBlock()); }
    @Override public void read(NbtCompound nbt) { blockType = BlockTypeRegistry.fromMinecraftBlock(NbtHelper.toBlockState(RegistryLookups.registryLookup(RegistryKeys.BLOCK), nbt)); }
    
    @Override public Text getName() { return this.blockType.getMinecraftBlock().getBlock().getName(); }
    @Override
    public List<Text> getProperties()
    {
        BlockState state = blockType.getMinecraftBlock();
        List<Text> properties = new ArrayList<>();
        state.getProperties().forEach(property ->
        {
            if (property instanceof BooleanProperty booleanProperty)
            {
                if (state.get(booleanProperty)) properties.addAll(Text.literal(StringUtils.snakeCaseToTitleCase(property.getName())).getWithStyle(Style.EMPTY.withColor(Formatting.GRAY)));
            }
            else properties.addAll(Text.literal(StringUtils.snakeCaseToTitleCase(property.getName()) + ": " + StringUtils.snakeCaseToTitleCase(state.get(property).toString())).getWithStyle(Style.EMPTY.withColor(Formatting.GRAY)));
        });
        return properties;
    }
    @Override
    public ItemStack getDisplayItem()
    {
        Item item = BlockUtils.getBlockItem(blockType.getMinecraftBlock().getBlock());
        return new ItemStack(item);
    }
    @Override
    public void openEditPropertiesScreen(BlockGridButton gridButton, int mouseButton)
    {
        BlockPropertiesScreen.editBlockProperties(BlockTypeRegistry.fromMinecraftBlock(blockType.getMinecraftBlock()), block ->
        {
            if (block != null) gridButton.getParent().onEntryClicked(new BlockGridWidget.Entry(block.getMinecraftBlock(), gridButton.getTooltipBuilder()), mouseButton);
            gridButton.getParent().restoreWidgets();
        });
    }
    
    @Override
    public int hashCode()
    {
        return blockType.hashCode();
    }
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockTypeProvider blockTypeProvider = (BlockTypeProvider) o;
        return blockType.equals(blockTypeProvider.get());
    }
}