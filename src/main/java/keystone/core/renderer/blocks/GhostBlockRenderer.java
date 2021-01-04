package keystone.core.renderer.blocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.core.renderer.client.Camera;
import keystone.core.renderer.common.models.Coords;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.HashMap;
import java.util.Map;

public class GhostBlockRenderer
{
    private BlockRendererDispatcher blockRenderer;
    private IRenderTypeBuffer buffer;
    private Map<BlockPos, BlockState> blocks = new HashMap<>();

    public GhostBlockRenderer()
    {
        blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
        buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
    }

    public void setBlock(BlockPos pos, BlockState block)
    {
        blocks.put(pos, block);
    }

    public void render(MatrixStack stack, Coords offset)
    {
        stack.push();

        stack.translate(-Camera.getX() + offset.getX(), -Camera.getY() + offset.getY(), -Camera.getZ() + offset.getZ());
        blocks.forEach((pos, block) -> renderBlock(stack, pos, block));

        stack.pop();
    }
    private void renderBlock(MatrixStack stack, BlockPos pos, BlockState block)
    {
        stack.push();
        stack.translate(pos.getX(), pos.getY(), pos.getZ());
        blockRenderer.renderBlock(block, stack, buffer, 15728880, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
        stack.pop();
    }
}
