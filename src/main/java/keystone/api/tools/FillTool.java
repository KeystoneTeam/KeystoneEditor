package keystone.api.tools;

import keystone.api.SelectionBox;
import keystone.api.tools.interfaces.IBlockTool;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

public class FillTool implements IBlockTool
{
    private BlockState block;

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
