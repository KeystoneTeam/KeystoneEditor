package keystone.core.gui.screens.hotbar;

import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public enum KeystoneHotbarSlot
{
    SELECTION("keystone.hotbar.selection"),
    BRUSH("keystone.hotbar.brush"),
    CLONE("keystone.hotbar.clone"),
    FILL("keystone.hotbar.fill"),
    FILTER("keystone.hotbar.filter"),
    IMPORT("keystone.hotbar.import"),
    SPAWN("keystone.hotbar.spawn");

    private final IFormattableTextComponent title;

    KeystoneHotbarSlot(String titleTranslationKey)
    {
        this.title = new TranslationTextComponent(titleTranslationKey);
    }

    public IFormattableTextComponent getTitle()
    {
        return this.title;
    }
}
