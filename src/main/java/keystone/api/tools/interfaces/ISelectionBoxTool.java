package keystone.api.tools.interfaces;

import keystone.api.WorldRegion;

/**
 * A tool which performs a function on every {@link WorldRegion} in the current
 * selection
 */
public interface ISelectionBoxTool extends IKeystoneTool
{
    /**
     * Ran for every {@link WorldRegion} in the current selection
     * @param box The {@link WorldRegion} that is being processed
     */
    void process(WorldRegion box);
}
