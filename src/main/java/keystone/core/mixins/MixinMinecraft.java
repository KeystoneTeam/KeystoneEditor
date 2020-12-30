package keystone.core.mixins;

import keystone.core.Keystone;
import keystone.core.renderer.config.KeystoneConfig;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft
{
    @Inject(method = "displayInGameMenu", at = @At(value = "HEAD"), cancellable = true)
    public void displayInGameMenu(boolean pauseOnly, CallbackInfo callback)
    {
        if (Keystone.Active && KeystoneConfig.disableInGameMenu) callback.cancel();
    }
}
