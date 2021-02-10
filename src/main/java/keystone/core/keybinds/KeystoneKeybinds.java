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

        mc.gameSettings.keyBindForward.setKeyConflictContext(notGuiBlocking);
        mc.gameSettings.keyBindLeft.setKeyConflictContext(notGuiBlocking);
        mc.gameSettings.keyBindBack.setKeyConflictContext(notGuiBlocking);
        mc.gameSettings.keyBindRight.setKeyConflictContext(notGuiBlocking);
        mc.gameSettings.keyBindJump.setKeyConflictContext(notGuiBlocking);
        mc.gameSettings.keyBindSneak.setKeyConflictContext(notGuiBlocking);
        mc.gameSettings.keyBindSprint.setKeyConflictContext(notGuiBlocking);
        mc.gameSettings.keyBindAttack.setKeyConflictContext(notGuiBlocking);
        mc.gameSettings.keyBindChat.setKeyConflictContext(notGuiBlocking);
        mc.gameSettings.keyBindPlayerList.setKeyConflictContext(notGuiBlocking);
        mc.gameSettings.keyBindCommand.setKeyConflictContext(notGuiBlocking);
        mc.gameSettings.keyBindTogglePerspective.setKeyConflictContext(notGuiBlocking);
        mc.gameSettings.keyBindSmoothCamera.setKeyConflictContext(notGuiBlocking);

        mc.gameSettings.keyBindAdvancements.setKeyConflictContext(notGuiBlocking);
        mc.gameSettings.keyBindDrop.setKeyConflictContext(notGuiBlocking);
        mc.gameSettings.keyBindInventory.setKeyConflictContext(notGuiBlocking);
        mc.gameSettings.keyBindLoadToolbar.setKeyConflictContext(notGuiBlocking);
        mc.gameSettings.keyBindPickBlock.setKeyConflictContext(notGuiBlocking);
        mc.gameSettings.keyBindSaveToolbar.setKeyConflictContext(notGuiBlocking);
        mc.gameSettings.keyBindSwapHands.setKeyConflictContext(notGuiBlocking);
        mc.gameSettings.keyBindUseItem.setKeyConflictContext(notGuiBlocking);
        for (KeyBinding keyBinding : mc.gameSettings.keyBindsHotbar) keyBinding.setKeyConflictContext(notGuiBlocking);
    }

    @SubscribeEvent
    public static final void onKeyInput(final InputEvent.KeyInputEvent event)
    {
        if (TOGGLE_KEYSTONE.isPressed()) Keystone.toggleKeystone();
        else if (Keystone.isActive())
        {
            if (DELETE_BLOCKS.isPressed()) Keystone.runTool(new FillTool(Blocks.AIR.getDefaultState()));
        }
    }
}
