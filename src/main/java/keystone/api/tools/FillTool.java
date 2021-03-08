package keystone.api.tools;

import keystone.api.BlockRegion;
import keystone.api.tools.interfaces.IBlockTool;
import keystone.api.wrappers.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

/**
 * An {@link keystone.api.tools.interfaces.IBlockTool} which sets every block to a given block state
 */
public class FillTool implements IBlockTool
{
    private Block block;

    /**
     * @param block The block state to fill
     */
    public FillTool(BlockState block) { this(new Block(block)); }
    /**
     * @param block The {@link keystone.api.wrappers.Block} to fill
     * @param tileEntity The {@link net.minecraft.tileentity.TileEntity} to fill
     */
    public FillTool(BlockState block, TileEntity tileEntity) { this(new Block(block, tileEntity)); }
    /**
     * @param block The {@link keystone.api.wrappers.Block} to fill
     */
    public FillTool(Block block)
    {
        this.block = block;
    }

    @Override
    public void process(int x, int y, int z, BlockRegion region)
    {
        if (block != null) region.setBlock(x, y, z, block);
    }
}
