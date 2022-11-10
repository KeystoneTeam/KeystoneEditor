package keystone.core.keybinds;

import keystone.api.Keystone;
import keystone.api.tools.DeleteEntitiesTool;
import keystone.api.tools.FillTool;
import keystone.core.KeystoneConfig;
import keystone.core.KeystoneGlobalState;
import keystone.core.gui.screens.KeystoneOptionsScreen;
import keystone.core.keybinds.conflicts.DefaultKeyConditions;
import keystone.core.keybinds.conflicts.IKeyCondition;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class KeystoneKeyBindings
{
    public static final KeyBinding TOGGLE_KEYSTONE = new KeyBinding("keystone.key.toggleKeystone", GLFW.GLFW_KEY_K, "key.categories.keystone");
    public static final KeyBinding INCREASE_FLY_SPEED = new KeyBinding("keystone.key.fly_speed.increase", GLFW.GLFW_KEY_UP, "key.categories.keystone");
    public static final KeyBinding DECREASE_FLY_SPEED = new KeyBinding("keystone.key.fly_speed.decrease", GLFW.GLFW_KEY_DOWN, "key.categories.keystone");
    public static final KeyBinding DELETE_SELECTION = new KeyBinding("keystone.key.deleteSelection", GLFW.GLFW_KEY_DELETE, "key.categories.keystone");
    public static final KeyBinding TOGGLE_UPDATES = new KeyBinding("keystone.key.toggleUpdates", GLFW.GLFW_KEY_U, "key.categories.keystone");
    public static final KeyBinding HIGHLIGHT_TILE_ENTITIES = new KeyBinding("keystone.key.highlightTileEntities", GLFW.GLFW_KEY_H, "key.categories.keystone");
    public static final KeyBinding FEATURE_TEST = new KeyBinding("keystone.key.featureTest", GLFW.GLFW_KEY_O, "key.categories.keystone");

    private static boolean addedConditions = false;

    public static void register()
    {
        KeyBindingHelper.registerKeyBinding(TOGGLE_KEYSTONE);
        KeyBindingHelper.registerKeyBinding(INCREASE_FLY_SPEED);
        KeyBindingHelper.registerKeyBinding(DECREASE_FLY_SPEED);
        KeyBindingHelper.registerKeyBinding(DELETE_SELECTION);
        KeyBindingHelper.registerKeyBinding(TOGGLE_UPDATES);
        KeyBindingHelper.registerKeyBinding(HIGHLIGHT_TILE_ENTITIES);
        KeyBindingHelper.registerKeyBinding(FEATURE_TEST);

        ClientTickEvents.END_CLIENT_TICK.register(client ->
        {
            while (TOGGLE_KEYSTONE.wasPressed()) Keystone.toggleKeystone();
            
            if (Keystone.isActive())
            {
                while (INCREASE_FLY_SPEED.wasPressed()) Keystone.increaseFlySpeed(KeystoneConfig.flySpeedChangeAmount);
                while (DECREASE_FLY_SPEED.wasPressed()) Keystone.decreaseFlySpeed(KeystoneConfig.flySpeedChangeAmount);
                while (DELETE_SELECTION.wasPressed()) Keystone.runInternalFilters(new FillTool(Blocks.AIR.getDefaultState()), new DeleteEntitiesTool());
                while (TOGGLE_UPDATES.wasPressed()) KeystoneGlobalState.SuppressingBlockTicks = !KeystoneGlobalState.SuppressingBlockTicks;
                while (HIGHLIGHT_TILE_ENTITIES.wasPressed()) KeystoneGlobalState.HighlightTileEntities = !KeystoneGlobalState.HighlightTileEntities;
                
                while (FEATURE_TEST.wasPressed())
                {
                    MinecraftClient.getInstance().setScreenAndRender(new KeystoneOptionsScreen(null));
                }
            }
        });
    }
    public static void configureKeyConditions()
    {
        if (addedConditions) return;
        else addedConditions = true;

        GameOptions options = MinecraftClient.getInstance().options;
        IKeyCondition noGuiOpen = DefaultKeyConditions.NO_GUI_OPEN;
        IKeyCondition keystoneInactive = DefaultKeyConditions.KEYSTONE_INACTIVE;
        IKeyCondition keystoneActive = DefaultKeyConditions.KEYSTONE_ACTIVE;

        KeyBindingUtils.addConditions(TOGGLE_KEYSTONE, noGuiOpen);
        KeyBindingUtils.addConditions(INCREASE_FLY_SPEED, keystoneActive, noGuiOpen);
        KeyBindingUtils.addConditions(DECREASE_FLY_SPEED, keystoneActive, noGuiOpen);
        KeyBindingUtils.addConditions(DELETE_SELECTION, keystoneActive, noGuiOpen);
        KeyBindingUtils.addConditions(TOGGLE_UPDATES, keystoneActive, noGuiOpen);
        KeyBindingUtils.addConditions(HIGHLIGHT_TILE_ENTITIES, keystoneActive, noGuiOpen);
        KeyBindingUtils.addConditions(FEATURE_TEST, keystoneActive, noGuiOpen);

        KeyBindingUtils.addConditions(options.forwardKey, noGuiOpen);
        KeyBindingUtils.addConditions(options.leftKey, noGuiOpen);
        KeyBindingUtils.addConditions(options.backKey, noGuiOpen);
        KeyBindingUtils.addConditions(options.rightKey, noGuiOpen);
        KeyBindingUtils.addConditions(options.jumpKey, noGuiOpen);
        KeyBindingUtils.addConditions(options.sneakKey, noGuiOpen);

        KeyBindingUtils.addConditions(options.sprintKey, noGuiOpen, keystoneInactive);
        KeyBindingUtils.addConditions(options.attackKey, noGuiOpen, keystoneInactive);
        KeyBindingUtils.addConditions(options.chatKey, noGuiOpen, keystoneInactive);
        KeyBindingUtils.addConditions(options.playerListKey, noGuiOpen, keystoneInactive);
        KeyBindingUtils.addConditions(options.commandKey, noGuiOpen, keystoneInactive);
        KeyBindingUtils.addConditions(options.togglePerspectiveKey, noGuiOpen, keystoneInactive);
        KeyBindingUtils.addConditions(options.smoothCameraKey, noGuiOpen, keystoneInactive);
        KeyBindingUtils.addConditions(options.advancementsKey, noGuiOpen, keystoneInactive);
        KeyBindingUtils.addConditions(options.dropKey, noGuiOpen, keystoneInactive);
        KeyBindingUtils.addConditions(options.inventoryKey, noGuiOpen, keystoneInactive);
        KeyBindingUtils.addConditions(options.loadToolbarActivatorKey, noGuiOpen, keystoneInactive);
        KeyBindingUtils.addConditions(options.pickItemKey, noGuiOpen, keystoneInactive);
        KeyBindingUtils.addConditions(options.saveToolbarActivatorKey, noGuiOpen, keystoneInactive);
        KeyBindingUtils.addConditions(options.swapHandsKey, noGuiOpen, keystoneInactive);
        KeyBindingUtils.addConditions(options.useKey, noGuiOpen, keystoneInactive);
        for (KeyBinding keyBinding : options.hotbarKeys) KeyBindingUtils.addConditions(keyBinding, noGuiOpen, keystoneInactive);
    }
}
