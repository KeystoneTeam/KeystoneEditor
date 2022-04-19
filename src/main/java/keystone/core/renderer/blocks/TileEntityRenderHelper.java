package keystone.core.renderer.blocks;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.jozufozu.flywheel.util.transform.TransformStack;
import keystone.api.Keystone;
import keystone.core.renderer.blocks.buffer.SuperByteBuffer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vector4f;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Iterator;

public class TileEntityRenderHelper
{
    public static void renderTileEntities(World world, Iterable<BlockEntity> customRenderTEs, MatrixStack ms,
                                          VertexConsumerProvider buffer) {
        renderTileEntities(world, null, customRenderTEs, ms, null, buffer);
    }

    public static void renderTileEntities(World world, Iterable<BlockEntity> customRenderTEs, MatrixStack ms,
                                          VertexConsumerProvider buffer, float pt) {
        renderTileEntities(world, null, customRenderTEs, ms, null, buffer, pt);
    }

    public static void renderTileEntities(World world, @Nullable VirtualRenderWorld renderWorld,
                                          Iterable<BlockEntity> customRenderTEs, MatrixStack ms, @Nullable Matrix4f lightTransform, VertexConsumerProvider buffer) {
        renderTileEntities(world, renderWorld, customRenderTEs, ms, lightTransform, buffer,
                AnimationTickHolder.getPartialTicks());
    }

    public static void renderTileEntities(World world, @Nullable VirtualRenderWorld renderWorld,
                                          Iterable<BlockEntity> customRenderTEs, MatrixStack ms, @Nullable Matrix4f lightTransform, VertexConsumerProvider buffer,
                                          float pt) {
        Iterator<BlockEntity> iterator = customRenderTEs.iterator();
        while (iterator.hasNext()) {
            BlockEntity tileEntity = iterator.next();
            if (Backend.canUseInstancing(renderWorld) && InstancedRenderRegistry.shouldSkipRender(tileEntity))
                continue;

            BlockEntityRenderer<BlockEntity> renderer = MinecraftClient.getInstance().getBlockEntityRenderDispatcher().get(tileEntity);
            if (renderer == null) {
                iterator.remove();
                continue;
            }

            BlockPos pos = tileEntity.getPos();
            ms.push();
            TransformStack.cast(ms)
                    .translate(pos);

            try {
                BlockPos lightPos;
                if (lightTransform != null) {
                    Vector4f lightVec = new Vector4f(pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, 1);
                    lightVec.transform(lightTransform);
                    lightPos = new BlockPos(lightVec.getX(), lightVec.getY(), lightVec.getZ());
                } else {
                    lightPos = pos;
                }
                int worldLight = getCombinedLight(world, lightPos, renderWorld, pos);
                renderer.render(tileEntity, pt, ms, buffer, worldLight, OverlayTexture.DEFAULT_UV);

            } catch (Exception e) {
                iterator.remove();
                String message = "BlockEntity " + Registry.BLOCK_ENTITY_TYPE.getKey(tileEntity.getType()).toString() + " could not be rendered virtually.";
                Keystone.LOGGER.error(message, e);
            }

            ms.pop();
        }
    }

    public static int getCombinedLight(World world, BlockPos worldPos, @Nullable VirtualRenderWorld renderWorld,
                                       BlockPos renderWorldPos) {
        int worldLight = WorldRenderer.getLightmapCoordinates(world, worldPos);

        if (renderWorld != null) {
            int renderWorldLight = WorldRenderer.getLightmapCoordinates(renderWorld, renderWorldPos);
            return SuperByteBuffer.maxLight(worldLight, renderWorldLight);
        }

        return worldLight;
    }
}