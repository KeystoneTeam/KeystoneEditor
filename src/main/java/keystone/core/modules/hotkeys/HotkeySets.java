package keystone.core.modules.hotkeys;

import keystone.api.Keystone;
import keystone.api.tools.DeleteEntitiesTool;
import keystone.api.tools.FillTool;
import keystone.core.DebugFlags;
import keystone.core.KeystoneGlobalState;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.screens.KeystoneOptionsScreen;
import keystone.core.modules.clipboard.ClipboardModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.modules.session.SessionModule;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class HotkeySets
{
    public static final HotkeySet DEFAULT = new HotkeySet("default", 1000);
    public static final HotkeySet SELECTION_MODE = new HotkeySet("selection_mode");
    
    static
    {
        DEFAULT.getHotkey(GLFW.GLFW_KEY_ESCAPE).addCondition(() -> !KeystoneOverlayHandler.escapeKeyInUse()).addListener(() -> MinecraftClient.getInstance().setScreenAndRender(new KeystoneOptionsScreen(null)));
        DEFAULT.getHotkey(GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_R).addListener(DebugFlags::reload);
        DEFAULT.getHotkey(GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_S).addListener(() -> Keystone.getModule(SessionModule.class).commitChanges());
        DEFAULT.getHotkey(GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_Z).addListener(() -> Keystone.getModule(HistoryModule.class).undo());
        DEFAULT.getHotkey(GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_Y).addListener(() -> Keystone.getModule(HistoryModule.class).redo());
        DEFAULT.getHotkey(GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_P).addListener(() -> Keystone.getModule(HistoryModule.class).logHistoryStack());
        DEFAULT.getHotkey(GLFW.GLFW_KEY_U).addListener(() -> KeystoneGlobalState.SuppressingBlockTicks = !KeystoneGlobalState.SuppressingBlockTicks);
        DEFAULT.getHotkey(GLFW.GLFW_KEY_H).addListener(() -> KeystoneGlobalState.HighlightTileEntities = !KeystoneGlobalState.HighlightTileEntities);
        DEFAULT.getHotkey(GLFW.GLFW_KEY_O).addListener(HotkeySets::featureTest);
    
        SELECTION_MODE.getHotkey(GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_D).addListener(() -> Keystone.getModule(SelectionModule.class).deselect());
        SELECTION_MODE.getHotkey(GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_X).addListener(() -> Keystone.getModule(ClipboardModule.class).cut());
        SELECTION_MODE.getHotkey(GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_C).addListener(() -> Keystone.getModule(ClipboardModule.class).copy());
        SELECTION_MODE.getHotkey(GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_V).addListener(() -> Keystone.getModule(ClipboardModule.class).paste());
        SELECTION_MODE.getHotkey(GLFW.GLFW_KEY_DELETE).addListener(() -> Keystone.runInternalFilters(new FillTool(Blocks.AIR.getDefaultState()), new DeleteEntitiesTool()));
    }
    
    private static void featureTest()
    {
    
    }
}
