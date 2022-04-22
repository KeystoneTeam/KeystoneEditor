package keystone.core.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import keystone.core.renderer.interfaces.IRenderer;
import keystone.core.renderer.interfaces.IRendererModifier;
import keystone.core.renderer.overlay.ComplexOverlayRenderer;
import keystone.core.renderer.overlay.FillOverlayRenderer;
import keystone.core.renderer.overlay.IOverlayRenderer;
import keystone.core.renderer.overlay.WireframeOverlayRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class RendererFactory
{
    public static class RendererBuilder implements IRenderer
    {
        private Tessellator tessellator;
        private BufferBuilder buffer;
        private Camera camera;
        private List<IRendererModifier> modifiers;

        private boolean began;
        private boolean quads;

        private RendererBuilder(IRendererModifier... modifiers)
        {
            this.tessellator = RenderSystem.renderThreadTesselator();
            this.buffer = this.tessellator.getBuffer();

            this.modifiers = new ArrayList<>(List.of(modifiers));
        }

        public RendererBuilder translucent()
        {
            this.modifiers.add(DefaultRendererModifiers.TRANSLUCENT);
            return this;
        }
        public RendererBuilder ignoreDepth()
        {
            this.modifiers.add(DefaultRendererModifiers.IGNORE_DEPTH);
            return this;
        }
        public RendererBuilder ignoreCull()
        {
            this.modifiers.add(DefaultRendererModifiers.IGNORE_CULL);
            return this;
        }
        public RendererBuilder withModifier(IRendererModifier modifier)
        {
            this.modifiers.add(modifier);
            return this;
        }

        public IOverlayRenderer buildFill()
        {
            return new FillOverlayRenderer(this);
        }
        public IOverlayRenderer buildWireframe()
        {
            return buildWireframe(2.0f);
        }
        public IOverlayRenderer buildWireframe(float lineWidth)
        {
            return new WireframeOverlayRenderer(this, lineWidth);
        }

        @Override
        public void begin(VertexFormat.DrawMode drawMode, VertexFormat vertexFormat)
        {
            this.camera = MinecraftClient.getInstance().gameRenderer.getCamera();
            this.began = true;
            this.quads = drawMode == VertexFormat.DrawMode.QUADS;

            RenderSystem.applyModelViewMatrix();
            RenderSystem.enableDepthTest();
            for (IRendererModifier modifier : modifiers) modifier.enable();

            buffer.begin(drawMode, vertexFormat);
        }

        @Override
        public IRenderer vertex(double x, double y, double z)
        {
            this.buffer.vertex(x - camera.getPos().x, y - camera.getPos().y, z - camera.getPos().z);
            return this;
        }

        @Override
        public IRenderer color(Color4f color)
        {
            this.buffer.color(color.r, color.g, color.b, color.a);
            return this;
        }

        @Override
        public IRenderer normal(float x, float y, float z)
        {
            this.buffer.normal(x, y, z);
            return this;
        }

        @Override
        public IRenderer texture(float u, float v)
        {
            this.buffer.texture(u, v);
            return this;
        }

        @Override
        public IRenderer light(int uv)
        {
            this.buffer.light(uv);
            return this;
        }

        @Override
        public IRenderer light(int u, int v)
        {
            this.buffer.light(u, v);
            return this;
        }

        @Override
        public IRenderer overlay(int uv)
        {
            this.buffer.overlay(uv);
            return this;
        }

        @Override
        public IRenderer overlay(int u, int v)
        {
            this.buffer.overlay(u, v);
            return this;
        }

        @Override
        public IRenderer next()
        {
            this.buffer.next();
            return this;
        }

        @Override
        public void draw()
        {
            began = false;
            if (quads) buffer.sortFrom((float)camera.getPos().x, (float)camera.getPos().y, (float)camera.getPos().z);

            tessellator.draw();
            for (IRendererModifier modifier : modifiers) modifier.disable();
        }

        @Override
        public BufferBuilder getBuffer()
        {
            return null;
        }
    }

    public static RendererBuilder createWireframeOverlay()
    {
        return new RendererBuilder(DefaultRendererModifiers.LINES_SHADER, DefaultRendererModifiers.TRANSLUCENT, DefaultRendererModifiers.IGNORE_CULL, new DefaultRendererModifiers.PolygonOffset(1));
    }
    public static RendererBuilder createSmartPolygonOverlay(Supplier<Boolean> cullingCondition)
    {
        return new RendererBuilder(DefaultRendererModifiers.POSITION_COLOR_SHADER, DefaultRendererModifiers.TRANSLUCENT, new DefaultRendererModifiers.ConditionalCull(cullingCondition), new DefaultRendererModifiers.PolygonOffset(1));
    }
    public static RendererBuilder createPolygonOverlay()
    {
        return new RendererBuilder(DefaultRendererModifiers.POSITION_COLOR_SHADER, DefaultRendererModifiers.TRANSLUCENT, DefaultRendererModifiers.IGNORE_CULL, new DefaultRendererModifiers.PolygonOffset(1));
    }

    public static ComplexOverlayRenderer createComplexOverlay(IOverlayRenderer fillRenderer, IOverlayRenderer wireframeRenderer)
    {
        return new ComplexOverlayRenderer(ComplexOverlayRenderer.DrawMode.FILL, Map.of(ComplexOverlayRenderer.DrawMode.FILL, fillRenderer, ComplexOverlayRenderer.DrawMode.WIREFRAME, wireframeRenderer));
    }
}
