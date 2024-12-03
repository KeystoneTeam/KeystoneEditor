package keystone.core.keybinds;

import keystone.api.Keystone;
import keystone.core.KeystoneConfig;
import keystone.core.keybinds.conflicts.DefaultKeyConditions;
import keystone.core.keybinds.conflicts.IKeyCondition;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class KeystoneKeyBindings
{
    public static final KeyBinding TOGGLE_KEYSTONE = new KeyBinding("keystone.key.toggleKeystone", GLFW.GLFW_KEY_K, "key.categories.keystone");
    public static final KeyBinding INCREASE_FLY_SPEED = new KeyBinding("keystone.key.fly_speed.increase", GLFW.GLFW_KEY_UP, "key.categories.keystone");
    public static final KeyBinding DECREASE_FLY_SPEED = new KeyBinding("keystone.key.fly_speed.decrease", GLFW.GLFW_KEY_DOWN, "key.categories.keystone");

    private static boolean addedConditions = false;
    private static Map<KeyBinding, IKeyCondition[]> conditions = new HashMap<>();

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
    
    public static void configureKeyConditions(KeyBinding keyBinding, IKeyCondition... conditions)
    {
        KeyBindingUtils.clearConditions(keyBinding);
        KeyBindingUtils.addConditions(keyBinding, conditions);
        KeystoneKeyBindings.conditions.put(keyBinding, conditions);
    }
    public static void configureKeyConditions()
    {
        if (addedConditions) return;
        else addedConditions = true;

        GameOptions options = MinecraftClient.getInstance().options;
        
        // Register Keystone Conditions
        configureKeyConditions(TOGGLE_KEYSTONE, DefaultKeyConditions.NO_GUI_OPEN);
        configureKeyConditions(INCREASE_FLY_SPEED, DefaultKeyConditions.NO_GUI_OPEN, DefaultKeyConditions.KEYSTONE_ACTIVE);
        configureKeyConditions(DECREASE_FLY_SPEED, DefaultKeyConditions.NO_GUI_OPEN, DefaultKeyConditions.KEYSTONE_ACTIVE);
        
        // Register Movement Conditions
        IKeyCondition[] movementConditions = { DefaultKeyConditions.NO_GUI_OPEN };
        configureKeyConditions(options.forwardKey, movementConditions);
        configureKeyConditions(options.leftKey, movementConditions);
        configureKeyConditions(options.backKey, movementConditions);
        configureKeyConditions(options.rightKey, movementConditions);
        configureKeyConditions(options.jumpKey, movementConditions);
        configureKeyConditions(options.sneakKey, movementConditions);
        
        // Register Default Conditions
        IKeyCondition[] defaultConditions = { DefaultKeyConditions.NO_GUI_OPEN, DefaultKeyConditions.KEYSTONE_INACTIVE };
        for (KeyBinding keyBinding : options.allKeys) if (!conditions.containsKey(keyBinding)) configureKeyConditions(keyBinding, defaultConditions);
    }
}
