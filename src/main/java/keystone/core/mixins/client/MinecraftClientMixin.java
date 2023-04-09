package keystone.core.mixins.client;

import keystone.api.Keystone;
import keystone.core.KeystoneMod;
import keystone.core.gui.KeystoneOverlayHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin
{
    @Shadow @Final private Window window;

    @Inject(method = "run", at = @At("HEAD"))
    public void gameLoaded(CallbackInfo callback)
    {
        KeystoneMod.tryGameLoaded();
    }
    
    @Inject(method = "openPauseMenu", at = @At("HEAD"), cancellable = true)
    public void pauseGame(boolean pauseOnly, CallbackInfo callback)
    {
        if (Keystone.isEnabled()) callback.cancel();
    }

    @Inject(method = "onResolutionChanged", at = @At("RETURN"))
    public void resizeDisplay(CallbackInfo callback)
    {
        KeystoneOverlayHandler.resize(MinecraftClient.getInstance(), this.window.getScaledWidth(), this.window.getScaledHeight());
    }
}
