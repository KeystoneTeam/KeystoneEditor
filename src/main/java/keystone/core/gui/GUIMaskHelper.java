package keystone.core.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import keystone.api.Keystone;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;

public final class GUIMaskHelper
{
    private static int nextStencilChannel = 0;
    
    public static void addMask(MatrixStack stack, int x, int y, int width, int height)
    {
        if (nextStencilChannel > 255)
        {
            Keystone.LOGGER.warn("Exceeded maximum stencil channel! Be sure you are ending masks.");
            nextStencilChannel <<= 1;
            return;
        }

        RenderSystem.clearStencil(0);
        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, false);
        GL11.glEnable(GL11.GL_STENCIL_TEST);
    
        RenderSystem.stencilFunc(GL11.GL_NEVER, nextStencilChannel, nextStencilChannel);
        RenderSystem.stencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.stencilMask(nextStencilChannel);
        RenderSystem.colorMask(false, false, false, false);
        RenderSystem.depthMask(false);
    
        DrawableHelper.fill(stack, x, y, x + width, y + height, 0xFFFFFFFF);
    
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthMask(true);
        RenderSystem.stencilMask(0x00);
        RenderSystem.stencilFunc(GL11.GL_EQUAL, nextStencilChannel, nextStencilChannel);

        nextStencilChannel <<= 1;
    }
    public static void endMask()
    {
        if (nextStencilChannel <= 1)
        {
            Keystone.LOGGER.warn("Exceeded minimum stencil channel! Be sure you are starting masks.");
            nextStencilChannel = 1;
            return;
        }

        RenderSystem.clearStencil(0);
        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, false);
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        nextStencilChannel >>= 1;
    }
}