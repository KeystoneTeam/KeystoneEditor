package keystone.api.tools;

import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.wrappers.blocks.BlockMask;
import keystone.api.wrappers.blocks.BlockPalette;
import keystone.api.wrappers.blocks.BlockType;
import keystone.core.registries.BlockTypeRegistry;
import net.minecraft.block.BlockState;

public class FillTool extends KeystoneFilter
{
    private BlockMask mask;
    private BlockPalette palette;

    public FillTool(BlockMask mask, BlockPalette palette)
    {
        this.mask = mask;
        this.palette = palette;
        setName("Fill");
    }

    public FillTool(BlockType blockType)
    {
        this(new BlockMask().blacklist(), new BlockPalette().with(blockType));
    }
    public FillTool(BlockState block)
    {
        this(new BlockMask().blacklist(), new BlockPalette().with(BlockTypeRegistry.fromMinecraftBlock(block)));
    }


    @Override
    public void processBlock(int x, int y, int z, WorldRegion region)
    {
        if (mask.valid(region.getBlockType(x, y, z))) region.setBlock(x, y, z, palette);
    }
}
