package keystone.api.tools;

import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.blocks.BlockPalette;
import net.minecraft.block.BlockState;

/**
 * A {@link KeystoneFilter} which sets every block to a given block state
 */
public class FillTool extends KeystoneFilter
{
    private BlockPalette palette;

    /**
     * @param palette The block palette to fill
     */
    public FillTool(BlockPalette palette) { this.palette = palette; }
    /**
     * @param block The {@link Block} to fill
     */
    public FillTool(Block block)
    {
        this(new BlockPalette().with(block));
    }
    public FillTool(BlockState block)
    {
        this(new BlockPalette().with(new Block(block)));
    }

    @Override
    public void processBlock(int x, int y, int z, WorldRegion region)
    {
        region.setBlock(x, y, z, palette.randomBlock());
    }
}
