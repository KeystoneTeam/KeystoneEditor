package keystone.core.gui.screens.hotbar;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

public enum KeystoneHotbarSlot
{
    SELECTION("keystone.hotbar.selection"),
    BRUSH("keystone.hotbar.brush"),
    CLONE("keystone.hotbar.clone"),
    FILL("keystone.hotbar.fill"),
    FILTER("keystone.hotbar.filter"),
    IMPORT("keystone.hotbar.import"),
    SPAWN("keystone.hotbar.spawn");

    private final Text title;

    KeystoneHotbarSlot(String titleTranslationKey)
    {
        this.title = Text.translatable(titleTranslationKey);
    }

    public Text getTitle()
    {
        return this.title;
    }
}
