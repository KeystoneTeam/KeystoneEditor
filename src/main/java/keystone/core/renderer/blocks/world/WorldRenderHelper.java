package keystone.core.renderer.blocks.world;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import keystone.core.mixins.client.RenderSystemAccessor;
import keystone.core.mixins.interfaces.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.*;
import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import net.minecraft.client.render.chunk.BlockBufferBuilderPool;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.HitResult;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public class WorldRenderHelper
{
    public static class WorldRenderContext
    {
        private ClientWorld world;
        private WorldRenderer worldRenderer;
        private LightmapTextureManager lightmapTextureManager;
        private boolean noClip;
        private boolean renderHand;
        private ObjectArrayList<ChunkBuilder.BuiltChunk> builtChunks;
        private HitResult crosshairTarget;
        private Camera camera;
        private PostEffectProcessor transparencyPostProcessor;
        private BufferBuilderStorage bufferBuilders;
        private BufferBuilderStorage clientBufferBuilders;
        private BlockBufferBuilderPool buffersPool;
        private BlockBufferAllocatorStorage bufferAllocatorStorage;
        private Frustum frustum;
        private Matrix4f projectionMatrix;
        private Matrix4fStack modelViewStack;
        
        private WorldRenderContext(ClientWorld world, WorldRenderer worldRenderer, LightmapTextureManager lightmapTextureManager, boolean noClip, boolean renderHand, ObjectArrayList<ChunkBuilder.BuiltChunk> builtChunks, HitResult crosshairTarget, Camera camera, PostEffectProcessor transparencyPostProcessor, BufferBuilderStorage bufferBuilders, BufferBuilderStorage clientBufferBuilders, BlockBufferBuilderPool buffersPool, BlockBufferAllocatorStorage bufferAllocatorStorage, Frustum frustum, Matrix4f projectionMatrix, Matrix4fStack modelViewStack)
        {
            this.world = world;
            this.worldRenderer = worldRenderer;
            this.lightmapTextureManager = lightmapTextureManager;
            this.noClip = noClip;
            this.renderHand = renderHand;
            this.builtChunks = builtChunks;
            this.crosshairTarget = crosshairTarget;
            this.camera = camera;
            this.transparencyPostProcessor = transparencyPostProcessor;
            this.bufferBuilders = bufferBuilders;
            this.clientBufferBuilders = clientBufferBuilders;
            this.buffersPool = buffersPool;
            this.bufferAllocatorStorage = bufferAllocatorStorage;
            this.frustum = frustum;
            this.projectionMatrix = projectionMatrix;
            this.modelViewStack = modelViewStack;
        }
        
        public static WorldRenderContext captureCurrent()
        {
            MinecraftClient client = MinecraftClient.getInstance();
            
            ClientWorld world = client.world;
            WorldRenderer renderer = client.worldRenderer;
            LightmapTextureManager lightmapTextureManager = client.gameRenderer.getLightmapTextureManager();
            boolean noClip = client.player.noClip;
            boolean renderHand = ((KeystoneGameRenderer)client.gameRenderer).keystone_getRenderHand();
            ObjectArrayList<ChunkBuilder.BuiltChunk> builtChunks = ((KeystoneWorldRenderer)renderer).keystone_getBuiltChunks();
            HitResult crosshairTarget = client.crosshairTarget;
            Camera camera = client.gameRenderer.getCamera();
            PostEffectProcessor transparencyPostProcessor = ((KeystoneWorldRenderer)renderer).keystone_getTransparencyPostProcessor();
            BufferBuilderStorage bufferBuilders = ((KeystoneWorldRenderer)renderer).keystone_getBufferBuilders();
            BufferBuilderStorage clientBufferBuilders = client.getBufferBuilders();
            BlockBufferBuilderPool buffersPool = ((KeystoneChunkBuilder)renderer.getChunkBuilder()).keystone_getBuffersPool();
            BlockBufferAllocatorStorage bufferAllocatorStorage = ((KeystoneChunkBuilder)renderer.getChunkBuilder()).keystone_getBufferAllocatorStorage();
            Frustum frustum = ((KeystoneWorldRenderer)renderer).keystone_getFrustum();
            Matrix4f projectionMatrix = RenderSystem.getProjectionMatrix();
            Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
            
            return new WorldRenderContext(world, renderer, lightmapTextureManager, noClip, renderHand, builtChunks, crosshairTarget, camera, transparencyPostProcessor, bufferBuilders, clientBufferBuilders, buffersPool, bufferAllocatorStorage, frustum, projectionMatrix, modelViewStack);
        }
        
        public WorldRenderContext copy()
        {
            return new WorldRenderContext(world, worldRenderer, lightmapTextureManager, noClip, renderHand, builtChunks, crosshairTarget, camera, transparencyPostProcessor, bufferBuilders, clientBufferBuilders, buffersPool, bufferAllocatorStorage, frustum, projectionMatrix, modelViewStack);
        }
        public WorldRenderContext setupGhostWorld(GhostWorld world)
        {
            this.world = world;
            this.worldRenderer = world.getRenderer();
            this.noClip = true;
            this.renderHand = false;
            this.crosshairTarget = null;
            this.bufferBuilders = ((KeystoneWorldRenderer)this.worldRenderer).keystone_getBufferBuilders();
            this.clientBufferBuilders = this.bufferBuilders;
            this.buffersPool = this.bufferBuilders.getBlockBufferBuildersPool();
            return this;
        }
        
        public void apply()
        {
            MinecraftClient client = MinecraftClient.getInstance();
            
            ((KeystoneMinecraftClient)client).keystone_setWorldRenderer(worldRenderer);
            client.world = world;
            //((KeystoneGameRenderer)client.gameRenderer).keystone_setLightmapTextureManager(lightmapTextureManager);
            client.getBlockEntityRenderDispatcher().world = world;
            client.getEntityRenderDispatcher().setWorld(world);
            //client.player.noClip = noClip;
            //client.gameRenderer.setRenderHand(renderHand);
            
            ((KeystoneParticleManager)client.particleManager).setWorld(world);
            client.crosshairTarget = crosshairTarget;
            ((KeystoneGameRenderer)client.gameRenderer).keystone_setCamera(camera);
            
            ((KeystoneWorldRenderer)client.worldRenderer).keystone_setTransparencyPostProcessor(transparencyPostProcessor);
            ((KeystoneWorldRenderer)client.worldRenderer).keystone_setBuiltChunks(builtChunks);
            
            ((KeystoneWorldRenderer)client.worldRenderer).keystone_setBufferBuilders(bufferBuilders);
            ((KeystoneMinecraftClient)client).keystone_setBufferBuilders(clientBufferBuilders);
            ((KeystoneChunkBuilder) worldRenderer.getChunkBuilder()).keystone_setBuffersPool(buffersPool);
            ((KeystoneChunkBuilder) worldRenderer.getChunkBuilder()).keystone_setBufferAllocatorStorage(bufferAllocatorStorage);
            
            ((KeystoneWorldRenderer) worldRenderer).keystone_setFrustum(frustum);
            
            client.gameRenderer.loadProjectionMatrix(projectionMatrix);
            RenderSystemAccessor.setModelViewStack(modelViewStack);
            RenderSystem.applyModelViewMatrix();
        }
        
        public ClientWorld getWorld()
        {
            return world;
        }
        
        public WorldRenderer getWorldRenderer()
        {
            return worldRenderer;
        }
        
        public LightmapTextureManager getLightmapTextureManager()
        {
            return lightmapTextureManager;
        }
        
        public boolean isNoClip()
        {
            return noClip;
        }
        
        public boolean isRenderHand()
        {
            return renderHand;
        }
        
        public ObjectArrayList<ChunkBuilder.BuiltChunk> getBuiltChunks()
        {
            return builtChunks;
        }
        
        public HitResult getCrosshairTarget()
        {
            return crosshairTarget;
        }
        
        public Camera getCamera()
        {
            return camera;
        }
        
        public PostEffectProcessor getTransparencyPostProcessor()
        {
            return transparencyPostProcessor;
        }
        
        public BufferBuilderStorage getBufferBuilders()
        {
            return bufferBuilders;
        }
        
        public BufferBuilderStorage getClientBufferBuilders()
        {
            return clientBufferBuilders;
        }
        
        public BlockBufferBuilderPool getBuffersPool()
        {
            return buffersPool;
        }
        
        public Frustum getFrustum()
        {
            return frustum;
        }
        
        public Matrix4f getProjectionMatrix()
        {
            return projectionMatrix;
        }
        
        public Matrix4fStack getModelViewStack()
        {
            return modelViewStack;
        }
    }
}
