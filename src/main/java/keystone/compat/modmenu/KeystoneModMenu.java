package keystone.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import keystone.core.gui.screens.KeystoneOptionsScreen;

public class KeystoneModMenu implements ModMenuApi
{
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory()
    {
        return KeystoneOptionsScreen::new;
    }
}
