package keystone.core.filters;

import keystone.api.block.Block;
import net.minecraft.block.BlockState;

import java.util.HashMap;
import java.util.Map;

public class FilterCache
{
    private static final Map<BlockState, Block> blockCache = new HashMap<>();

    public static final void clear()
    {
        blockCache.clear();
    }

    public static final Block getBlock(BlockState state)
    {
        Block block = blockCache.get(state);
        if (block == null) block = new Block(state);
        return block;
    }
    public static final void setBlock(Block block)
    {
        blockCache.put(block.getMinecraftBlock(), block);
    }
}
