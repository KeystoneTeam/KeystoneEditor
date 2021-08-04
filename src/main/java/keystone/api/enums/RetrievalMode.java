package keystone.api.enums;

public enum RetrievalMode
{
    /**
     * Retrieve the content that is currently in the Minecraft
     * world, ignoring the currently opened history entry completely
     */
    ORIGINAL,
    /**
     * Retrieve the content that was last swapped via swapBuffers
     */
    LAST_SWAPPED,
    /**
     * Retrieve the content that is the latest state in the currently
     * opened history entry
     */
    CURRENT
}
