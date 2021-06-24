package keystone.core.mixins;

import keystone.api.Keystone;
import keystone.core.KeystoneConfig;
import keystone.core.gui.KeystoneOverlayHandler;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft
{
    @Shadow @Final private MainWindow window;

    @Inject(method = "pauseGame", at = @At(value = "HEAD"), cancellable = true)
    public void pauseGame(boolean pauseOnly, CallbackInfo callback)
    {
        if (Keystone.isActive() && KeystoneConfig.disableInGameMenu) callback.cancel();
    }

    @Inject(method = "resizeDisplay", at = @At("RETURN"))
    public void resizeDisplay(CallbackInfo callback)
    {
        KeystoneOverlayHandler.resize(Minecraft.getInstance(), this.window.getGuiScaledWidth(), this.window.getGuiScaledHeight());
    }
}
