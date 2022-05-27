package keystone.core.events.minecraft;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

public final class ClientPlayerEvents
{
    //region Client Events
    public static final Event<ClientTick> START_CLIENT_TICK = EventFactory.createArrayBacked(ClientTick.class, listeners -> player ->
    {
        for (final ClientTick listener : listeners) listener.tick(player);
    });
    public static final Event<ClientTick> END_CLIENT_TICK = EventFactory.createArrayBacked(ClientTick.class, listeners -> player ->
    {
        for (final ClientTick listener : listeners) listener.tick(player);
    });
    public static final Event<AllowUseBlock> ALLOW_USE_BLOCK = EventFactory.createArrayBacked(AllowUseBlock.class, (player, hand, hitResult) -> true, listeners -> (player, hand, hitResult) ->
    {
        boolean allow = true;
        for (final AllowUseBlock listener : listeners) if (!listener.allow(player, hand, hitResult)) allow = false;
        return allow;
    });
    //endregion
    //region Client Event Interfaces
    public interface ClientTick
    {
        void tick(ClientPlayerEntity player);
    }
    public interface AllowUseBlock
    {
        boolean allow(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult);
    }
    //endregion
}
