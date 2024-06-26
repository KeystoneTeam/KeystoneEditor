package keystone.core.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import keystone.core.renderer.interfaces.IRendererModifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix3f;
import org.joml.Vector3f;

public class ShapeRenderer
{
    private final Tessellator tessellator;
    private final BufferBuilder buffer;
    private final VertexBuffer bufferUpload;
    private final Camera camera;
    
    private final RendererProperties properties;
    
    /**
     * Create a ShapeRenderer. This will create a new {@link Tessellator} for the renderer, so avoid
     * calling this by itself. Instead, use {@link ShapeRenderers#getOrCreate(RendererProperties)}
     * @param properties The {@link RendererProperties properties} of this renderer
     */
    public ShapeRenderer(RendererProperties properties)
    {
        this.tessellator = new Tessellator();
        this.buffer = tessellator.getBuffer();
        this.bufferUpload = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
        this.camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        this.properties = properties;
    }
    
    public void begin()
    {
        buffer.begin(properties.drawMode(), properties.vertexFormat());
    }
    public void end()
    {
        // Configure Rendering
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();
        for (IRendererModifier modifier : properties.modifiers()) modifier.enable();
    
        // Sort and Draw
        buffer.setSorter(VertexSorter.byDistance((float)camera.getPos().x, (float)camera.getPos().y, (float)camera.getPos().z));
        tessellator.draw();
        
        // Disable the Modifiers
        for (IRendererModifier modifier : properties.modifiers()) modifier.disable();
    }
    public BufferBuilder getBuffer() { return this.buffer; }
    
    public ShapeRenderer vertex(Vec3d vertex) { return vertex(vertex.x, vertex.y, vertex.z); }
    public ShapeRenderer normal(Vector3f normal) { return normal(normal.x, normal.y, normal.z); }
    public ShapeRenderer normal(Vec3d normal) { return normal((float)normal.x, (float)normal.y, (float)normal.z); }
    public ShapeRenderer normal(Matrix3f transform, Vec3d normal)
    {
        Vector3f vec = new Vector3f((float)normal.x, (float)normal.y, (float)normal.z);
        vec.mul(transform);
        normal(vec.x, vec.y, vec.z);
        return this;
    }
    
    public ShapeRenderer vertex(double x, double y, double z) { buffer.vertex(x - camera.getPos().x, y - camera.getPos().y, z - camera.getPos().z); return this; }
    public ShapeRenderer color(Color4f color) { buffer.color(color.r, color.g, color.b, color.a); return this; }
    public ShapeRenderer normal(float x, float y, float z) { buffer.normal(x, y, z); return this; }
    public ShapeRenderer texture(float u, float v) { buffer.texture(u, v); return this; }
    public ShapeRenderer light(int uv) { buffer.light(uv); return this; }
    public ShapeRenderer light(int u, int v) { buffer.light(u, v); return this; }
    public ShapeRenderer overlay(int uv) { buffer.overlay(uv); return this; }
    public ShapeRenderer overlay(int u, int v) { buffer.overlay(u, v); return this; }
    public ShapeRenderer next() { buffer.next(); return this; }
}
