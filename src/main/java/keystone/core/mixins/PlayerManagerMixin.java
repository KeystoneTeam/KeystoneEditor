package keystone.core.mixins;

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
    @Inject(method = "onPlayerConnect", at = @At(value = "RETURN"))
    public void connectedToWorld(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo callback)
    {
    }
}
