package keystone.api.tools.interfaces;

import keystone.api.WorldRegion;
import keystone.api.wrappers.entities.Entity;

/**
 * A tool which performs a function on every block within the current selection
 */
public interface IEntityTool extends IKeystoneTool
{
    /**
     * Ran for every entity in the current selection. Be sure this code is
     * self-contained, as it will be ran on multiple threads, and as such
     * is subject to race conditions
     * @param entity The {@link Entity} to process
     * @param region The {@link WorldRegion} the entity is in
     */
    void process(Entity entity, WorldRegion region);
    /**
     * @return If true, entities that are in multiple selection boxes will only be processed once
     */
    default boolean ignoreRepeatBlocks() { return true; }
}
