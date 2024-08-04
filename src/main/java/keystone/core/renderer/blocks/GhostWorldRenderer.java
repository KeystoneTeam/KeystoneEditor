package keystone.core.renderer.blocks;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import keystone.core.renderer.blocks.buffer.SuperByteBuffer;
import keystone.core.renderer.blocks.buffer.SuperRenderTypeBuffer;
import keystone.core.renderer.blocks.world.GhostBlocksWorld;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import net.minecraft.client.render.chunk.SectionBuilder;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GhostWorldRenderer
{
    private static GhostSectionBuilder chunkBuilder;
    
    private final MinecraftClient minecraft;
    private final Map<ChunkSectionPos, BuiltGhostChunkSection> chunks;

    protected GhostBlocksWorld ghostBlocks;
    public Vec3d offset;
    private boolean changed;
    
    public GhostWorldRenderer()
    {
        minecraft = MinecraftClient.getInstance();
        chunks = new Reference2ObjectArrayMap<>();
        offset = Vec3d.ZERO;
        changed = false;
        if (chunkBuilder == null) chunkBuilder = new GhostSectionBuilder(minecraft);
    }

    public void display(GhostBlocksWorld world)
    {
        this.ghostBlocks = world;
        this.changed = true;
    }
    public void markDirty()
    {
        changed = true;
    }

    public void tick()
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null || !changed) return;

        redraw();
        changed = false;
    }

    public void render(MatrixStack ms, SuperRenderTypeBuffer buffer, float partialTicks)
    {
        ms.push();
        applyOrientation(ms);
        RenderSystem.disableCull();

        // Dispatch Ghost World Entity Rendering
        EntityRenderDispatcher entityRenderer = minecraft.getEntityRenderDispatcher();
        ghostBlocks.getEntities().forEach(entity ->
        {
            int light = LightmapTextureManager.pack(15, 15);
            entityRenderer.render(entity, entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), 0, ms, buffer, light);
        });
    
        // Process Each RenderLayer
        for (RenderLayer layer : RenderLayer.getBlockLayers())
        {
            // Iterate through chunk sections
            for (Map.Entry<ChunkSectionPos, BuiltGhostChunkSection> entry : chunks.entrySet())
            {
                // Get Chunk Data
                ChunkSectionPos pos = entry.getKey();
                BuiltGhostChunkSection section = entry.getValue();
                
                // Apply Chunk Position
                ms.push();
                ms.translate(pos.getMinX(), pos.getMinY(), pos.getMinZ());
                
                // Render Layer
                SuperByteBuffer layerBuffer = section.buffers.get(layer);
                if (layerBuffer != null) layerBuffer.renderInto(ms, buffer.getBuffer(layer));
                
                ms.pop();
            }
        }
        TileEntityRenderHelper.renderTileEntities(ghostBlocks, ghostBlocks.getRenderedTileEntities(), ms, new MatrixStack(), buffer, partialTicks);

        ms.pop();
    }
    
    protected void redraw()
    {
        chunks.clear();
        ChunkSectionPos min = ChunkSectionPos.from(new BlockPos(ghostBlocks.getBounds().getMinX(), ghostBlocks.getBounds().getMinY(), ghostBlocks.getBounds().getMinZ()));
        ChunkSectionPos max = ChunkSectionPos.from(new BlockPos(ghostBlocks.getBounds().getMaxX(), ghostBlocks.getBounds().getMaxY(), ghostBlocks.getBounds().getMaxZ()));
        
        for (int sectionY = min.getSectionY(); sectionY <= max.getSectionY(); sectionY++)
        {
            for (int sectionX = min.getSectionX(); sectionX <= max.getSectionX(); sectionX++)
            {
                for (int sectionZ = min.getSectionX(); sectionZ <= max.getSectionZ(); sectionZ++)
                {
                    ChunkSectionPos pos = ChunkSectionPos.from(sectionX, sectionY, sectionZ);
                    BuiltGhostChunkSection built = chunkBuilder.build(ghostBlocks, pos);
                    chunks.put(pos, built);
                }
            }
        }
    }
    
    private void applyOrientation(MatrixStack matrixStack)
    {
        matrixStack.translate(offset.x, offset.y, offset.z);
        
        // Apply Ghost World Orientation to MatrixStack
        int xAxisSize = ghostBlocks.getRotation() == BlockRotation.NONE || ghostBlocks.getRotation() == BlockRotation.CLOCKWISE_180 ? ghostBlocks.getBounds().getBlockCountX() : ghostBlocks.getBounds().getBlockCountZ();
        int zAxisSize = ghostBlocks.getRotation() == BlockRotation.NONE || ghostBlocks.getRotation() == BlockRotation.CLOCKWISE_180 ? ghostBlocks.getBounds().getBlockCountZ() : ghostBlocks.getBounds().getBlockCountX();
        if (ghostBlocks.getRotation() == BlockRotation.CLOCKWISE_90)
        {
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90));
            matrixStack.translate(0, 0, -xAxisSize);
        }
        else if (ghostBlocks.getRotation() == BlockRotation.CLOCKWISE_180)
        {
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
            matrixStack.translate(-xAxisSize, 0, -zAxisSize);
        }
        else if (ghostBlocks.getRotation() == BlockRotation.COUNTERCLOCKWISE_90)
        {
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
            matrixStack.translate(-zAxisSize, 0, 0);
        }
        
        if (ghostBlocks.getMirror() == BlockMirror.FRONT_BACK)
        {
            matrixStack.scale(-1.0f, 1.0f, 1.0f);
            matrixStack.translate(-ghostBlocks.getBounds().getBlockCountX(), 0, 0);
        }
        else if (ghostBlocks.getMirror() == BlockMirror.LEFT_RIGHT)
        {
            matrixStack.scale(1.0f, 1.0f, -1.0f);
            matrixStack.translate(0, 0, -ghostBlocks.getBounds().getBlockCountZ());
        }
    }
}