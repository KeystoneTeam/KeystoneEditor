package keystone.core;

import keystone.core.modules.world.change_queue.WorldChangeQueueModule;

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
     * If true, either a vanilla screen is open or a TextFieldWidget is focused. This will
     * disable any keybinds with the NO_GUI_OPEN condition and enable an keybinds with the
     * GUI_OPEN condition
     */
    public static boolean GuiConsumingKeys = false;
    /**
     * If true, Keystone is waiting for the {@link WorldChangeQueueModule} to finish applying
     * world changes. While true, all keybinds are disabled and no tick handlers will be run
     */
    public static boolean WaitingForChangeQueue = false;
    /**
     * If true, all block ticks will be ignored. This is manually changed by the player for
     * things like ignoring block gravity
     */
    public static boolean SuppressingBlockTicks = false;
    /**
     * If true, block states will not check if they can be placed at a location before being
     * placed in the world
     */
    public static boolean SuppressPlacementChecks = false;

    /**
     * If true, selection boxes will not be rendered
     */
    public static boolean HideSelectionBoxes = false;
    /**
     * If true, the Minecraft world renderer will be reloaded. This is used to
     * redraw biomes
     */
    public static boolean ReloadWorldRenderer = false;
}
