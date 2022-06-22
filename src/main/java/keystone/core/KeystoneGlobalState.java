package keystone.core;

import keystone.core.modules.world.WorldChangeQueueModule;

public class KeystoneGlobalState
{
    /**
     * If true, target the block a set distance in front of player. If false, target the block
     * the player is looking at, ignoring distance
     */
    public static boolean CloseSelection = false;
    /**
     * If {@link keystone.core.KeystoneGlobalState#CloseSelection} is true, this is the distance
     * in front of the player that will be targeted
     */
    public static double CloseSelectionDistance = 4.0;
    /**
     * Whether mouse movement should move the player camera
     */
    public static boolean AllowPlayerLook = false;

    /**
     * Whether the mouse is hovering over an interactable overlay widget
     */
    public static boolean MouseOverGUI = false;
    /**
     * If true, either a vanilla screen is open or a TextFieldWidget is focused. This will
     * disable any keybinds with the NO_GUI_OPEN condition and enable an keybinds with the
     * GUI_OPEN condition
     */
    public static boolean GuiConsumingKeys = false;
    /**
     * If true, all keybinds will be disabled. This is used to force the player to wait for
     * something, such as flushing the {@link WorldChangeQueueModule}
     */
    public static boolean BlockingKeys = false;

    /**
     * If true, all block ticks will be ignored. This is manually changed by the player for
     * things like ignoring block gravity
     */
    public static boolean SuppressingBlockTicks = false;

    /**
     * If true, selection boxes will not be rendered
     */
    public static boolean HideSelectionBoxes = false;
    /**
     * If true, tile entities will be highlighted with a yellow box
     */
    public static boolean HighlightTileEntities = false;
}
