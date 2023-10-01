package keystone.core.renderer;

import keystone.core.renderer.interfaces.IRendererModifier;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public record RendererProperties(VertexFormat.DrawMode drawMode, VertexFormat vertexFormat, List<IRendererModifier> modifiers)
{
    public RendererProperties(VertexFormat.DrawMode drawMode, VertexFormat vertexFormat, IRendererModifier... modifiers)
    {
        this(drawMode, vertexFormat, new ArrayList<>());
        Collections.addAll(modifiers(), modifiers);
    }
    
    public RendererProperties copy(VertexFormat.DrawMode drawMode) { return new RendererProperties(drawMode, vertexFormat, modifiers); }
    public RendererProperties deepCopy(VertexFormat.DrawMode drawMode) { return new RendererProperties(drawMode, vertexFormat, new ArrayList<>(modifiers)); }
    
    // region Presets
    public static RendererProperties createWireframe(float lineWidth)
    {
        return new RendererProperties(VertexFormat.DrawMode.LINES, VertexFormats.LINES)
                .withModifier(DefaultRendererModifiers.LINES_SHADER)
                .translucent()
                .ignoreCull()
                .ignoreFog()
                .depthOffset(2)
                .lineWidth(lineWidth);
    }
    public static RendererProperties createFill()
    {
        return new RendererProperties(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
                .withModifier(DefaultRendererModifiers.POSITION_COLOR_SHADER)
                .translucent()
                .ignoreCull()
                .ignoreFog()
                .depthOffset(2);
    }
    public static RendererProperties createFill(Supplier<Boolean> cullingCondition)
    {
        return new RendererProperties(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
                .withModifier(DefaultRendererModifiers.POSITION_COLOR_SHADER)
                .translucent()
                .ignoreFog()
                .conditionalCull(cullingCondition)
                .depthOffset(2);
    }
    //endregion
    //region Modifiers
    public RendererProperties translucent() { return withModifier(DefaultRendererModifiers.TRANSLUCENT); }
    public RendererProperties ignoreDepth() { return withModifier(DefaultRendererModifiers.IGNORE_DEPTH); }
    public RendererProperties ignoreCull() { return withModifier(DefaultRendererModifiers.IGNORE_CULL); }
    public RendererProperties ignoreFog() { return withModifier(DefaultRendererModifiers.IGNORE_FOG); }
    public RendererProperties depthOffset(int scale) { return withModifier(new DefaultRendererModifiers.PolygonOffset(scale)); }
    public RendererProperties conditionalCull(Supplier<Boolean> shouldCull) { return withModifier(new DefaultRendererModifiers.ConditionalCull(shouldCull)); }
    public RendererProperties lineWidth(float lineWidth) { return withModifier(new DefaultRendererModifiers.LineWidth(lineWidth)); }
    
    public RendererProperties withModifier(IRendererModifier modifier)
    {
        if (!this.modifiers.contains(modifier)) this.modifiers.add(modifier);
        return this;
    }
    //endregion
    //region Equals and HashCode
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RendererProperties that = (RendererProperties) o;
        return drawMode == that.drawMode && vertexFormat.equals(that.vertexFormat) && modifiers.equals(that.modifiers);
    }
    @Override
    public int hashCode()
    {
        return Objects.hash(drawMode, vertexFormat, modifiers);
    }
    //endregion
}