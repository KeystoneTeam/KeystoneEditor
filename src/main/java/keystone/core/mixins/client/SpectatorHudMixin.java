package keystone.core.mixins.client;

import keystone.api.Keystone;
import net.minecraft.client.gui.hud.SpectatorHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpectatorHud.class)
public class SpectatorHudMixin
{
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render(MatrixStack matrices, CallbackInfo callback)
    {
        if (Keystone.isEnabled()) callback.cancel();
    }
}
