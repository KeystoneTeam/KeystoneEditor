package keystone.core.events.minecraft;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.world.ClientWorld;

public final class ClientWorldEvents
{
    public static final Event<WorldChanged> CHANGED_WORLDS = EventFactory.createArrayBacked(WorldChanged.class, listeners -> (previous, world) ->
    {
        for (final WorldChanged listener : listeners) listener.changed(previous, world);
    });

    public interface WorldChanged
    {
        void changed(ClientWorld previous, ClientWorld world);
    }
}
