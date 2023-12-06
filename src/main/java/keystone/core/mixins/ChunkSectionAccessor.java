package keystone.core.mixins;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ReadableContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkSection.class)
public interface ChunkSectionAccessor
{
    @Accessor("biomeContainer")
    void setBiomeStorage(ReadableContainer<RegistryEntry<Biome>> biomeContainer);
}
