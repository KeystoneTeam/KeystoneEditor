package keystone.api.tools.interfaces;

import keystone.api.SelectionBox;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

public class FillTool implements IBlockTool
{
    private BlockState block;

    public FillTool(Item item)
    {
        if (item instanceof BlockItem) this.block = ((BlockItem) item).getBlock().getDefaultState();
        else this.block = Blocks.AIR.getDefaultState();
    }
    public FillTool(BlockState block)
    {
        this.block = block;
    }

    @Override
    public void process(BlockPos pos, SelectionBox box)
    {
        if (block != null) box.setBlock(pos, block);
    }
}
