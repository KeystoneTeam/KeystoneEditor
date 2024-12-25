package keystone.core.mixins.common;

import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PalettedContainer.Data.class)
public interface PalettedContainerDataAccessor<T>
{
    @Mutable
    @Accessor("palette") void setPalette(Palette<T> palette);
}
