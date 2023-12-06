package keystone.core.mixins.client;

import keystone.core.KeystoneGlobalState;
import keystone.core.gui.KeystoneOverlayHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin
{
    @Shadow @Final MinecraftClient client;
    @Shadow @Final private BufferBuilderStorage buffers;
    
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;draw()V", shift = At.Shift.BEFORE))
    public void renderOverlays(float partialTicks, long nanoTime, boolean renderWorld, CallbackInfo callback)
    {
        if (!client.skipGameRender)
        {
            int i = (int)(this.client.mouse.getX() * (double)this.client.getWindow().getScaledWidth() / (double)this.client.getWindow().getWidth());
            int j = (int)(this.client.mouse.getY() * (double)this.client.getWindow().getScaledHeight() / (double)this.client.getWindow().getHeight());
            DrawContext drawContext = new DrawContext(this.client, this.buffers.getEntityVertexConsumers());
            KeystoneOverlayHandler.render(this.client, drawContext, i, j, this.client.getLastFrameDuration());
            drawContext.draw();
        }
    }
    
    @Inject(method = "render", at = @At("RETURN"))
    public void reloadWorldRenderer(float tickDelta, long startTime, boolean tick, CallbackInfo ci)
    {
        if (KeystoneGlobalState.ReloadWorldRenderer)
        {
            client.worldRenderer.reload();
            KeystoneGlobalState.ReloadWorldRenderer = false;
        }
    }
}

