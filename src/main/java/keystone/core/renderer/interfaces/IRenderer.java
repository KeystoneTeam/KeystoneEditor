package keystone.core.renderer.interfaces;

import com.mojang.blaze3d.systems.RenderSystem;
import keystone.core.renderer.Color4f;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

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
        RenderSystem.lineWidth(lineWidth);
        begin(VertexFormat.DrawMode.LINES, vertexFormat);
    }
    default void lineStrip(float lineWidth, VertexFormat vertexFormat)
    {
        RenderSystem.lineWidth(lineWidth);
        begin(VertexFormat.DrawMode.LINE_STRIP, vertexFormat);
    }

    void begin(VertexFormat.DrawMode drawMode, VertexFormat vertexFormat);
    default IRenderer vertex(Vec3d vertex) { return vertex(vertex.x, vertex.y, vertex.z); }
    default IRenderer normal(Vec3f normal) { return normal(normal.getX(), normal.getY(), normal.getZ()); }
    default IRenderer normal(Vec3d normal) { return normal((float)normal.x, (float)normal.y, (float)normal.z); }
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
