package keystone.core.mixins.client;

import keystone.api.Keystone;
import keystone.core.gui.KeystoneOverlayHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin
{
    @Inject(method = "render", at = @At("HEAD"))
    public void preRender(DrawContext context, float tickDelta, CallbackInfo ci)
    {
        if (Keystone.isEnabled()) KeystoneOverlayHandler.onPreRenderGui();
    }

    @Inject(method = "renderVignetteOverlay", at = @At("HEAD"), cancellable = true)
    public void cancelVignette(DrawContext context, Entity entity, CallbackInfo callback)
    {
        if (Keystone.isEnabled()) callback.cancel();
    }
}
