package keystone.api.tools;

import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.wrappers.blocks.BlockPalette;
import keystone.api.wrappers.blocks.BlockType;
import keystone.core.registries.BlockTypeRegistry;
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
    public FillTool(BlockPalette palette)
    {
        this.palette = palette;
        setName("Fill");
    }
    /**
     * @param blockType The {@link BlockType} to fill
     */
    public FillTool(BlockType blockType)
    {
        this(new BlockPalette().with(blockType));
    }
    public FillTool(BlockState block)
    {
        this(new BlockPalette().with(BlockTypeRegistry.fromMinecraftBlock(block)));
    }


    @Override
    public void processBlock(int x, int y, int z, WorldRegion region)
    {
        region.setBlock(x, y, z, palette.randomBlock());
    }
}
