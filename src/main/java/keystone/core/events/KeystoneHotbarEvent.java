package keystone.core.events;

import keystone.core.gui.screens.hotbar.KeystoneHotbar;
import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class KeystoneHotbarEvent extends Event
{
    public final KeystoneHotbarSlot previousSlot;
    public final KeystoneHotbarSlot slot;

    public KeystoneHotbarEvent(KeystoneHotbarSlot slot)
    {
        this.previousSlot = KeystoneHotbar.getSelectedSlot();
        this.slot = slot;
    }
}
