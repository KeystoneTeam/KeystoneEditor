package keystone.core.mixins.common;

import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.ReadableContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkSection.class)
public interface ChunkSectionAccessor
{
    @Mutable
    @Accessor("blockStateContainer")
    void setBlockStateContainer(PalettedContainer<BlockState> blockStateContainer);
    
    @Accessor("biomeContainer")
    void setBiomeContainer(ReadableContainer<RegistryEntry<Biome>> biomeContainer);
}
