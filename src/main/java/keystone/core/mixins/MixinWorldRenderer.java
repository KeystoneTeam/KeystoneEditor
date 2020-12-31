package keystone.core.mixins;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.core.renderer.client.interop.ClientInterop;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.math.vector.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer
{
    @Shadow
    @Final
    private Minecraft mc;

    @Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/debug/DebugRenderer;render(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer$Impl;DDD)V", shift = At.Shift.BEFORE))
    private void renderFirst(MatrixStack matrixStack, float partialTicks, long ignored_2, boolean ignored_3, ActiveRenderInfo ignored_4, GameRenderer ignored_5, LightTexture ignored_6, Matrix4f ignored_7, CallbackInfo ci)
    {
        ClientInterop.render(partialTicks, this.mc.player);
    }

    @Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WorldRenderer;renderDebug(Lnet/minecraft/client/renderer/ActiveRenderInfo;)V", shift = At.Shift.BEFORE))
    private void render(MatrixStack ignored_1, float partialTicks, long ignored_2, boolean ignored_3, ActiveRenderInfo ignored_4, GameRenderer ignored_5, LightTexture ignored_6, Matrix4f ignored_7, CallbackInfo ci)
    {
        ClientInterop.renderDeferred(partialTicks);
    }
}
