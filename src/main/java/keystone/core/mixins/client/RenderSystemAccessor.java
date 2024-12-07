package keystone.core.mixins.client;

import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderSystem.class)
public interface RenderSystemAccessor
{
    @Mutable @Accessor("modelViewStack")
    static void setModelViewStack(Matrix4fStack stack)
    {
        throw new RuntimeException();
    }
}
