package keystone.core.modules.history.chunk;

import com.mojang.serialization.Codec;
import keystone.api.Keystone;
import keystone.core.utils.PalettedContainerUtils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.ReadableContainer;

public class BiomeHistoryBuffer extends HistoryBuffer<PalettedContainer<RegistryEntry<Biome>>, NbtElement>
{
    private final Codec<ReadableContainer<RegistryEntry<Biome>>> codec;
    
    private BiomeHistoryBuffer(World world, PalettedContainer<RegistryEntry<Biome>> old, PalettedContainer<RegistryEntry<Biome>> buffer1, PalettedContainer<RegistryEntry<Biome>> buffer2)
    {
        super(old, buffer1, buffer2);
        
        Registry<Biome> biomeRegistry = world.getRegistryManager().get(RegistryKeys.BIOME);
        this.codec = PalettedContainer.createReadableContainerCodec(biomeRegistry.getIndexedEntries(), biomeRegistry.getEntryCodec(), PalettedContainer.PaletteProvider.BIOME, biomeRegistry.entryOf(BiomeKeys.PLAINS));
    }
    
    public static BiomeHistoryBuffer createEmpty(World world)
    {
        return new BiomeHistoryBuffer(world, null, null, null);
    }
    public static BiomeHistoryBuffer createFromChunkSection(World world, ChunkSection section)
    {
        PalettedContainer<RegistryEntry<Biome>> old = PalettedContainerUtils.fromReadableContainer(section.getBiomeContainer(), 4, 4, 4);
        PalettedContainer<RegistryEntry<Biome>> buffer1 = PalettedContainerUtils.copyContainer(old);
        PalettedContainer<RegistryEntry<Biome>> buffer2 = PalettedContainerUtils.copyContainer(old);
        return new BiomeHistoryBuffer(world, old, buffer1, buffer2);
    }
    public static BiomeHistoryBuffer createFilled(World world, RegistryKey<Biome> fill)
    {
        Registry<Biome> registry = world.getRegistryManager().get(RegistryKeys.BIOME);
        RegistryEntry<Biome> fillBiome = registry.entryOf(fill);
        
        PalettedContainer<RegistryEntry<Biome>> old = new PalettedContainer<>(registry.getIndexedEntries(), fillBiome, PalettedContainer.PaletteProvider.BIOME);
        PalettedContainer<RegistryEntry<Biome>> buffer1 = new PalettedContainer<>(registry.getIndexedEntries(), fillBiome, PalettedContainer.PaletteProvider.BIOME);
        PalettedContainer<RegistryEntry<Biome>> buffer2 = new PalettedContainer<>(registry.getIndexedEntries(), fillBiome, PalettedContainer.PaletteProvider.BIOME);
        return new BiomeHistoryBuffer(world, old, buffer1, buffer2);
    }
    
    @Override
    protected NbtElement writeBuffer(World world, PalettedContainer<RegistryEntry<Biome>> buffer)
    {
        return codec.encodeStart(NbtOps.INSTANCE, buffer).getOrThrow();
    }
    
    @Override
    protected PalettedContainer<RegistryEntry<Biome>> readBuffer(World world, NbtElement nbt)
    {
        ReadableContainer<RegistryEntry<Biome>> container = codec.parse(NbtOps.INSTANCE, nbt).promotePartial(error -> Keystone.LOGGER.warn("Recoverable error while reading biome history buffer: {}", error)).getOrThrow();
        return PalettedContainerUtils.fromReadableContainer(container, 4, 4, 4);
    }
    
    @Override
    protected PalettedContainer<RegistryEntry<Biome>> copyBuffer(PalettedContainer<RegistryEntry<Biome>> buffer)
    {
        return PalettedContainerUtils.copyContainer(buffer);
    }
}
