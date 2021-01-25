package keystone.api.filters;

import keystone.api.SelectionBox;
import keystone.api.tools.interfaces.IBlockTool;
import keystone.api.tools.interfaces.ISelectionBoxTool;
import net.minecraft.util.math.BlockPos;

public class KeystoneFilter implements ISelectionBoxTool, IBlockTool
{
    public void processBox(SelectionBox box) {}
    public void processBlock(int x, int y, int z, SelectionBox box)  {}

    @Override
    public final void process(SelectionBox box)
    {
        processBox(box);
    }
    @Override
    public final void process(BlockPos pos, SelectionBox box)
    {
        processBlock(pos.getX(), pos.getY(), pos.getZ(), box);
    }
}
