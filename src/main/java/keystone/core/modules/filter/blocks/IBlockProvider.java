package keystone.core.modules.filter.blocks;

import keystone.api.wrappers.blocks.BlockType;
import keystone.core.gui.overlays.block_selection.BlockGridButton;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public interface IBlockProvider
{
    BlockType get();
    BlockType getFirst();
    void forEach(Consumer<BlockType> consumer);
    IBlockProvider clone();
    
    boolean containsState(BlockType blockType);
    boolean containsBlock(BlockType block);
    
    NbtCompound write();
    void read(NbtCompound nbt);
    
    Text getName();
    List<Text> getProperties();
    ItemStack getDisplayItem();
    void openEditPropertiesScreen(BlockGridButton gridButton, int mouseButton);
}