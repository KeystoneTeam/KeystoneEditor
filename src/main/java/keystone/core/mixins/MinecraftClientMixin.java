package keystone.core.mixins;

import keystone.api.Keystone;
import keystone.api.KeystoneDirectories;
import keystone.core.KeystoneConfig;
import keystone.core.gui.KeystoneOverlayHandler;
import keystone.core.modules.history.HistoryModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.server.SaveLoader;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin
{
    @Shadow @Final private Window window;

    @Inject(method = "openPauseMenu", at = @At(value = "HEAD"), cancellable = true)
    public void pauseGame(boolean pauseOnly, CallbackInfo callback)
    {
        if (Keystone.isActive() && KeystoneConfig.disableInGameMenu) callback.cancel();
    }

    @Inject(method = "onResolutionChanged", at = @At("RETURN"))
    public void resizeDisplay(CallbackInfo callback)
    {
        KeystoneOverlayHandler.resize(MinecraftClient.getInstance(), this.window.getScaledWidth(), this.window.getScaledHeight());
    }
}
