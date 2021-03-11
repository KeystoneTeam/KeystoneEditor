package keystone.core.mixins;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.core.gui.KeystoneOverlayHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer
{
    @Shadow @Final private Minecraft mc;

    @Inject(method = "updateCameraAndRender", at = @At("TAIL"))
    public void updateCameraAndRender(float partialTicks, long nanoTime, boolean renderWorld, CallbackInfo callback)
    {
        if (!mc.skipRenderWorld) KeystoneOverlayHandler.render(new MatrixStack(), partialTicks);
    }
}
