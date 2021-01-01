package keystone.core;

import keystone.api.Keystone;
import keystone.api.tools.DeleteTool;
import keystone.modules.selection.SelectionModule;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KeystoneMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeystoneKeybinds
{
    public static final KeyBinding TOGGLE_KEYSTONE = new KeyBinding("key.toggle_keystone", 75, "key.categories.keystone");
    public static final KeyBinding CLEAR_SELECTIONS = new KeyBinding("key.clear_selections", 256, "key.categories.keystone");
    public static final KeyBinding DELETE_BLOCKS = new KeyBinding("key.delete_blocks", 261, "key.categories.keystone");

    public static void register()
    {
        ClientRegistry.registerKeyBinding(TOGGLE_KEYSTONE);
        ClientRegistry.registerKeyBinding(CLEAR_SELECTIONS);
        ClientRegistry.registerKeyBinding(DELETE_BLOCKS);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event)
    {
        if (TOGGLE_KEYSTONE.isPressed()) Keystone.toggleKeystone();
        else if (Keystone.isActive())
        {
            if (CLEAR_SELECTIONS.isPressed()) Keystone.getModule(SelectionModule.class).clearSelectionBoxes();
            if (DELETE_BLOCKS.isPressed()) Keystone.runTool(new DeleteTool());
        }
    }
}
