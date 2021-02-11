package keystone.core;

public class KeystoneStateFlags
{
    /**
     * If true, target the block 4 blocks in front of player. If false, target the block
     * the player is looking at, ignoring distance
     */
    public static boolean CloseSelection = false;
    /**
     * Whether mouse movement should move the player camera
     */
    public static boolean AllowPlayerLook = false;
    /**
     * Whether the mouse is hovering over an interactable overlay widget
     */
    public static boolean MouseOverGUI = false;
    /**
     * If true, most keybinds will be disabled, such as movement and menu hotkeys
     */
    public static boolean BlockingKeys = false;
    /**
     * If true, selection boxes will not be rendered
     */
    public static boolean HideSelectionBoxes = false;
}
