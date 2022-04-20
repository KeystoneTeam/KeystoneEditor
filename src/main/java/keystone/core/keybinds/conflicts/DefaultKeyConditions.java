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
            return MinecraftClient.getInstance().currentScreen != null || (KEYSTONE_ACTIVE.test() && KeystoneGlobalState.BlockingKeys);
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
