package keystone.api.enums;

public enum BlockRetrievalMode
{
    /**
     * Retrieve the {@link keystone.api.wrappers.Block} that is currently in the Minecraft
     * world, ignoring the currently opened history entry completely
     */
    ORIGINAL,
    /**
     * Retrieve the {@link keystone.api.wrappers.Block} that was last swapped via the
     * the swapBuffers or swapBlockBuffers method
     */
    LAST_SWAPPED,
    /**
     * Retrieve the {@link keystone.api.wrappers.Block} that is the latest state in the currently
     * opened history entry
     */
    CURRENT
}
