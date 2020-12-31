package keystone.api.tools;

import keystone.api.SelectionBox;
import keystone.api.tools.interfaces.IBlockTool;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class DeleteTool implements IBlockTool
{
    @Override
    public void process(BlockPos pos, SelectionBox box)
    {
        box.setBlock(pos, Blocks.AIR.getDefaultState());
    }
}
