package keystone.core.keybinds.conflicts;

import keystone.api.Keystone;
import keystone.core.KeystoneGlobalState;
import net.minecraft.client.MinecraftClient;

public enum DefaultKeyConditions implements IKeyCondition
{
    GUI_OPEN
    {
        @Override
        public boolean test()
        {
            return MinecraftClient.getInstance().currentScreen != null || (KEYSTONE_ENABLED.test() && KeystoneGlobalState.GuiConsumingKeys);
        }
    },
    NO_GUI_OPEN
    {
        @Override
        public boolean test()
        {
            return !GUI_OPEN.test();
        }
    },
    KEYSTONE_ENABLED
    {
        @Override
        public boolean test()
        {
            return Keystone.isActive();
        }
    },
    KEYSTONE_DISABLED
    {
        @Override
        public boolean test()
        {
            return !Keystone.isActive();
        }
    },
    KEYSTONE_ACTIVE
    {
        @Override
        public boolean test()
        {
            return Keystone.isActive();
        }
    },
    KEYSTONE_INACTIVE
    {
        @Override
        public boolean test()
        {
            return !Keystone.isActive();
        }
    }
}
