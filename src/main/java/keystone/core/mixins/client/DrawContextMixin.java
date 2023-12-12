package keystone.core.mixins.client;

import com.mojang.blaze3d.systems.RenderSystem;
import keystone.core.gui.KeystoneOverlayHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public class DrawContextMixin
{
    @Inject(method = "fill(Lnet/minecraft/client/render/RenderLayer;IIIIII)V", at = @At("HEAD"))
    private void fillHead(RenderLayer layer, int x1, int y1, int x2, int y2, int z, int color, CallbackInfo ci)
    {
        if (KeystoneOverlayHandler.isRendering())
        {
            KeystoneOverlayHandler.addMouseBlockingRegion(x1, y1, x2, y2);
            RenderSystem.depthMask(false);
        }
    }

    @Inject(method = "fill(Lnet/minecraft/client/render/RenderLayer;IIIIII)V", at = @At("RETURN"))
    private void fillReturn(RenderLayer layer, int x1, int y1, int x2, int y2, int z, int color, CallbackInfo ci)
    {
        if (KeystoneOverlayHandler.isRendering()) RenderSystem.depthMask(true);
    }
}
