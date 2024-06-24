package keystone.core.mixins.client;

import keystone.api.Keystone;
import keystone.api.enums.WorldType;
import keystone.core.events.keystone.KeystoneLifecycleEvents;
import keystone.core.modules.world_cache.WorldCacheModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin extends ClientCommonNetworkHandler
{
    protected ClientPlayNetworkHandlerMixin(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState)
    {
        super(client, connection, connectionState);
    }
    
    @Inject(method = "onGameJoin", at = @At("RETURN"))
    private void triggerOpenWorld(GameJoinS2CPacket packet, CallbackInfo ci)
    {
        if (WorldType.get().supportsKeystone)
        {
            ServerWorld world = Keystone.getModule(WorldCacheModule.class).getDimensionWorld(client.player.getEntityWorld().getRegistryKey());
            KeystoneLifecycleEvents.OPEN_WORLD.invoker().join(world);
        }
    }
}
