package keystone.core.mixins.client;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import keystone.core.mixins.interfaces.KeystoneWorldRenderer;
import keystone.core.renderer.blocks.world.GhostWorld;
import keystone.core.renderer.blocks.world.GhostWorldRenderManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.*;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin implements KeystoneWorldRenderer
{
    @Shadow public abstract void renderSky(Matrix4f matrix4f, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback);
    @Shadow protected abstract void renderWeather(LightmapTextureManager manager, float tickDelta, double cameraX, double cameraY, double cameraZ);
    
    @Shadow @Mutable @Final private ObjectArrayList<ChunkBuilder.BuiltChunk> builtChunks;
    @Shadow private @Nullable PostEffectProcessor transparencyPostProcessor;
    @Shadow private Frustum frustum;
    @Shadow @Mutable @Final private BufferBuilderStorage bufferBuilders;
    @Shadow private @Nullable ClientWorld world;
    @Shadow private @Nullable ChunkBuilder chunkBuilder;
    
    @Unique private boolean keystone_isGhostWorldRenderer()
    {
        return world instanceof GhostWorld;
    }
    
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V"))
    public void cancelGhostRenderClear(int i, boolean bl)
    {
        if (!keystone_isGhostWorldRenderer()) RenderSystem.clear(i, bl);
    }
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderSky(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V"))
    public void cancelGhostSkyRender(WorldRenderer instance, Matrix4f matrix4f, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback)
    {
        if (!keystone_isGhostWorldRenderer()) renderSky(matrix4f, projectionMatrix, tickDelta, camera, thickFog, fogCallback);
    }
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderWeather(Lnet/minecraft/client/render/LightmapTextureManager;FDDD)V"))
    public void cancelGhostWeatherRender(WorldRenderer instance, LightmapTextureManager manager, float tickDelta, double cameraX, double cameraY, double cameraZ)
    {
        if (!keystone_isGhostWorldRenderer()) renderWeather(manager, tickDelta, cameraX, cameraY, cameraZ);
    }
    
    @Inject(method = "setupFrustum", at = @At("HEAD"))
    public void setupGhostFrustum(Vec3d vec3d, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci)
    {
        if (!keystone_isGhostWorldRenderer()) GhostWorldRenderManager.setupFrustum(vec3d, matrix4f, matrix4f2);
    }
    @Inject(method = "render", at = @At("HEAD"))
    public void setupGhostFrustum(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci)
    {
        if (!keystone_isGhostWorldRenderer()) GhostWorldRenderManager.render(tickCounter, renderBlockOutline, camera, gameRenderer, lightmapTextureManager, matrix4f, matrix4f2);
    }
    @Inject(method = "drawEntityOutlinesFramebuffer", at = @At("RETURN"))
    public void drawGhostEntityOutlinesFramebuffer(CallbackInfo ci)
    {
        if (!keystone_isGhostWorldRenderer()) GhostWorldRenderManager.drawEntityOutlinesFramebuffer();
    }
    @Inject(method = "close", at = @At("RETURN"))
    public void closeGhostRenderers(CallbackInfo ci)
    {
        if (!keystone_isGhostWorldRenderer()) GhostWorldRenderManager.close();
    }
    @Inject(method = "cleanUp", at = @At("RETURN"))
    public void cleanUpGhostRenderers(CallbackInfo ci)
    {
        if (!keystone_isGhostWorldRenderer()) GhostWorldRenderManager.cleanUp();
    }
    @Inject(method = "reload()V", at = @At("RETURN"))
    public void reloadGhostRenderers(CallbackInfo ci)
    {
        if (!keystone_isGhostWorldRenderer()) GhostWorldRenderManager.reload();
    }
    
    @Override
    public ObjectArrayList<ChunkBuilder.BuiltChunk> keystone_getBuiltChunks()
    {
        return builtChunks;
    }
    
    @Override
    public void keystone_setBuiltChunks(ObjectArrayList<ChunkBuilder.BuiltChunk> chunks)
    {
        builtChunks = chunks;
    }
    
    @Override
    public PostEffectProcessor keystone_getTransparencyPostProcessor()
    {
        return transparencyPostProcessor;
    }
    
    @Override
    public void keystone_setTransparencyPostProcessor(PostEffectProcessor postProcessor)
    {
        transparencyPostProcessor = postProcessor;
    }
    
    @Override
    public Frustum keystone_getFrustum()
    {
        return frustum;
    }
    
    @Override
    public void keystone_setFrustum(Frustum newFrustum)
    {
        frustum = newFrustum;
    }
    
    @Override
    public BufferBuilderStorage keystone_getBufferBuilders()
    {
        return bufferBuilders;
    }
    
    @Override
    public void keystone_setBufferBuilders(BufferBuilderStorage buffers)
    {
        bufferBuilders = buffers;
    }
    
    @Override
    public void keystone_setChunkBuilder(ChunkBuilder builder)
    {
        chunkBuilder = builder;
    }
}
