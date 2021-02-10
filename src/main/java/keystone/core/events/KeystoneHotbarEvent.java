package keystone.core.events;

import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class KeystoneHotbarEvent extends Event
{
    public final KeystoneHotbarSlot slot;

    public KeystoneHotbarEvent(KeystoneHotbarSlot slot)
    {
        this.slot = slot;
    }
}
