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
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class KeystoneKeyBindings
{
    public static final KeyBinding TOGGLE_KEYSTONE = new KeyBinding("keystone.key.toggleKeystone", GLFW.GLFW_KEY_K, "key.categories.keystone");
    public static final KeyBinding INCREASE_FLY_SPEED = new KeyBinding("keystone.key.fly_speed.increase", GLFW.GLFW_KEY_UP, "key.categories.keystone");
    public static final KeyBinding DECREASE_FLY_SPEED = new KeyBinding("keystone.key.fly_speed.decrease", GLFW.GLFW_KEY_DOWN, "key.categories.keystone");

    private static boolean addedConditions = false;

    public static void register()
    {
        KeyBindingHelper.registerKeyBinding(TOGGLE_KEYSTONE);
        KeyBindingHelper.registerKeyBinding(INCREASE_FLY_SPEED);
        KeyBindingHelper.registerKeyBinding(DECREASE_FLY_SPEED);

        ClientTickEvents.END_CLIENT_TICK.register(client ->
        {
            while (TOGGLE_KEYSTONE.wasPressed()) Keystone.toggleKeystone();
            
            if (Keystone.isActive())
            {
                while (INCREASE_FLY_SPEED.wasPressed()) Keystone.increaseFlySpeed(KeystoneConfig.flySpeedChangeAmount);
                while (DECREASE_FLY_SPEED.wasPressed()) Keystone.decreaseFlySpeed(KeystoneConfig.flySpeedChangeAmount);
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
