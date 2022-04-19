package keystone.core.renderer;

public interface IRendererModifier
{
    void enable();
    default void disable() {}
}
