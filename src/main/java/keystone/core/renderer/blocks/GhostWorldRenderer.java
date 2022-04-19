package keystone.core.renderer.blocks;

import com.jozufozu.flywheel.core.model.ShadeSeparatedBufferBuilder;
import com.jozufozu.flywheel.core.model.ShadeSeparatingVertexConsumer;
import com.jozufozu.flywheel.fabric.model.CullingBakedModel;
import com.jozufozu.flywheel.fabric.model.FabricModelUtil;
import com.jozufozu.flywheel.fabric.model.LayerFilteringBakedModel;
import keystone.core.renderer.blocks.buffer.SuperByteBuffer;
import keystone.core.renderer.blocks.buffer.SuperRenderTypeBuffer;
import keystone.core.renderer.blocks.world.GhostBlocksWorld;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.*;

import java.util.*;

public class GhostWorldRenderer
{
    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

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

    private void applyOrientation(MatrixStack ms)
    {
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

        redraw(mc);
        changed = false;
    }

    public void render(MatrixStack ms, SuperRenderTypeBuffer buffers, float partialTicks)
    {
        ms.push();
        ms.translate(offset.x, offset.y, offset.z);
        applyOrientation(ms);

        // TODO: See if I can remove this
        //buffers.getBuffer(RenderLayer.getSolid());

        // Dispatch Ghost World Entity Rendering
        EntityRenderDispatcher entityRenderer = minecraft.getEntityRenderDispatcher();
        ghostBlocks.getEntities().forEach(entity ->
        {
            int light = LightmapTextureManager.pack(15, 15);
            entityRenderer.render(entity, entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(partialTicks), entity.getPitch(partialTicks), ms, buffers, light);
        });

        // Render Blocks
        bufferCache.forEach((layer, buffer) -> buffer.renderInto(ms, buffers.getBuffer(layer)));

        // Render Tile Entities
        TileEntityRenderHelper.renderTileEntities(ghostBlocks, ghostBlocks.getRenderedTileEntities(), ms, buffers);

        ms.pop();
    }

    protected void redraw(MinecraftClient minecraft)
    {
        bufferCache.clear();
        for (RenderLayer layer : RenderLayer.getBlockLayers())
        {
            SuperByteBuffer buffer = drawLayer(layer);
            if (!buffer.isEmpty()) bufferCache.put(layer, buffer);
        }
    }

    protected SuperByteBuffer drawLayer(RenderLayer layer)
    {
        BlockRenderManager dispatcher = MinecraftClient.getInstance().getBlockRenderManager();
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

        MatrixStack matrixStack = objects.matrixStack;
        Random random = objects.random;
        BlockPos.Mutable mutablePos = objects.mutablePos;
        GhostBlocksWorld renderWorld = ghostBlocks;
        BlockBox bounds = renderWorld.getBounds();

        ShadeSeparatingVertexConsumer shadeSeparatingWrapper = objects.shadeSeparatingWrapper;
        ShadeSeparatedBufferBuilder builder = new ShadeSeparatedBufferBuilder(512);
        BufferBuilder unshadedBuilder = objects.unshadedBuilder;

        builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
        unshadedBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
        shadeSeparatingWrapper.prepare(builder, unshadedBuilder);

        BlockModelRenderer.enableBrightnessCache();
        for (BlockPos localPos : BlockPos.iterate(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ(), bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ()))
        {
            BlockPos pos = mutablePos.set(localPos);
            BlockState state = renderWorld.getBlockState(pos);

            matrixStack.push();
            matrixStack.translate(localPos.getX(), localPos.getY(), localPos.getZ());

            if (state.getRenderType() == BlockRenderType.MODEL)
            {
                BakedModel model = dispatcher.getModel(state);
                if (((FabricBakedModel)model).isVanillaAdapter())
                {
                    if (!FabricModelUtil.doesLayerMatch(state, layer)) model = null;
                }
                else
                {
                    model = CullingBakedModel.wrap(model);
                    model = LayerFilteringBakedModel.wrap(model, layer);
                    model = shadeSeparatingWrapper.wrapModel(model);
                }

                if (model != null)
                {
                    dispatcher.getModelRenderer().render(renderWorld, model, state, pos, matrixStack, shadeSeparatingWrapper, true, random, state.getRenderingSeed(pos), OverlayTexture.DEFAULT_UV);
                }
            }

            matrixStack.pop();
        }
        BlockModelRenderer.disableBrightnessCache();

        shadeSeparatingWrapper.clear();
        unshadedBuilder.clear();
        builder.appendUnshadedVertices(unshadedBuilder);
        builder.end();

        return new SuperByteBuffer(builder);
    }

    private static int getLayerCount()
    {
        return RenderLayer.getBlockLayers().size();
    }

    private static class ThreadLocalObjects
    {
        public final MatrixStack matrixStack = new MatrixStack();
        public final Random random = new Random();
        public final BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        public final ShadeSeparatingVertexConsumer shadeSeparatingWrapper = new ShadeSeparatingVertexConsumer();
        public final BufferBuilder unshadedBuilder = new BufferBuilder(512);
    }
}