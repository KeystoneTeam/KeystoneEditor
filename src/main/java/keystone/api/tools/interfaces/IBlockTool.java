package keystone.api.tools.interfaces;

import keystone.api.SelectionBox;
import net.minecraft.util.math.BlockPos;

/**
 * A tool which performs a function on every block within the current {@link keystone.api.SelectionBox SelectionBoxes}
 */
public interface IBlockTool extends IKeystoneTool
{
    /**
     * Ran for every block in the current {@link keystone.api.SelectionBox SelectionBoxes}. Be sure this
     * code is self-contained, as it will be ran on multiple threads, and as such is subject to race
     * conditions
     * @param pos The position of the block
     * @param box The {@link keystone.api.SelectionBox} the block is in
     */
    void process(BlockPos pos, SelectionBox box);
    /**
     * @return If true, blocks that are in multiple selection boxes will only be processed once
     */
    default boolean ignoreRepeatBlocks() { return true; }
}
