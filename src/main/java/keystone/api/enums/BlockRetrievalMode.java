package keystone.api.enums;

import keystone.api.wrappers.blocks.Block;

public enum BlockRetrievalMode
{
    /**
     * Retrieve the {@link Block} that is currently in the Minecraft
     * world, ignoring the currently opened history entry completely
     */
    ORIGINAL,
    /**
     * Retrieve the {@link Block} that was last swapped via the
     * the swapBuffers or swapBlockBuffers method
     */
    LAST_SWAPPED,
    /**
     * Retrieve the {@link Block} that is the latest state in the currently
     * opened history entry
     */
    CURRENT
}
