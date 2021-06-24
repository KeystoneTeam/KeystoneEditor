package keystone.core.mixins;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import net.minecraft.client.gui.SpectatorGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpectatorGui.class)
public class MixinSpectatorGui
{
    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    public void renderHotbar(MatrixStack stack, float partialTicks, CallbackInfo callback)
    {
        if (Keystone.isActive()) callback.cancel();
    }

    @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
    public void renderTooltip(MatrixStack stack, CallbackInfo callback)
    {
        if (Keystone.isActive()) callback.cancel();
    }
}
