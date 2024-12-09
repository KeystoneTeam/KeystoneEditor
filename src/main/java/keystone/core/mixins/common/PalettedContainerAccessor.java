package keystone.core.mixins.common;

import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PalettedContainer.class)
public interface PalettedContainerAccessor<T>
{
    @Accessor("data") PalettedContainer.Data<T> getData();
}
