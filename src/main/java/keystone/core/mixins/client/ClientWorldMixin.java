package keystone.core.mixins.client;

import keystone.api.Keystone;
import keystone.api.enums.WorldType;
import keystone.core.events.keystone.KeystoneLifecycleEvents;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class ClientWorldMixin
{
    @Inject(method = "disconnect", at = @At("HEAD"))
    public void worldLeave(CallbackInfo callback)
    {
        if (WorldType.get().supportsKeystone)
        {
            Keystone.disableKeystone();
            KeystoneLifecycleEvents.CLOSE_WORLD.invoker().leave();
        }
    }
}
