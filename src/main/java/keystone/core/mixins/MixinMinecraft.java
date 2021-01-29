package keystone.core.mixins;

import keystone.api.Keystone;
import keystone.core.KeystoneConfig;
import keystone.gui.KeystoneOverlayHandler;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(Minecraft.class)
public class MixinMinecraft
{
    @Shadow @Final private MainWindow mainWindow;

    @Inject(method = "displayInGameMenu", at = @At(value = "HEAD"), cancellable = true)
    public void displayInGameMenu(boolean pauseOnly, CallbackInfo callback)
    {
        if (Keystone.isActive() && KeystoneConfig.disableInGameMenu) callback.cancel();
    }

    @Inject(method = "updateWindowSize", at = @At("RETURN"))
    public void updateWindowSize(CallbackInfo callback)
    {
        KeystoneOverlayHandler.resize(Minecraft.getInstance(), this.mainWindow.getScaledWidth(), this.mainWindow.getScaledHeight());
    }
}
