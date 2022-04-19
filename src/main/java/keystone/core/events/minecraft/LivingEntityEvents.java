package keystone.core.events.minecraft;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;

public class LivingEntityEvents
{
    public static final Event<Update> UPDATE = EventFactory.createArrayBacked(Update.class, entity -> true, listeners -> entity ->
    {
        boolean shouldTick = true;
        for (final Update listener : listeners)
        {
            if (!listener.shouldTick(entity)) shouldTick = false;
        }
        return shouldTick;
    });

    public interface Update
    {
        boolean shouldTick(LivingEntity entity);
    }
}
