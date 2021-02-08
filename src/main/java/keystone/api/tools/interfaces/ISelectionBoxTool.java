package keystone.api.tools.interfaces;

import keystone.api.SelectionBox;

/**
 * A tool which performs a function on every {@link keystone.api.SelectionBox} in the current
 * {@link keystone.api.SelectionBox SelectionBoxes}
 */
public interface ISelectionBoxTool extends IKeystoneTool
{
    /**
     * Ran for every {@link keystone.api.SelectionBox} in the current {@link keystone.api.SelectionBox SelectionBoxes}
     * @param box The {@link keystone.api.SelectionBox} that is being processed
     */
    void process(SelectionBox box);
}
