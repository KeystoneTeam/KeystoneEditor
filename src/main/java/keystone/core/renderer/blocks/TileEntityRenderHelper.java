package keystone.core.renderer.blocks;

import java.util.Iterator;

import com.mojang.blaze3d.matrix.MatrixStack;

import keystone.api.Keystone;
import keystone.core.renderer.blocks.world.PlacementSimulationWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class TileEntityRenderHelper
{
    public static void renderTileEntities(World world, Iterable<TileEntity> customRenderTEs, MatrixStack ms,
                                          MatrixStack localTransform, IRenderTypeBuffer buffer, float pt)
    {
        renderTileEntities(world, null, customRenderTEs, ms, localTransform, buffer, pt);
    }

    public static void renderTileEntities(World world, PlacementSimulationWorld renderWorld,
                                          Iterable<TileEntity> customRenderTEs, MatrixStack ms, MatrixStack localTransform, IRenderTypeBuffer buffer,
                                          float pt)
    {
        Matrix4f matrix = localTransform.last()
                .pose();

        for (Iterator<TileEntity> iterator = customRenderTEs.iterator(); iterator.hasNext(); )
        {
            TileEntity tileEntity = iterator.next();
            // if (tileEntity instanceof IInstanceRendered) continue; // TODO: some things still need to render

            TileEntityRenderer<TileEntity> renderer = TileEntityRendererDispatcher.instance.getRenderer(tileEntity);
            if (renderer == null)
            {
                iterator.remove();
                continue;
            }

            try
            {
                BlockPos pos = tileEntity.getBlockPos();
                ms.pushPose();
                ms.translate(pos.getX(), pos.getY(), pos.getZ());

                Vector4f vec = new Vector4f(pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, 1);
                vec.transform(matrix);
                BlockPos lightPos = new BlockPos(vec.x(), vec.y(), vec.z());
                int worldLight = getLight(world, renderWorld, pos, lightPos);

                renderer.render(tileEntity, pt, ms, buffer, worldLight, OverlayTexture.NO_OVERLAY);
                ms.popPose();

            } catch (Exception e)
            {
                iterator.remove();

                String message = "TileEntity " + tileEntity.getType()
                        .getRegistryName()
                        .toString() + " didn't want to render while moved.\n";

                Keystone.LOGGER.error(message, e);
                continue;
            }
        }
    }

    private static int getLight(World world, PlacementSimulationWorld renderWorld, BlockPos pos, BlockPos lightPos)
    {
        int worldLight = WorldRenderer.getLightColor(world, lightPos);
        if (renderWorld != null) return getMaxBlockLight(worldLight, renderWorld.getBrightness(LightType.BLOCK, pos));
        return worldLight;
    }
    private static int getMaxBlockLight(int packedLight, int blockLightValue)
    {
        int unpackedBlockLight = LightTexture.block(packedLight);
        if (blockLightValue > unpackedBlockLight) packedLight = (packedLight & 0xFFFF0000) | (blockLightValue << 4);
        return packedLight;
    }
}