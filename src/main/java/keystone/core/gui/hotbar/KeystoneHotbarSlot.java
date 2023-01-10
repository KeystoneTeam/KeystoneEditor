package keystone.core.gui.hotbar;

import keystone.core.keybinds.hotkeys.HotkeySet;
import net.minecraft.text.Text;

public enum KeystoneHotbarSlot
{
    SELECTION("keystone.hotbar.selection", HotkeySet.SELECTION_MODE),
    BRUSH("keystone.hotbar.brush"),
    CLONE("keystone.hotbar.clone"),
    FILL("keystone.hotbar.fill"),
    FILTER("keystone.hotbar.filter"),
    IMPORT("keystone.hotbar.import"),
    SPAWN("keystone.hotbar.spawn");

    private final Text title;
    private final HotkeySet hotkeys;

    KeystoneHotbarSlot(String titleTranslationKey)
    {
        this(titleTranslationKey, HotkeySet.EMPTY);
    }
    KeystoneHotbarSlot(String titleTranslationKey, HotkeySet hotkeys)
    {
        this.title = Text.translatable(titleTranslationKey);
        this.hotkeys = hotkeys;
    }

    public Text getTitle()
    {
        return this.title;
    }
    public HotkeySet getHotkeys()
    {
        return this.hotkeys;
    }
}
