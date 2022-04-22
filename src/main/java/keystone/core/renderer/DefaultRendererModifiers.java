package keystone.core.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import keystone.core.renderer.interfaces.IRendererModifier;
import net.minecraft.client.render.GameRenderer;

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
    public static final IRendererModifier POLYGON_OFFSET = new IRendererModifier()
    {
        @Override
        public void enable()
        {
            RenderSystem.enablePolygonOffset();
            RenderSystem.polygonOffset(-0.2f, -0.4f);
        }

        @Override
        public void disable()
        {
            RenderSystem.polygonOffset(0, 0);
            RenderSystem.disablePolygonOffset();
        }
    };
}
