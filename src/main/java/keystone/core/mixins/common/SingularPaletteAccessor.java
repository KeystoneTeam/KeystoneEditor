package keystone.core.mixins.common;

import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.world.chunk.PaletteResizeListener;
import net.minecraft.world.chunk.SingularPalette;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SingularPalette.class)
public interface SingularPaletteAccessor<T>
{
    @Mutable
    @Accessor("listener") void setListener(PaletteResizeListener<T> listener);
    
    @Accessor("idList") IndexedIterable<T> getIdList();
    @Accessor("entry") T getEntry();
}
