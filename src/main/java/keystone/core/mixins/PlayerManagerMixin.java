package keystone.core.mixins;

import keystone.api.enums.WorldType;
import keystone.core.events.keystone.KeystoneLifecycleEvents;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin
{
    @Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;onSpawn()V"))
    public void connectedToWorld(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo callback)
    {
        if (WorldType.get().supportsKeystone) KeystoneLifecycleEvents.OPEN_WORLD.invoker().join(player.getEntityWorld());
    }
}
