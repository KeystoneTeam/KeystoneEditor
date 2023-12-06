package keystone.core.renderer.blocks;

import keystone.api.Keystone;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Iterator;

public class TileEntityRenderHelper
{
    public static void renderTileEntities(World world, Iterable<BlockEntity> customRenderTEs, MatrixStack ms, MatrixStack localTransform, VertexConsumerProvider buffer, float pt)
    {
        Matrix4f matrix = localTransform.peek()
                .getPositionMatrix();

        for (Iterator<BlockEntity> iterator = customRenderTEs.iterator(); iterator.hasNext(); )
        {
            BlockEntity tileEntity = iterator.next();

            BlockEntityRenderer<BlockEntity> renderer = MinecraftClient.getInstance().getBlockEntityRenderDispatcher().get(tileEntity);
            if (renderer == null)
            {
                iterator.remove();
                continue;
            }

            try
            {
                BlockPos pos = tileEntity.getPos();
                ms.push();
                ms.translate(pos.getX(), pos.getY(), pos.getZ());

                Vector4f vec = new Vector4f(pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, 1);
                vec.mul(matrix);
                BlockPos lightPos = BlockPos.ofFloored(vec.x, vec.y, vec.z);
                int worldLight = WorldRenderer.getLightmapCoordinates(world, lightPos);

                renderer.render(tileEntity, pt, ms, buffer, worldLight, OverlayTexture.DEFAULT_UV);
                ms.pop();

            } catch (Exception e)
            {
                iterator.remove();

                String message = "BlockEntity " + Registries.BLOCK_ENTITY_TYPE.getEntry(Registries.BLOCK_ENTITY_TYPE.getKey(tileEntity.getType()).get()).get().getKey().get().getValue().toString() + " didn't want to render while moved.\n";

                Keystone.LOGGER.error(message, e);
                continue;
            }
        }
    }
}