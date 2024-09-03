package keystone.core.renderer.blocks;

import com.mojang.blaze3d.systems.VertexSorter;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import keystone.core.renderer.blocks.buffer.SuperByteBuffer;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.Map;

public class GhostSectionBuilder
{
    private final MinecraftClient client;
    private final BlockBufferAllocatorStorage blockAllocators;
    private final BlockRenderManager blockRenderer;
    private final BlockEntityRenderDispatcher blockEntityRenderer;
    
    public GhostSectionBuilder(MinecraftClient client)
    {
        this.client = client;
        blockAllocators = client.getBufferBuilders().getBlockBufferBuilders();
        blockRenderer = client.getBlockRenderManager();
        blockEntityRenderer = client.getBlockEntityRenderDispatcher();
    }
    
    public BuiltGhostChunkSection build(World world, ChunkSectionPos pos)
    {
        BuiltGhostChunkSection built = new BuiltGhostChunkSection();
        
        Map<RenderLayer, BufferBuilder> builders = new Reference2ObjectArrayMap<>();
        MatrixStack matrixStack = new MatrixStack();
        BlockModelRenderer.enableBrightnessCache();
        Random random = Random.create();
        
        // Iterate Through Blocks
        for (BlockPos globalPos : BlockPos.iterate(pos.getMinX(), pos.getMinY(), pos.getMinZ(), pos.getMaxX(), pos.getMaxY(), pos.getMaxZ()))
        {
            // Get Block Data
            BlockState blockState = world.getBlockState(globalPos);
            
            // Process Block Entity
            BlockEntity blockEntity;
            if (blockState.hasBlockEntity() && (blockEntity = world.getBlockEntity(globalPos)) != null) addBlockEntity(built, blockEntity);
            
            // Process Fluid
            FluidState fluidState = blockState.getFluidState();
            if (!fluidState.isEmpty())
            {
                RenderLayer renderLayer = RenderLayers.getFluidLayer(fluidState);
                BufferBuilder buffer = startBufferBuilding(builders, blockAllocators, renderLayer);
                blockRenderer.renderFluid(globalPos, world, buffer, blockState, fluidState);
            }
            
            // Process BlockState
            if (blockState.getRenderType() == BlockRenderType.MODEL)
            {
                RenderLayer renderLayer = RenderLayers.getBlockLayer(blockState);
                BufferBuilder buffer = startBufferBuilding(builders, blockAllocators, renderLayer);
                matrixStack.push();
                matrixStack.translate(ChunkSectionPos.getLocalCoord(globalPos.getX()), ChunkSectionPos.getLocalCoord(globalPos.getY()), ChunkSectionPos.getLocalCoord(globalPos.getZ()));
                blockRenderer.renderBlock(blockState, globalPos, world, matrixStack, buffer, true, random);
                matrixStack.pop();
            }
        }
        
        // Finalize Layers
        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
        VertexSorter vertexSorter = VertexSorter.byDistance((float)(cameraPos.x - (double)pos.getMinX()), (float)(cameraPos.y - (double)pos.getMinY()), (float)(cameraPos.z - (double)pos.getMinZ()));
        
        for (Map.Entry<RenderLayer, BufferBuilder> layer : builders.entrySet())
        {
            RenderLayer renderLayer = layer.getKey();
            BuiltBuffer builtBuffer = layer.getValue().endNullable();
            if (builtBuffer == null) continue;
            
            if (renderLayer == RenderLayer.getTranslucent()) builtBuffer.sortQuads(blockAllocators.get(RenderLayer.getTranslucent()), vertexSorter);
            built.buffers.put(renderLayer, new SuperByteBuffer(builtBuffer));
        }
        
        // Return Built Chunk Section
        BlockModelRenderer.disableBrightnessCache();
        return built;
    }
    private BufferBuilder startBufferBuilding(Map<RenderLayer, BufferBuilder> buffers, BlockBufferAllocatorStorage allocators, RenderLayer layer)
    {
        BufferBuilder buffer = buffers.get(layer);
        if (buffer == null)
        {
            BufferAllocator allocator = allocators.get(layer);
            buffer = new BufferBuilder(allocator, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
            buffers.put(layer, buffer);
        }
        return buffer;
    }
    private <E extends BlockEntity> void addBlockEntity(BuiltGhostChunkSection chunk, E blockEntity)
    {
        BlockEntityRenderer<E> renderer = blockEntityRenderer.get(blockEntity);
        if (renderer != null) chunk.blockEntities.add(blockEntity);
    }
}
