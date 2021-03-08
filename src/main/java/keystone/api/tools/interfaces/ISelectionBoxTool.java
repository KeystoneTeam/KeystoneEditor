package keystone.api.tools.interfaces;

import keystone.api.BlockRegion;

/**
 * A tool which performs a function on every {@link BlockRegion} in the current
 * selection
 */
public interface ISelectionBoxTool extends IKeystoneTool
{
    /**
     * Ran for every {@link BlockRegion} in the current selection
     * @param box The {@link BlockRegion} that is being processed
     */
    void process(BlockRegion box);
}
