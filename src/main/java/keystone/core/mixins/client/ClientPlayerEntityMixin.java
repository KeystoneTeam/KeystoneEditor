package keystone.core.mixins.client;

import keystone.core.events.minecraft.ClientPlayerEvents;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin
{
    @Inject(method = "tick", at = @At("HEAD"))
    public void startTick(CallbackInfo callback)
    {
        ClientPlayerEvents.START_CLIENT_TICK.invoker().tick((ClientPlayerEntity)(Object)this);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void endTick(CallbackInfo callback)
    {
        ClientPlayerEvents.END_CLIENT_TICK.invoker().tick((ClientPlayerEntity)(Object)this);
    }
}
