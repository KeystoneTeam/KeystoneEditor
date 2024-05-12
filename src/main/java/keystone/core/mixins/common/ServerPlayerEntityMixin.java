package keystone.core.mixins.common;

import keystone.core.events.minecraft.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin
{
    @Inject(method = "tick", at = @At("HEAD"))
    public void startTick(CallbackInfo callback)
    {
        ServerPlayerEvents.START_TICK.invoker().tick((ServerPlayerEntity)(Object)this);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void endTick(CallbackInfo callback)
    {
        ServerPlayerEvents.END_TICK.invoker().tick((ServerPlayerEntity)(Object)this);
    }
}
