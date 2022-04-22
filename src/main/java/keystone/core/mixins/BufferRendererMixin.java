package keystone.core.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

@Mixin(BufferRenderer.class)
public class BufferRendererMixin
{
    @Inject(method = "draw(Ljava/nio/ByteBuffer;Lnet/minecraft/client/render/VertexFormat$DrawMode;Lnet/minecraft/client/render/VertexFormat;ILnet/minecraft/client/render/VertexFormat$IntType;IZ)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setupShaderLights(Lnet/minecraft/client/render/Shader;)V", shift = At.Shift.BEFORE))
    private static void applyLineWidthToDebug(ByteBuffer buffer, VertexFormat.DrawMode drawMode, VertexFormat vertexFormat, int count, VertexFormat.IntType elementFormat, int vertexCount, boolean textured, CallbackInfo callback)
    {
        Shader shader = RenderSystem.getShader();
        if (shader.lineWidth != null && (drawMode == VertexFormat.DrawMode.DEBUG_LINES || drawMode == VertexFormat.DrawMode.DEBUG_LINE_STRIP)) {
            shader.lineWidth.set(10);
        }
    }
}
