package keystone.core.mixins;

import keystone.core.gui.KeystoneOverlayHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin
{
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "render", at = @At("TAIL"))
    public void render(float partialTicks, long nanoTime, boolean renderWorld, CallbackInfo callback)
    {
        if (!client.skipGameRender) KeystoneOverlayHandler.render(new MatrixStack(), partialTicks);
    }
}

