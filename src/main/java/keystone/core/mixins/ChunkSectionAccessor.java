package keystone.core.mixins;

import net.minecraft.class_7522;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkSection.class)
public interface ChunkSectionAccessor
{
    @Accessor("biomeContainer")
    void setBiomeStorage(class_7522<RegistryEntry<Biome>> biomeContainer);
}
