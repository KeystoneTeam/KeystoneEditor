package keystone.core.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import keystone.api.Keystone;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public final class GUIMaskStack
{
    private record MaskBox(DrawContext context, int x, int y, int width, int height) { }
    
    private static final Stack<List<MaskBox>> masks = new Stack<>();
    
    public static void push(boolean empty)
    {
        if (!empty && masks.size() > 0) masks.push(new ArrayList<>(masks.peek()));
        else masks.push(new ArrayList<>());
        recreateScissorBuffer();
    }
    public static void pop()
    {
        if (masks.size() > 0) masks.pop();
        recreateScissorBuffer();
    }
    
    public static void addMaskBox(DrawContext context, int x, int y, int width, int height)
    {
        if (masks.size() == 0)
        {
            Keystone.LOGGER.warn("Trying to call GUIMaskStack.addMask without an active stencil set! Did you remember to call GUIMaskStack.push?");
            push(true);
        }
        
        MaskBox maskBox = new MaskBox(context, x, y, width, height);
        masks.peek().add(maskBox);
        recreateScissorBuffer();
    }
    
    private static void recreateScissorBuffer()
    {
        RenderSystem.clearStencil(0);
        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, false);
    
        if (masks.size() > 0 && masks.peek().size() > 0)
        {
            List<MaskBox> maskFrame = masks.peek();
    
            GL11.glEnable(GL11.GL_STENCIL_TEST);
            RenderSystem.stencilFunc(GL11.GL_NEVER, 0xFF, 0xFF);
            RenderSystem.stencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
            RenderSystem.colorMask(false, false, false, false);
            RenderSystem.depthMask(false);
            
            for (MaskBox mask : maskFrame) mask.context.fill(mask.x, mask.y, mask.x + mask.width, mask.y + mask.height, 0xFFFFFFFF);
            
            RenderSystem.depthMask(true);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            RenderSystem.stencilFunc(GL11.GL_EQUAL, 0xFF, 0xFF);
        }
        else GL11.glDisable(GL11.GL_STENCIL_TEST);
    }
    
    //private static int pauseHandles = 0;
    //private static int nextStencilChannel = 0;
    //
    //public static void addMask(MatrixStack stack, int x, int y, int width, int height)
    //{
    //    if (nextStencilChannel > 255)
    //    {
    //        Keystone.LOGGER.warn("Exceeded maximum stencil channel! Be sure you are ending masks.");
    //        nextStencilChannel <<= 1;
    //        return;
    //    }
    //
    //    RenderSystem.clearStencil(0);
    //    RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, false);
    //    GL11.glEnable(GL11.GL_STENCIL_TEST);
    //
    //    RenderSystem.stencilFunc(GL11.GL_NEVER, nextStencilChannel, nextStencilChannel);
    //    RenderSystem.stencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
    //    RenderSystem.stencilMask(nextStencilChannel);
    //    RenderSystem.colorMask(false, false, false, false);
    //    RenderSystem.depthMask(false);
    //
    //    DrawableHelper.fill(stack, x, y, x + width, y + height, 0xFFFFFFFF);
    //
    //    RenderSystem.colorMask(true, true, true, true);
    //    RenderSystem.depthMask(true);
    //    RenderSystem.stencilMask(0x00);
    //    RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
    //    RenderSystem.stencilFunc(GL11.GL_EQUAL, nextStencilChannel, nextStencilChannel);
    //
    //    nextStencilChannel <<= 1;
    //    pauseHandles = 0;
    //}
    //public static void endMask()
    //{
    //    if (nextStencilChannel <= 1)
    //    {
    //        Keystone.LOGGER.warn("Exceeded minimum stencil channel! Be sure you are starting masks.");
    //        nextStencilChannel = 1;
    //        return;
    //    }
    //
    //    RenderSystem.clearStencil(0);
    //    RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, false);
    //    GL11.glDisable(GL11.GL_STENCIL_TEST);
    //    nextStencilChannel >>= 1;
    //    pauseHandles = 0;
    //}
    //
    //public static void pauseMask()
    //{
    //    if (pauseHandles <= 0)
    //    {
    //        GL11.glDisable(GL11.GL_STENCIL_TEST);
    //        pauseHandles = 1;
    //    }
    //    else pauseHandles++;
    //}
    //public static void resumeMask()
    //{
    //    pauseHandles--;
    //    if (pauseHandles == 0) GL11.glEnable(GL11.GL_STENCIL_TEST);
    //}
}