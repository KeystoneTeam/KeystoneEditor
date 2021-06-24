package keystone.core.renderer.blocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.core.renderer.blocks.buffer.SuperByteBuffer;
import keystone.core.renderer.blocks.buffer.SuperRenderTypeBuffer;
import keystone.core.renderer.blocks.world.GhostBlocksWorld;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GhostWorldRenderer
{
    private final Map<RenderType, SuperByteBuffer> bufferCache = new HashMap<>(getLayerCount());
    private final Set<RenderType> usedBlockRenderLayers = new HashSet<>(getLayerCount());
    private final Set<RenderType> startedBufferBuilders = new HashSet<>(getLayerCount());
    private boolean changed;

    protected GhostBlocksWorld ghostBlocks;

    public Vector3d offset;

    public GhostWorldRenderer()
    {
        changed = false;
        offset = Vector3d.ZERO;
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
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || !changed)
            return;

        redraw(mc);
        changed = false;
    }

    public void render(MatrixStack ms, SuperRenderTypeBuffer buffer, float partialTicks)
    {
        ms.pushPose();
        ms.translate(offset.x, offset.y, offset.z);

        // Apply Ghost World Orientation to MatrixStack
        int xAxisSize = ghostBlocks.getRotation() == Rotation.NONE || ghostBlocks.getRotation() == Rotation.CLOCKWISE_180 ? ghostBlocks.getBounds().getXSpan() : ghostBlocks.getBounds().getZSpan();
        int zAxisSize = ghostBlocks.getRotation() == Rotation.NONE || ghostBlocks.getRotation() == Rotation.CLOCKWISE_180 ? ghostBlocks.getBounds().getZSpan() : ghostBlocks.getBounds().getXSpan();
        if (ghostBlocks.getRotation() == Rotation.CLOCKWISE_90)
        {
            ms.mulPose(Vector3f.YP.rotationDegrees(-90));
            ms.translate(0, 0, -xAxisSize);
        }
        else if (ghostBlocks.getRotation() == Rotation.CLOCKWISE_180)
        {
            ms.mulPose(Vector3f.YP.rotationDegrees(180));
            ms.translate(-xAxisSize, 0, -zAxisSize);
        }
        else if (ghostBlocks.getRotation() == Rotation.COUNTERCLOCKWISE_90)
        {
            ms.mulPose(Vector3f.YP.rotationDegrees(90));
            ms.translate(-zAxisSize, 0, 0);
        }
        if (ghostBlocks.getMirror() == Mirror.FRONT_BACK)
        {
            ms.scale(-1.0f, 1.0f, 1.0f);
            ms.translate(-ghostBlocks.getBounds().getXSpan(), 0, 0);
        }
        else if (ghostBlocks.getMirror() == Mirror.LEFT_RIGHT)
        {
            ms.scale(1.0f, 1.0f, -1.0f);
            ms.translate(0, 0, -ghostBlocks.getBounds().getZSpan());
        }

        // Dispatch Ghost World Rendering
        buffer.getBuffer(RenderType.solid());
        for (RenderType layer : RenderType.chunkBufferLayers())
        {
            if (!usedBlockRenderLayers.contains(layer))
                continue;
            SuperByteBuffer superByteBuffer = bufferCache.get(layer);
            superByteBuffer.renderInto(ms, buffer.getBuffer(layer));
        }
        TileEntityRenderHelper.renderTileEntities(ghostBlocks, ghostBlocks.getRenderedTileEntities(), ms, new MatrixStack(),
                buffer, partialTicks);

        ms.popPose();
    }

    protected void redraw(Minecraft minecraft)
    {
        usedBlockRenderLayers.clear();
        startedBufferBuilders.clear();

        final GhostBlocksWorld blockAccess = ghostBlocks;
        final BlockRendererDispatcher blockRendererDispatcher = minecraft.getBlockRenderer();

        Map<RenderType, BufferBuilder> buffers = new HashMap<>();
        MatrixStack ms = new MatrixStack();

        BlockPos.betweenClosedStream(blockAccess.getBounds()).forEach(localPos ->
        {
            BlockState blockState = blockAccess.getBlockState(localPos);
            FluidState fluidState = blockState.getFluidState();

            for (RenderType renderType : RenderType.chunkBufferLayers())
            {
                ForgeHooksClient.setRenderLayer(renderType);

                // Fluid Rendering
                if (!fluidState.isEmpty() && RenderTypeLookup.canRenderInLayer(fluidState, renderType))
                {
                    if (!buffers.containsKey(renderType)) buffers.put(renderType, new BufferBuilder(DefaultVertexFormats.BLOCK.getIntegerSize()));
                    BufferBuilder bufferBuilder = buffers.get(renderType);
                    if (startedBufferBuilders.add(renderType)) bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

                    if (blockRendererDispatcher.renderLiquid(localPos, blockAccess, bufferBuilder, fluidState)) usedBlockRenderLayers.add(renderType);
                }

                // Block Rendering
                if (blockState.getRenderShape() != BlockRenderType.INVISIBLE && RenderTypeLookup.canRenderInLayer(blockState, renderType))
                {
                    if (!buffers.containsKey(renderType)) buffers.put(renderType, new BufferBuilder(DefaultVertexFormats.BLOCK.getIntegerSize()));
                    BufferBuilder bufferBuilder = buffers.get(renderType);
                    if (startedBufferBuilders.add(renderType)) bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

                    TileEntity tileEntity = blockAccess.getBlockEntity(localPos);

                    ms.pushPose();
                    ms.translate(localPos.getX(), localPos.getY(), localPos.getZ());
                    if (blockRendererDispatcher.renderModel(blockState, localPos, blockAccess, ms, bufferBuilder, true, minecraft.level.random,
                            tileEntity != null ? tileEntity.getModelData() : EmptyModelData.INSTANCE))
                    {
                        usedBlockRenderLayers.add(renderType);
                    }
                    ms.popPose();
                }
            }

            ForgeHooksClient.setRenderLayer(null);
        });

        // finishDrawing
        for (RenderType layer : RenderType.chunkBufferLayers())
        {
            if (!startedBufferBuilders.contains(layer))
                continue;
            BufferBuilder buf = buffers.get(layer);
            buf.end();
            bufferCache.put(layer, new SuperByteBuffer(buf));
        }
    }

    private static int getLayerCount()
    {
        return RenderType.chunkBufferLayers()
                .size();
    }
}