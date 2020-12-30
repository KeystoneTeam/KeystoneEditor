package keystone.core;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KeystoneMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeystoneKeybinds
{
    public static final KeyBinding TOGGLE_KEYSTONE = new KeyBinding("key.toggle_keystone", 75, "key.categories.keystone");

    public static void register()
    {
        ClientRegistry.registerKeyBinding(TOGGLE_KEYSTONE);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event)
    {
        if (TOGGLE_KEYSTONE.isPressed()) KeystoneMod.toggleKeystone();
        else if (KeystoneMod.KeystoneActive)
        {

        }
    }
}
