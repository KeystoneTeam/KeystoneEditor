package keystone.core.renderer.blocks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import keystone.core.renderer.blocks.buffer.SuperByteBuffer;
import keystone.core.renderer.blocks.buffer.SuperRenderTypeBuffer;
import keystone.core.renderer.blocks.world.GhostBlocksWorld;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;

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
        if (mc.world == null || mc.player == null || !changed)
            return;

        redraw(mc);
        changed = false;
    }

    public void render(MatrixStack ms, SuperRenderTypeBuffer buffer, float partialTicks)
    {
        ms.push();
        ms.translate(offset.x, offset.y, offset.z);

        buffer.getBuffer(RenderType.getSolid());
        for (RenderType layer : RenderType.getBlockRenderTypes())
        {
            if (!usedBlockRenderLayers.contains(layer))
                continue;
            SuperByteBuffer superByteBuffer = bufferCache.get(layer);
            superByteBuffer.renderInto(ms, buffer.getBuffer(layer));
        }
        TileEntityRenderHelper.renderTileEntities(ghostBlocks, ghostBlocks.getRenderedTileEntities(), ms, new MatrixStack(),
                buffer, partialTicks);

        ms.pop();
    }

    protected void redraw(Minecraft minecraft)
    {
        usedBlockRenderLayers.clear();
        startedBufferBuilders.clear();

        final GhostBlocksWorld blockAccess = ghostBlocks;
        final BlockRendererDispatcher blockRendererDispatcher = minecraft.getBlockRendererDispatcher();

        List<BlockState> blockstates = new LinkedList<>();
        Map<RenderType, BufferBuilder> buffers = new HashMap<>();
        MatrixStack ms = new MatrixStack();

        BlockPos.getAllInBox(blockAccess.getBounds())
                .forEach(localPos ->
                {
                    ms.push();
                    ms.translate(localPos.getX(), localPos.getY(), localPos.getZ());
                    BlockState state = blockAccess.getBlockState(localPos);

                    for (RenderType blockRenderLayer : RenderType.getBlockRenderTypes())
                    {
                        if (!RenderTypeLookup.canRenderInLayer(state, blockRenderLayer))
                            continue;
                        ForgeHooksClient.setRenderLayer(blockRenderLayer);
                        if (!buffers.containsKey(blockRenderLayer))
                            buffers.put(blockRenderLayer, new BufferBuilder(DefaultVertexFormats.BLOCK.getIntegerSize()));

                        BufferBuilder bufferBuilder = buffers.get(blockRenderLayer);
                        if (startedBufferBuilders.add(blockRenderLayer))
                            bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

                        TileEntity tileEntity = blockAccess.getTileEntity(localPos);

                        if (blockRendererDispatcher.renderModel(state, localPos, blockAccess, ms, bufferBuilder, true,
                                minecraft.world.rand,
                                tileEntity != null ? tileEntity.getModelData() : EmptyModelData.INSTANCE))
                        {
                            usedBlockRenderLayers.add(blockRenderLayer);
                        }
                        blockstates.add(state);
                    }

                    ForgeHooksClient.setRenderLayer(null);
                    ms.pop();
                });

        // finishDrawing
        for (RenderType layer : RenderType.getBlockRenderTypes())
        {
            if (!startedBufferBuilders.contains(layer))
                continue;
            BufferBuilder buf = buffers.get(layer);
            buf.finishDrawing();
            bufferCache.put(layer, new SuperByteBuffer(buf));
        }
    }

    private static int getLayerCount()
    {
        return RenderType.getBlockRenderTypes()
                .size();
    }
}