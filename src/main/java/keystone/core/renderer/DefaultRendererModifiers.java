package keystone.core.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import keystone.core.renderer.interfaces.IRendererModifier;
import net.minecraft.client.render.GameRenderer;

import java.util.function.Supplier;

public class DefaultRendererModifiers
{
    public static final IRendererModifier POSITION_COLOR_SHADER = new IRendererModifier()
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
    public static final IRendererModifier LINES_SHADER = new IRendererModifier()
    {
        @Override
        public void enable()
        {
            RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader);
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
    
    public static class PolygonOffset implements IRendererModifier
    {
        private final int scale;

        public PolygonOffset(int scale)
        {
            this.scale = scale;
        }

        @Override
        public void enable()
        {
            RenderSystem.enablePolygonOffset();
            RenderSystem.polygonOffset(-0.1f * scale, -0.2f * scale);
        }
        @Override
        public void disable()
        {
            RenderSystem.polygonOffset(0, 0);
            RenderSystem.disablePolygonOffset();
        }
    
        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PolygonOffset that = (PolygonOffset) o;
            return scale == that.scale;
        }
        @Override
        public int hashCode()
        {
            return scale;
        }
    }

    public static class ConditionalCull implements IRendererModifier
    {
        private Supplier<Boolean> shouldCull;

        public ConditionalCull(Supplier<Boolean> shouldCull)
        {
            this.shouldCull = shouldCull;
        }

        @Override
        public void enable()
        {
            if (!this.shouldCull.get()) RenderSystem.disableCull();
        }
        @Override
        public void disable()
        {
            RenderSystem.enableCull();
        }
    }
    
    public static class LineWidth implements IRendererModifier
    {
        private final float lineWidth;
        
        public LineWidth(float lineWidth)
        {
            this.lineWidth = lineWidth;
        }
    
        @Override
        public void enable()
        {
            RenderSystem.lineWidth(lineWidth);
        }
    
        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LineWidth that = (LineWidth) o;
            return lineWidth == that.lineWidth;
        }
        @Override
        public int hashCode()
        {
            return Float.hashCode(lineWidth);
        }
    }
}
