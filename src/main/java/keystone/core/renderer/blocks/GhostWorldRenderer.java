package keystone.core.renderer.blocks;

import keystone.core.renderer.blocks.buffer.SuperByteBuffer;
import keystone.core.renderer.blocks.buffer.SuperRenderTypeBuffer;
import keystone.core.renderer.blocks.world.GhostBlocksWorld;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GhostWorldRenderer
{
    private final MinecraftClient minecraft;
    private final Map<RenderLayer, SuperByteBuffer> bufferCache = new HashMap<>(getLayerCount());
    private final Set<RenderLayer> usedBlockRenderLayers = new HashSet<>(getLayerCount());
    private final Set<RenderLayer> startedBufferBuilders = new HashSet<>(getLayerCount());
    private final Map<RenderLayer, Map<ChunkPos, SuperByteBuffer>> fluidBufferCache = new HashMap<>(getLayerCount());
    private final Set<RenderLayer> usedFluidRenderLayers = new HashSet<>(getLayerCount());
    private final Map<RenderLayer, Set<ChunkPos>> startedFluidBufferBuilders = new HashMap<>(getLayerCount());
    private boolean changed;

    protected GhostBlocksWorld ghostBlocks;

    public Vec3d offset;

    public GhostWorldRenderer()
    {
        minecraft = MinecraftClient.getInstance();
        changed = false;
        offset = Vec3d.ZERO;
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
        if (mc.world == null || mc.player == null || !changed)
            return;

        redraw(mc);
        changed = false;
    }

    public void render(MatrixStack ms, SuperRenderTypeBuffer buffer, float partialTicks)
    {
        ms.push();
        ms.translate(offset.x, offset.y, offset.z);

        // Apply Ghost World Orientation to MatrixStack
        int xAxisSize = ghostBlocks.getRotation() == BlockRotation.NONE || ghostBlocks.getRotation() == BlockRotation.CLOCKWISE_180 ? ghostBlocks.getBounds().getBlockCountX() : ghostBlocks.getBounds().getBlockCountZ();
        int zAxisSize = ghostBlocks.getRotation() == BlockRotation.NONE || ghostBlocks.getRotation() == BlockRotation.CLOCKWISE_180 ? ghostBlocks.getBounds().getBlockCountZ() : ghostBlocks.getBounds().getBlockCountX();
        if (ghostBlocks.getRotation() == BlockRotation.CLOCKWISE_90)
        {
            ms.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-90));
            ms.translate(0, 0, -xAxisSize);
        }
        else if (ghostBlocks.getRotation() == BlockRotation.CLOCKWISE_180)
        {
            ms.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180));
            ms.translate(-xAxisSize, 0, -zAxisSize);
        }
        else if (ghostBlocks.getRotation() == BlockRotation.COUNTERCLOCKWISE_90)
        {
            ms.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90));
            ms.translate(-zAxisSize, 0, 0);
        }

        if (ghostBlocks.getMirror() == BlockMirror.FRONT_BACK)
        {
            ms.scale(-1.0f, 1.0f, 1.0f);
            ms.translate(-ghostBlocks.getBounds().getBlockCountX(), 0, 0);
        }
        else if (ghostBlocks.getMirror() == BlockMirror.LEFT_RIGHT)
        {
            ms.scale(1.0f, 1.0f, -1.0f);
            ms.translate(0, 0, -ghostBlocks.getBounds().getBlockCountZ());
        }

        buffer.getBuffer(RenderLayer.getSolid());

        // Dispatch Ghost World Entity Rendering
        EntityRenderDispatcher entityRenderer = minecraft.getEntityRenderDispatcher();
        ghostBlocks.getEntities().forEach(entity ->
        {
            int light = LightmapTextureManager.pack(15, 15);
            // TODO: Check if this needs pitch somehow
            entityRenderer.render(entity, entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), 0, ms, buffer, light);
        });

        // Dispatch Ghost World Fluid Rendering
        for (RenderLayer layer : RenderLayer.getBlockLayers())
        {
            if (!usedFluidRenderLayers.contains(layer)) continue;
            Map<ChunkPos, SuperByteBuffer> fluidChunks = fluidBufferCache.get(layer);
            for (Map.Entry<ChunkPos, SuperByteBuffer> entry : fluidChunks.entrySet())
            {
                ms.push();
                ms.translate(entry.getKey().getStartX(), 0, entry.getKey().getStartZ());
                entry.getValue().renderInto(ms, buffer.getBuffer(layer));
                ms.pop();
            }
        }

        // Dispatch Ghost World Block Rendering
        for (RenderLayer layer : RenderLayer.getBlockLayers())
        {
            if (!usedBlockRenderLayers.contains(layer)) continue;
            SuperByteBuffer superByteBuffer = bufferCache.get(layer);
            superByteBuffer.renderInto(ms, buffer.getBuffer(layer));
        }
        TileEntityRenderHelper.renderTileEntities(ghostBlocks, ghostBlocks.getRenderedTileEntities(), ms, new MatrixStack(), buffer, partialTicks);

        ms.pop();
    }

    // For vanilla implementation, see ChunkBuilder.BuiltChunk.RebuildTask.render()
    protected void redraw(MinecraftClient minecraft)
    {
        usedBlockRenderLayers.clear();
        usedFluidRenderLayers.clear();
        startedBufferBuilders.clear();
        startedFluidBufferBuilders.clear();

        final GhostBlocksWorld blockAccess = ghostBlocks;
        final BlockRenderManager blockRendererDispatcher = minecraft.getBlockRenderManager();

        Map<RenderLayer, BufferBuilder> blockBuffers = new HashMap<>();
        Map<RenderLayer, Map<ChunkPos, BufferBuilder>> fluidBuffers = new HashMap<>();
        MatrixStack ms = new MatrixStack();

        BlockPos.stream(blockAccess.getBounds()).forEach(localPos ->
        {
            ms.push();
            ms.translate(localPos.getX(), localPos.getY(), localPos.getZ());

            ChunkPos chunkPos = new ChunkPos(localPos);
            BlockState blockState = blockAccess.getBlockState(localPos);
            FluidState fluidState = blockState.getFluidState();

            RenderLayer renderLayer;
            BufferBuilder bufferBuilder;

            // Fluid Rendering
            if (!fluidState.isEmpty())
            {
                renderLayer = RenderLayers.getFluidLayer(fluidState);
                if (!fluidBuffers.containsKey(renderLayer)) fluidBuffers.put(renderLayer, new HashMap<>());
                if (!fluidBuffers.get(renderLayer).containsKey(chunkPos)) fluidBuffers.get(renderLayer).put(chunkPos, new BufferBuilder(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getVertexSizeInteger()));
                bufferBuilder = fluidBuffers.get(renderLayer).get(chunkPos);

                if (!startedFluidBufferBuilders.containsKey(renderLayer)) startedFluidBufferBuilders.put(renderLayer, new HashSet<>());
                if (startedFluidBufferBuilders.get(renderLayer).add(chunkPos)) bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);

                // TODO: Check if I need to find a way to re-implement the if statement before adding the layer to usedFluidRendersLayers
                blockRendererDispatcher.renderFluid(localPos, blockAccess, bufferBuilder, blockState, fluidState);
                usedFluidRenderLayers.add(renderLayer);
            }

            // Block Rendering
            if (blockState.getRenderType() != BlockRenderType.INVISIBLE)
            {
                renderLayer = RenderLayers.getBlockLayer(blockState);
                if (!blockBuffers.containsKey(renderLayer)) blockBuffers.put(renderLayer, new BufferBuilder(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getVertexSizeInteger()));
                bufferBuilder = blockBuffers.get(renderLayer);
                if (startedBufferBuilders.add(renderLayer)) bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);

                // TODO: Check if I need to find a way to re-implement the if statement before adding the layer to usedBlockRenderLayers
                blockRendererDispatcher.renderBlock(blockState, localPos, blockAccess, ms, bufferBuilder, true, minecraft.world.random);
                usedBlockRenderLayers.add(renderLayer);
            }

            ms.pop();
        });

        // Finish Drawing Fluids
        for (RenderLayer layer : startedFluidBufferBuilders.keySet())
        {
            Map<ChunkPos, BufferBuilder> chunkBuffers = fluidBuffers.get(layer);
            Map<ChunkPos, SuperByteBuffer> byteBuffers = new HashMap<>();
            for (BufferBuilder buf : chunkBuffers.values()) buf.end();

            for (Map.Entry<ChunkPos, BufferBuilder> entry : chunkBuffers.entrySet()) byteBuffers.put(entry.getKey(), new SuperByteBuffer(entry.getValue()));
            fluidBufferCache.put(layer, byteBuffers);
        }

        // Finish Drawing Blocks
        for (RenderLayer layer : startedBufferBuilders)
        {
            BufferBuilder buf = blockBuffers.get(layer);
            bufferCache.put(layer, new SuperByteBuffer(buf));
        }
    }

    private static int getLayerCount()
    {
        return RenderLayer.getBlockLayers()
                .size();
    }
}