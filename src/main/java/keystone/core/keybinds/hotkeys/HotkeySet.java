package keystone.core.keybinds.hotkeys;

import keystone.api.Keystone;
import keystone.core.DebugFlags;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.gui.screens.KeystoneOptionsScreen;
import keystone.core.modules.clipboard.ClipboardModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.modules.session.SessionModule;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import static keystone.core.keybinds.hotkeys.HotkeyManager.*;

public abstract class HotkeySet
{
    public static final HotkeySet EMPTY = new HotkeySet()
    {
        @Override public void addHotkeys() { }
        @Override public void removeHotkeys() { }
    };
    public static final HotkeySet DEFAULT = new HotkeySet()
    {
        @Override
        public void addHotkeys()
        {
            getHotkey(GLFW.GLFW_KEY_ESCAPE).addCondition(() -> !KeystoneOverlayHandler.escapeKeyInUse()).addListener(() -> MinecraftClient.getInstance().setScreenAndRender(new KeystoneOptionsScreen(null)));
            getHotkey(GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_R).addListener(DebugFlags::reload);
            getHotkey(GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_S).addListener(() -> Keystone.getModule(SessionModule.class).commitChanges());
            getHotkey(GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_Z).addListener(() -> Keystone.getModule(HistoryModule.class).undo());
            getHotkey(GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_Y).addListener(() -> Keystone.getModule(HistoryModule.class).redo());
            getHotkey(GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_P).addListener(() -> Keystone.getModule(HistoryModule.class).logHistoryStack());
        }
    
        @Override
        public void removeHotkeys()
        {
            clearHotkeys();
        }
    };
    public static final HotkeySet SELECTION_MODE = new HotkeySet()
    {
        @Override
        public void addHotkeys()
        {
            push();
            getHotkey(GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_D).addListener(() -> Keystone.getModule(SelectionModule.class).deselect());
            getHotkey(GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_X).addListener(() -> Keystone.getModule(ClipboardModule.class).cut());
            getHotkey(GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_C).addListener(() -> Keystone.getModule(ClipboardModule.class).copy());
            getHotkey(GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_V).addListener(() -> Keystone.getModule(ClipboardModule.class).paste());
        }
    };
    
    public abstract void addHotkeys();
    public void removeHotkeys() { pop(); }
}
