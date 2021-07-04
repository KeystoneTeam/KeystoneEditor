package keystone.api.tools;

import keystone.api.WorldRegion;
import keystone.api.tools.interfaces.IBlockTool;
import keystone.api.wrappers.blocks.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;

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
     * @param block The {@link Block} to fill
     * @param tileEntity The {@link net.minecraft.tileentity.TileEntity} to fill
     */
    public FillTool(BlockState block, TileEntity tileEntity) { this(new Block(block, tileEntity)); }
    /**
     * @param block The {@link Block} to fill
     */
    public FillTool(Block block)
    {
        this.block = block;
    }

    @Override
    public void process(int x, int y, int z, WorldRegion region)
    {
        if (block != null) region.setBlock(x, y, z, block);
    }
}
