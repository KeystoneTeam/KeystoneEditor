package keystone.api.tools.interfaces;

import keystone.api.BlockRegion;

/**
 * A tool which performs a function on every block within the current selection
 */
public interface IBlockTool extends IKeystoneTool
{
    /**
     * Ran for every block in the current selection. Be sure this code is
     * self-contained, as it will be ran on multiple threads, and as such
     * is subject to race conditions
     * @param x The x-coordinate of the block
     * @param y The y-coordinate of the block
     * @param z The z-coordinate of the block
     * @param region The {@link BlockRegion} the block is in
     */
    void process(int x, int y, int z, BlockRegion region);
    /**
     * @return If true, blocks that are in multiple selection boxes will only be processed once
     */
    default boolean ignoreRepeatBlocks() { return true; }
}
