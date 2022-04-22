package keystone.core.renderer.interfaces;

public interface IRendererModifier
{
    void enable();
    default void disable() {}
}
