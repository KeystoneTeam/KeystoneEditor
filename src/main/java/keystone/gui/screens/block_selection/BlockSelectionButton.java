package keystone.gui.screens.block_selection;

import keystone.gui.screens.types.AbstractBlockSelectionScreen;
import keystone.gui.screens.widgets.AbstractBlockButton;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class BlockSelectionButton extends AbstractBlockButton
{
    public static final int SIZE = 18;
    protected final AbstractBlockSelectionScreen parent;

    protected BlockSelectionButton(AbstractBlockSelectionScreen parent, ItemStack itemStack, Block block, int x, int y)
    {
        super(itemStack, block, x, y, SIZE, SIZE);
        this.parent = parent;
    }
    public static BlockSelectionButton create(AbstractBlockSelectionScreen parent, Block block, int x, int y)
    {
        Item item = AbstractBlockButton.getBlockItem(block, parent.getItemRegistry());
        if (item == null) return null;
        else return new BlockSelectionButton(parent, new ItemStack(item), block, x, y);
    }

    @Override
    protected void onClicked()
    {
        parent.onBlockSelected(getBlockState());
    }
}
