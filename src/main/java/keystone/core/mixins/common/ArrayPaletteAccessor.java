package keystone.core.mixins.common;

import net.minecraft.world.chunk.ArrayPalette;
import net.minecraft.world.chunk.PaletteResizeListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ArrayPalette.class)
public interface ArrayPaletteAccessor<T>
{
    @Mutable
    @Accessor("listener") void setListener(PaletteResizeListener<T> listener);
}
