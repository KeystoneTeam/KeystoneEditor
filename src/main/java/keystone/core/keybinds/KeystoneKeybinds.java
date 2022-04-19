package keystone.core.keybinds;

import keystone.api.Keystone;
import keystone.api.tools.FillTool;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.Blocks;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class KeystoneKeybinds
{
    public static final KeyBinding TOGGLE_KEYSTONE = new KeyBinding("key.toggle_keystone", GLFW.GLFW_KEY_K, "key.categories.keystone");
    public static final KeyBinding DELETE_BLOCKS = new KeyBinding("key.delete_blocks", GLFW.GLFW_KEY_DELETE, "key.categories.keystone");

    public static void register()
    {
        KeyBindingHelper.registerKeyBinding(TOGGLE_KEYSTONE);
        KeyBindingHelper.registerKeyBinding(DELETE_BLOCKS);

        ClientTickEvents.END_CLIENT_TICK.register(client ->
        {
            while (TOGGLE_KEYSTONE.wasPressed()) Keystone.toggleKeystone();
            while (DELETE_BLOCKS.wasPressed()) if (Keystone.isActive()) Keystone.runInternalFilter(new FillTool(Blocks.AIR.getDefaultState()));
        });
    }

    //public static void register()
    //{
    //    MinecraftClient mc = MinecraftClient.getInstance();
    //    IKeyConflictContext notGuiBlocking = KeystoneKeyConflictContext.NOT_GUI_BLOCKING;
//
    //    TOGGLE_KEYSTONE.conf(notGuiBlocking);
    //    DELETE_BLOCKS.setKeyConflictContext(notGuiBlocking);
//
    //    ClientRegistry.registerKeyBinding(TOGGLE_KEYSTONE);
    //    ClientRegistry.registerKeyBinding(DELETE_BLOCKS);
//
    //    mc.options.forwardKey.setKeyConflictContext(notGuiBlocking);
    //    mc.options.leftKey.setKeyConflictContext(notGuiBlocking);
    //    mc.options.backKey.setKeyConflictContext(notGuiBlocking);
    //    mc.options.rightKey.setKeyConflictContext(notGuiBlocking);
    //    mc.options.jumpKey.setKeyConflictContext(notGuiBlocking);
    //    mc.options.sneakKey.setKeyConflictContext(notGuiBlocking);
    //    mc.options.keySprint.setKeyConflictContext(notGuiBlocking);
    //    mc.options.keyAttack.setKeyConflictContext(notGuiBlocking);
    //    mc.options.keyChat.setKeyConflictContext(notGuiBlocking);
    //    mc.options.keyPlayerList.setKeyConflictContext(notGuiBlocking);
    //    mc.options.keyCommand.setKeyConflictContext(notGuiBlocking);
    //    mc.options.keyTogglePerspective.setKeyConflictContext(notGuiBlocking);
    //    mc.options.keySmoothCamera.setKeyConflictContext(notGuiBlocking);
//
    //    mc.options.keyAdvancements.setKeyConflictContext(notGuiBlocking);
    //    mc.options.keyDrop.setKeyConflictContext(notGuiBlocking);
    //    mc.options.keyInventory.setKeyConflictContext(notGuiBlocking);
    //    mc.options.keyLoadHotbarActivator.setKeyConflictContext(notGuiBlocking);
    //    mc.options.keyPickItem.setKeyConflictContext(notGuiBlocking);
    //    mc.options.keySaveHotbarActivator.setKeyConflictContext(notGuiBlocking);
    //    mc.options.keySwapOffhand.setKeyConflictContext(notGuiBlocking);
    //    mc.options.keyUse.setKeyConflictContext(notGuiBlocking);
    //    for (KeyBinding keyBinding : mc.options.keyHotbarSlots) keyBinding.setKeyConflictContext(notGuiBlocking);
//
    //    InputEvents.KEY_PRESSED.register(KeystoneKeybinds::onKeyInput);
    //}
//
    //public static void onKeyInput(int keycode, int action, int scancode, int modifiers)
    //{
    //    if (TOGGLE_KEYSTONE.isDown()) Keystone.toggleKeystone();
    //    else if (Keystone.isActive())
    //    {
    //        if (DELETE_BLOCKS.isDown()) Keystone.runInternalFilter(new FillTool(Blocks.AIR.getDefaultState()));
    //        if (event.getAction() == GLFW.GLFW_PRESS && event.getKey() == GLFW.GLFW_KEY_P) Keystone.getModule(HistoryModule.class).logHistoryStack();
    //    }
    //}
}
