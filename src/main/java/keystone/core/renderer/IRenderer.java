package keystone.core.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.math.Vec3d;

public interface IRenderer
{
    default void quads(VertexFormat vertexFormat)
    {
        begin(VertexFormat.DrawMode.QUADS, vertexFormat);
    }
    default void triangles(VertexFormat vertexFormat)
    {
        begin(VertexFormat.DrawMode.TRIANGLES, vertexFormat);
    }
    default void lines(float lineWidth, VertexFormat vertexFormat)
    {
        begin(VertexFormat.DrawMode.DEBUG_LINES, vertexFormat);
        RenderSystem.lineWidth(lineWidth);
    }
    default void lineStrip(float lineWidth, VertexFormat vertexFormat)
    {
        begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, vertexFormat);
        RenderSystem.lineWidth(lineWidth);
    }

    void begin(VertexFormat.DrawMode drawMode, VertexFormat vertexFormat);
    default IRenderer vertex(Vec3d vertex) { return vertex(vertex.x, vertex.y, vertex.z); }
    IRenderer vertex(double x, double y, double z);
    IRenderer color(Color4f color);
    IRenderer normal(float x, float y, float z);
    IRenderer texture(float u, float v);
    IRenderer light(int uv);
    IRenderer light(int u, int v);
    IRenderer overlay(int uv);
    IRenderer overlay(int u, int v);
    IRenderer next();
    void draw();

    BufferBuilder getBuffer();
}
