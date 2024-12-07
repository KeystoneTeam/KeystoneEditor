package keystone.core.renderer.blocks.world;

import keystone.core.mixins.interfaces.KeystoneWorldRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.Packet;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.apache.commons.lang3.NotImplementedException;
import org.joml.Matrix4f;

import java.util.function.Supplier;

public class GhostWorld extends ClientWorld
{
    private static final int DEFAULT_GHOST_FLAGS = Block.NOTIFY_LISTENERS | Block.FORCE_STATE | Block.SKIP_DROPS;
    private final ClientWorld template;
    private final WorldRenderer renderer;
    
    private GhostWorld(ClientPlayNetworkHandler networkHandler, ClientWorld template, Properties levelProperties, RegistryKey<World> registryKey, RegistryEntry<DimensionType> dimensionType, int loadDistance, int simulationDistance, Supplier<Profiler> profilerSupplier, WorldRenderer renderer, boolean debugWorld, long seed)
    {
        super(networkHandler, levelProperties, registryKey, dimensionType, loadDistance, simulationDistance, profilerSupplier, renderer, debugWorld, seed);
        this.chunkManager = new GhostChunkManager(this, loadDistance);
        
        this.networkHandler = null;
        this.template = template;
        this.renderer = renderer;
    }
    
    public static GhostWorld create() { return create(0, false); }
    public static GhostWorld createAndRegister() { return create(0, true); }
    public static GhostWorld create(boolean register) { return create(0, register); }
    public static GhostWorld create(long seed, boolean register)
    {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld template = client.world;
        int loadDistance = MinecraftClient.getInstance().options.getViewDistance().getValue();
        assert template != null;
        
        EntityRenderDispatcher entityRenderDispatcher = new EntityRenderDispatcher(client, client.getTextureManager(), client.getItemRenderer(), client.getBlockRenderManager(), client.textRenderer, client.options, client.getEntityModelLoader());
        BlockEntityRenderDispatcher blockEntityRenderDispatcher = new BlockEntityRenderDispatcher(client.textRenderer, client.getEntityModelLoader(), client::getBlockRenderManager, client::getItemRenderer, () -> entityRenderDispatcher);
        BufferBuilderStorage bufferBuilders = new BufferBuilderStorage(Runtime.getRuntime().availableProcessors());
        
        WorldRenderer renderer = new WorldRenderer(client, entityRenderDispatcher, blockEntityRenderDispatcher, bufferBuilders)
        {
            @Override public void renderSky(Matrix4f matrix4f, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback) { }
            @Override public void renderClouds(MatrixStack matrices, Matrix4f matrix4f, Matrix4f matrix4f2, float tickDelta, double cameraX, double cameraY, double cameraZ) { }
        };
        GhostWorld ghostWorld = new GhostWorld(client.getNetworkHandler(), template, template.getLevelProperties(), template.getRegistryKey(), template.getDimensionEntry(), loadDistance, template.getSimulationDistance(), template.getProfilerSupplier(), renderer, template.isDebugWorld(), seed);
        ((KeystoneWorldRenderer)renderer).keystone_setChunkBuilder(new ChunkBuilder(ghostWorld, renderer, Util.getMainWorkerExecutor(), bufferBuilders, client.getBlockRenderManager(), blockEntityRenderDispatcher));
        
        renderer.setWorld(ghostWorld);
        
        if (register) GhostWorldRenderManager.registerGhostWorld(ghostWorld);
        return ghostWorld;
    }
    
    public WorldRenderer getRenderer() { return renderer; }
    
    @Override public void disconnect() { throw new NotImplementedException("Trying to call disconnect() on a ghost world!"); }
    @Override public void sendPacket(Packet<?> packet) { throw new NotImplementedException("Trying to call sendPacket(Packet<?> packet) on a ghost world!"); }
    @Override public Scoreboard getScoreboard() { throw new NotImplementedException("Trying to call getScoreboard() on a ghost world!"); }
    
    @Override public RecipeManager getRecipeManager() { return template.getRecipeManager(); }
    @Override public FeatureSet getEnabledFeatures() { return template.getEnabledFeatures(); }
    @Override public BrewingRecipeRegistry getBrewingRecipeRegistry() { return template.getBrewingRecipeRegistry(); }
    
    @Override
    public boolean setBlockState(BlockPos pos, BlockState state)
    {
        return super.setBlockState(pos, state, DEFAULT_GHOST_FLAGS);
    }
}