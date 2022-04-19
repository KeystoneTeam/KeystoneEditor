package keystone.core.events.minecraft;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

public final class ServerPlayerEvents
{
    //region Events
    public static final Event<Tick> START_TICK = EventFactory.createArrayBacked(Tick.class, listeners -> player ->
    {
        for (final Tick listener : listeners) listener.tick(player);
    });
    public static final Event<Tick> END_TICK = EventFactory.createArrayBacked(Tick.class, listeners -> player ->
    {
        for (final Tick listener : listeners) listener.tick(player);
    });
    public static final Event<AllowUseBlock> ALLOW_USE_BLOCK = EventFactory.createArrayBacked(AllowUseBlock.class, (player, world, stack, hand, hitResult) -> true, listeners -> (player, world, stack, hand, hitResult) ->
    {
        boolean allow = true;
        for (final AllowUseBlock listener : listeners) if (!listener.allow(player, world, stack, hand, hitResult)) allow = false;
        return allow;
    });
    //endregion
    //region Event Interfaces
    public interface Tick
    {
        void tick(ServerPlayerEntity player);
    }
    public interface AllowUseBlock
    {
        boolean allow(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult);
    }
    //endregion
}
