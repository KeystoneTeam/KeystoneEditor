package keystone.core.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;
import org.lwjgl.opengl.GL11;

public class DefaultRendererModifiers
{
    public static final IRendererModifier POSITION_COLOR = new IRendererModifier()
    {
        @Override
        public void enable()
        {
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.disableTexture();
        }

        @Override
        public void disable()
        {
            RenderSystem.enableTexture();
        }
    };
    public static final IRendererModifier IGNORE_CULL = new IRendererModifier()
    {
        @Override
        public void enable()
        {
            RenderSystem.disableCull();
        }

        @Override
        public void disable()
        {
            RenderSystem.enableCull();
        }
    };
    public static final IRendererModifier IGNORE_DEPTH = new IRendererModifier()
    {
        @Override
        public void enable()
        {
            RenderSystem.disableDepthTest();
        }

        @Override
        public void disable()
        {
            RenderSystem.enableDepthTest();
        }
    };
    public static final IRendererModifier TRANSLUCENT = new IRendererModifier()
    {
        @Override
        public void enable()
        {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
            RenderSystem.depthMask(false);
        }

        @Override
        public void disable()
        {
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
        }
    };
    public static final IRendererModifier WIREFRAME = new IRendererModifier()
    {
        @Override
        public void enable()
        {
            RenderSystem.polygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        }

        @Override
        public void disable()
        {
            RenderSystem.polygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        }
    };
}
