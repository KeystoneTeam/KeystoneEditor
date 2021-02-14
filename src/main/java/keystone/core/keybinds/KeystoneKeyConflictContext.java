package keystone.core.keybinds;

import keystone.core.KeystoneGlobalState;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;

public enum KeystoneKeyConflictContext implements IKeyConflictContext
{
    GUI_BLOCKING
    {
        @Override
        public boolean isActive()
        {
            return KeyConflictContext.GUI.isActive() || KeystoneGlobalState.BlockingKeys;
        }
        @Override
        public boolean conflicts(IKeyConflictContext other)
        {
            return this == other || other == KeyConflictContext.GUI;
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
            return this == other || other == KeyConflictContext.IN_GAME;
        }
    }
}
