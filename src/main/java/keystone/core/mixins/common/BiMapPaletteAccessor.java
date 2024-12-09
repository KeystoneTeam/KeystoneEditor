package keystone.core.mixins.common;

import net.minecraft.world.chunk.BiMapPalette;
import net.minecraft.world.chunk.PaletteResizeListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BiMapPalette.class)
public interface BiMapPaletteAccessor<T>
{
    @Mutable
    @Accessor("listener") void setListener(PaletteResizeListener<T> listener);
}
