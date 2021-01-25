package keystone.core;

import keystone.api.Keystone;
import keystone.api.tools.FillTool;
import keystone.modules.selection.SelectionModule;
import net.minecraft.block.Blocks;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = KeystoneMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeystoneKeybinds
{
    public static final KeyBinding TOGGLE_KEYSTONE = new KeyBinding("key.toggle_keystone", GLFW.GLFW_KEY_K, "key.categories.keystone");
    public static final KeyBinding CLEAR_SELECTIONS = new KeyBinding("key.clear_selections", GLFW.GLFW_KEY_ESCAPE, "key.categories.keystone");
    public static final KeyBinding DELETE_BLOCKS = new KeyBinding("key.delete_blocks", GLFW.GLFW_KEY_DELETE, "key.categories.keystone");
    public static final KeyBinding FILTER_TEST = new KeyBinding("key.filter_test", GLFW.GLFW_KEY_R, "key.categories.keystone");

    public static void register()
    {
        ClientRegistry.registerKeyBinding(TOGGLE_KEYSTONE);
        ClientRegistry.registerKeyBinding(CLEAR_SELECTIONS);
        ClientRegistry.registerKeyBinding(DELETE_BLOCKS);
        ClientRegistry.registerKeyBinding(FILTER_TEST);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event)
    {
        if (TOGGLE_KEYSTONE.isPressed()) Keystone.toggleKeystone();
        else if (Keystone.isActive())
        {
            if (CLEAR_SELECTIONS.isPressed()) Keystone.getModule(SelectionModule.class).onCancelPressed();
            if (DELETE_BLOCKS.isPressed()) Keystone.runTool(new FillTool(Blocks.AIR.getDefaultState()));
            if (FILTER_TEST.isPressed()) Keystone.runFilter("C:\\Users\\codec\\Desktop\\TestFilter.java");
        }
    }
}
