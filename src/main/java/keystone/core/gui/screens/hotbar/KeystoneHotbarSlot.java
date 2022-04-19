package keystone.core.gui.screens.hotbar;

import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;

public enum KeystoneHotbarSlot
{
    SELECTION("keystone.hotbar.selection"),
    BRUSH("keystone.hotbar.brush"),
    CLONE("keystone.hotbar.clone"),
    FILL("keystone.hotbar.fill"),
    FILTER("keystone.hotbar.filter"),
    IMPORT("keystone.hotbar.import"),
    SPAWN("keystone.hotbar.spawn");

    private final MutableText title;

    KeystoneHotbarSlot(String titleTranslationKey)
    {
        this.title = new TranslatableText(titleTranslationKey);
    }

    public MutableText getTitle()
    {
        return this.title;
    }
}
