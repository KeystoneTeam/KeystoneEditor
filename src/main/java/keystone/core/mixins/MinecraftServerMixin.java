package keystone.core.mixins;

import keystone.core.KeystoneMod;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin
{
    @Inject(method = "runServer", at = @At("HEAD"))
    public void gameLoaded(CallbackInfo callback)
    {
        KeystoneMod.tryGameLoaded();
    }
}
