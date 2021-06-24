package keystone.core.keybinds;

import keystone.api.Keystone;
import keystone.api.tools.FillTool;
import keystone.core.KeystoneMod;
import keystone.core.modules.selection.SelectionModule;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = KeystoneMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeystoneKeybinds
{
    public static final KeyBinding TOGGLE_KEYSTONE = new KeyBinding("key.toggle_keystone", GLFW.GLFW_KEY_K, "key.categories.keystone");
    public static final KeyBinding DELETE_BLOCKS = new KeyBinding("key.delete_blocks", GLFW.GLFW_KEY_DELETE, "key.categories.keystone");

    public static void register()
    {
        Minecraft mc = Minecraft.getInstance();
        IKeyConflictContext notGuiBlocking = KeystoneKeyConflictContext.NOT_GUI_BLOCKING;

        TOGGLE_KEYSTONE.setKeyConflictContext(notGuiBlocking);
        DELETE_BLOCKS.setKeyConflictContext(notGuiBlocking);

        ClientRegistry.registerKeyBinding(TOGGLE_KEYSTONE);
        ClientRegistry.registerKeyBinding(DELETE_BLOCKS);

        mc.options.keyUp.setKeyConflictContext(notGuiBlocking);
        mc.options.keyLeft.setKeyConflictContext(notGuiBlocking);
        mc.options.keyDown.setKeyConflictContext(notGuiBlocking);
        mc.options.keyRight.setKeyConflictContext(notGuiBlocking);
        mc.options.keyJump.setKeyConflictContext(notGuiBlocking);
        mc.options.keyShift.setKeyConflictContext(notGuiBlocking);
        mc.options.keySprint.setKeyConflictContext(notGuiBlocking);
        mc.options.keyAttack.setKeyConflictContext(notGuiBlocking);
        mc.options.keyChat.setKeyConflictContext(notGuiBlocking);
        mc.options.keyPlayerList.setKeyConflictContext(notGuiBlocking);
        mc.options.keyCommand.setKeyConflictContext(notGuiBlocking);
        mc.options.keyTogglePerspective.setKeyConflictContext(notGuiBlocking);
        mc.options.keySmoothCamera.setKeyConflictContext(notGuiBlocking);

        mc.options.keyAdvancements.setKeyConflictContext(notGuiBlocking);
        mc.options.keyDrop.setKeyConflictContext(notGuiBlocking);
        mc.options.keyInventory.setKeyConflictContext(notGuiBlocking);
        mc.options.keyLoadHotbarActivator.setKeyConflictContext(notGuiBlocking);
        mc.options.keyPickItem.setKeyConflictContext(notGuiBlocking);
        mc.options.keySaveHotbarActivator.setKeyConflictContext(notGuiBlocking);
        mc.options.keySwapOffhand.setKeyConflictContext(notGuiBlocking);
        mc.options.keyUse.setKeyConflictContext(notGuiBlocking);
        for (KeyBinding keyBinding : mc.options.keyHotbarSlots) keyBinding.setKeyConflictContext(notGuiBlocking);
    }

    @SubscribeEvent
    public static final void onKeyInput(final InputEvent.KeyInputEvent event)
    {
        if (TOGGLE_KEYSTONE.isDown()) Keystone.toggleKeystone();
        else if (Keystone.isActive())
        {
            if (DELETE_BLOCKS.isDown()) Keystone.runTool(new FillTool(Blocks.AIR.defaultBlockState()));
        }
    }
}
