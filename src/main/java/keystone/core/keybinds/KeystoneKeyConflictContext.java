package keystone.core.keybinds;

import keystone.gui.KeystoneOverlayHandler;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;

public enum KeystoneKeyConflictContext implements IKeyConflictContext
{
    GUI_BLOCKING
    {
        @Override
        public boolean isActive()
        {
            return KeyConflictContext.GUI.isActive() || KeystoneOverlayHandler.BlockingKeys;
        }
        @Override
        public boolean conflicts(IKeyConflictContext other)
        {
            return this == other;
        }
    },
    NOT_GUI_BLOCKING
    {
        @Override
        public boolean isActive()
        {
            return !GUI_BLOCKING.isActive();
        }
        @Override
        public boolean conflicts(IKeyConflictContext other)
        {
            return this == other;
        }
    }
}
