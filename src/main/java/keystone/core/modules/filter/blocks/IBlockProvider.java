package keystone.core.modules.filter.blocks;

import keystone.core.gui.overlays.block_selection.BlockGridButton;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public interface IBlockProvider
{
    int size();
    
    BlockState get();
    BlockState getFirst();
    void forEach(Consumer<BlockState> consumer);
    IBlockProvider clone();
    
    boolean containsState(BlockState blockType);
    boolean containsBlock(BlockState block);
    
    NbtCompound write();
    void read(NbtCompound nbt);
    
    Text getName();
    List<Text> getProperties();
    ItemStack getDisplayItem();
    void openEditPropertiesScreen(BlockGridButton gridButton, int mouseButton);
}