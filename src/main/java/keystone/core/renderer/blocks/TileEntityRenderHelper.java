package keystone.core.renderer.blocks;

import keystone.api.Keystone;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vector4f;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

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
                vec.transform(matrix);
                BlockPos lightPos = new BlockPos(vec.getX(), vec.getY(), vec.getZ());
                int worldLight = WorldRenderer.getLightmapCoordinates(world, lightPos);

                renderer.render(tileEntity, pt, ms, buffer, worldLight, OverlayTexture.DEFAULT_UV);
                ms.pop();

            } catch (Exception e)
            {
                iterator.remove();

                String message = "BlockEntity " + Registry.BLOCK_ENTITY_TYPE.getEntry(Registry.BLOCK_ENTITY_TYPE.getKey(tileEntity.getType()).get()).get().getKey().get().getValue().toString() + " didn't want to render while moved.\n";

                Keystone.LOGGER.error(message, e);
                continue;
            }
        }
    }
}