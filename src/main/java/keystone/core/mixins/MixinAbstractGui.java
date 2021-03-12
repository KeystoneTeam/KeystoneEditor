package keystone.core.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import keystone.core.gui.KeystoneOverlayHandler;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractGui.class)
public class MixinAbstractGui
{
    @Inject(method = "fill(Lnet/minecraft/util/math/vector/Matrix4f;IIIII)V", at = @At("HEAD"))
    private static void fillHead(Matrix4f matrix, int minX, int minY, int maxX, int maxY, int color, CallbackInfo callback)
    {
        if (KeystoneOverlayHandler.isRendering()) RenderSystem.depthMask(false);
    }

    @Inject(method = "fill(Lnet/minecraft/util/math/vector/Matrix4f;IIIII)V", at = @At("RETURN"))
    private static void fillReturn(Matrix4f matrix, int minX, int minY, int maxX, int maxY, int color, CallbackInfo callback)
    {
        if (KeystoneOverlayHandler.isRendering()) RenderSystem.depthMask(true);
    }
}
