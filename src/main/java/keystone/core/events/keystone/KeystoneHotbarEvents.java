package keystone.core.events.keystone;

import keystone.core.gui.screens.hotbar.KeystoneHotbarSlot;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class KeystoneHotbarEvents
{
    public static final Event<AllowChange> ALLOW_CHANGE = EventFactory.createArrayBacked(AllowChange.class, (previous, slot) -> true, listeners -> (previous, slot) ->
    {
        boolean allowed = true;
        for (final AllowChange listener : listeners) if (!listener.allowChange(previous, slot)) allowed = false;
        return allowed;
    });
    public static final Event<Changed> CHANGED = EventFactory.createArrayBacked(Changed.class, listeners -> (previous, slot) ->
    {
        for (final Changed listener : listeners) listener.changed(previous, slot);
    });

    public interface AllowChange
    {
        boolean allowChange(KeystoneHotbarSlot previous, KeystoneHotbarSlot slot);
    }
    public interface Changed
    {
        void changed(KeystoneHotbarSlot previous, KeystoneHotbarSlot slot);
    }
}
