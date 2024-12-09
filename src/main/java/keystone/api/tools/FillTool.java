package keystone.api.tools;

import keystone.api.BlockMask;
import keystone.api.BlockPalette;
import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.wrappers.BlockType;
import keystone.core.modules.filter.blocks.IBlockProvider;
import net.minecraft.block.BlockState;

public class FillTool extends KeystoneFilter
{
    private final BlockMask mask;
    private final BlockPalette palette;

    public FillTool(BlockMask mask, BlockPalette palette)
    {
        this.mask = mask;
        this.palette = palette;
        setName("Fill");
    }
    
    public FillTool(IBlockProvider blockProvider)
    {
        this(new BlockMask().blacklist(), new BlockPalette().with(blockProvider));
    }
    public FillTool(BlockState block)
    {
        this(new BlockMask().blacklist(), new BlockPalette().with(block));
    }
    public FillTool(BlockType block)
    {
        this(new BlockMask().blacklist(), new BlockPalette().with(block));
    }
    @Override
    public void processBlock(int x, int y, int z, WorldRegion region)
    {
        if (mask.valid(region.getBlockType(x, y, z))) region.setBlockType(x, y, z, palette);
    }
}
