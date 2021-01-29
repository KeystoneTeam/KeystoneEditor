package keystone.core.keybinds;

import keystone.api.Keystone;
import keystone.api.tools.FillTool;
import keystone.core.KeystoneMod;
import keystone.modules.selection.SelectionModule;
import net.minecraft.block.Blocks;
import net.minecraft.client.GameSettings;
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
    public static final KeyBinding CLEAR_SELECTIONS = new KeyBinding("key.clear_selections", GLFW.GLFW_KEY_ESCAPE, "key.categories.keystone");
    public static final KeyBinding DELETE_BLOCKS = new KeyBinding("key.delete_blocks", GLFW.GLFW_KEY_DELETE, "key.categories.keystone");
    public static final KeyBinding FILTER_TEST = new KeyBinding("key.filter_test", GLFW.GLFW_KEY_R, "key.categories.keystone");

    public static void register()
    {
        Minecraft mc = Minecraft.getInstance();
        IKeyConflictContext notGuiBlocking = KeystoneKeyConflictContext.NOT_GUI_BLOCKING;

        TOGGLE_KEYSTONE.setKeyConflictContext(notGuiBlocking);
        CLEAR_SELECTIONS.setKeyConflictContext(notGuiBlocking);
        DELETE_BLOCKS.setKeyConflictContext(notGuiBlocking);
        FILTER_TEST.setKeyConflictContext(notGuiBlocking);

        ClientRegistry.registerKeyBinding(TOGGLE_KEYSTONE);
        ClientRegistry.registerKeyBinding(CLEAR_SELECTIONS);
        ClientRegistry.registerKeyBinding(DELETE_BLOCKS);
        ClientRegistry.registerKeyBinding(FILTER_TEST);

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
