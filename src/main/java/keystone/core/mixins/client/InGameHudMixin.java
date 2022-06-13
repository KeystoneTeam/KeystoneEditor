package keystone.core.mixins.client;

import keystone.api.Keystone;
import keystone.core.gui.KeystoneOverlayHandler;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin
{
    @Inject(method = "render", at = @At("HEAD"))
    public void preRender(MatrixStack matrices, float tickDelta, CallbackInfo callback)
    {
        if (Keystone.isActive()) KeystoneOverlayHandler.onPreRenderGui();
    }

    @Inject(method = "renderVignetteOverlay", at = @At("HEAD"), cancellable = true)
    public void cancelVignette(Entity entity, CallbackInfo callback)
    {
        if (Keystone.isActive()) callback.cancel();
    }
}
