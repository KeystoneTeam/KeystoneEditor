package keystone.api.tools.interfaces;

import keystone.api.SelectionBox;
import net.minecraft.util.math.BlockPos;

/*
Performs a function on every block within the selection boxes
 */
public interface IBlockTool extends IKeystoneTool
{
    void process(BlockPos pos, SelectionBox box);
    /*
    If true, blocks that are in multiple selection boxes will only be processed once
    */
    default boolean ignoreRepeatBlocks() { return true; }
}
