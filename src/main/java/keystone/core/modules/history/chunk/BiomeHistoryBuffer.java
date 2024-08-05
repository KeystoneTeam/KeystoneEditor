package keystone.core.modules.history.chunk;

import keystone.core.utils.PalettedArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkSection;

public class BiomeHistoryBuffer extends HistoryBuffer<PalettedArray<RegistryEntry<Biome>>, NbtCompound>
{
    private final RegistryWrapper<Biome> registry;
    
    private BiomeHistoryBuffer(RegistryWrapper<Biome> registry, PalettedArray<RegistryEntry<Biome>> old, PalettedArray<RegistryEntry<Biome>> buffer1, PalettedArray<RegistryEntry<Biome>> buffer2)
    {
        super(old, buffer1, buffer2);
        this.registry = registry;
    }
    
    public static BiomeHistoryBuffer createEmpty(World world)
    {
        return new BiomeHistoryBuffer(world.createCommandRegistryWrapper(RegistryKeys.BIOME), null, null, null);
    }
    public static BiomeHistoryBuffer createFromChunkSection(World world, ChunkSection section)
    {
        PalettedArray<RegistryEntry<Biome>> old = PalettedArray.fromContainer(section.getBiomeContainer(), 4, 4, 4);
        PalettedArray<RegistryEntry<Biome>> buffer1 = old.copy();
        PalettedArray<RegistryEntry<Biome>> buffer2 = old.copy();
        return new BiomeHistoryBuffer(world.createCommandRegistryWrapper(RegistryKeys.BIOME), old, buffer1, buffer2);
    }
    public static BiomeHistoryBuffer createFilled(World world, RegistryKey<Biome> fill)
    {
        RegistryWrapper<Biome> registry = world.createCommandRegistryWrapper(RegistryKeys.BIOME);
        RegistryEntry<Biome> fillBiome = registry.getOrThrow(fill);
        
        PalettedArray<RegistryEntry<Biome>> old = new PalettedArray<>(64, 1, fillBiome);
        PalettedArray<RegistryEntry<Biome>> buffer1 = old.copy();
        PalettedArray<RegistryEntry<Biome>> buffer2 = old.copy();
        return new BiomeHistoryBuffer(registry, old, buffer1, buffer2);
    }
    
    @Override
    protected NbtCompound writeBuffer(PalettedArray<RegistryEntry<Biome>> buffer)
    {
        return buffer.serialize(biome -> NbtString.of(biome.getKey().get().getValue().toString()));
    }
    
    @Override
    protected PalettedArray<RegistryEntry<Biome>> readBuffer(NbtCompound nbt)
    {
        return new PalettedArray<>(nbt, serialized ->
        {
            Identifier identifier = Identifier.of(serialized.asString());
            return registry.getOrThrow(RegistryKey.of(RegistryKeys.BIOME, identifier));
        });
    }
    
    @Override
    protected PalettedArray<RegistryEntry<Biome>> copyBuffer(PalettedArray<RegistryEntry<Biome>> buffer)
    {
        return buffer.copy();
    }
}
