package keystone.core.mixins.client;

import keystone.api.Keystone;
import keystone.core.KeystoneConfig;
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

    @Inject(method = "openPauseMenu", at = @At(value = "HEAD"), cancellable = true)
    public void pauseGame(boolean pauseOnly, CallbackInfo callback)
    {
        if (Keystone.isEnabled() && KeystoneConfig.disableInGameMenu) callback.cancel();
    }

    @Inject(method = "onResolutionChanged", at = @At("RETURN"))
    public void resizeDisplay(CallbackInfo callback)
    {
        KeystoneOverlayHandler.resize(MinecraftClient.getInstance(), this.window.getScaledWidth(), this.window.getScaledHeight());
    }

    @Inject(method = "isMultiplayerEnabled", at = @At("HEAD"), cancellable = true)
    public void isMultiplayerAllowed(CallbackInfoReturnable<Boolean> callback)
    {
        callback.setReturnValue(false);
    }

    @Inject(method = "isRealmsEnabled", at = @At("HEAD"), cancellable = true)
    public void isRealmsEnabled(CallbackInfoReturnable<Boolean> callback)
    {
        callback.setReturnValue(false);
    }
}
