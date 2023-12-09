package keystone.api.wrappers;

import keystone.core.mixins.BiomeInvoker;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

/**
 * A wrapper for a Minecraft biome.
 */
public class Biome
{
    private static final List<Biome> LAZY_CONSTRUCTED = Lists.newArrayList();
    
    private RegistryKey<net.minecraft.world.biome.Biome> key;
    private RegistryEntry<net.minecraft.world.biome.Biome> biome;
    private Identifier id;
    private Text name;

    //region INTERNAL USE ONLY, DO NOT USE IN FILTERS
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Lazily constructs a Biome wrapper. Cannot be used until
     * @param key The Minecraft Biome's registry key
     */
    private Biome(RegistryKey<net.minecraft.world.biome.Biome> key)
    {
        this.key = key;
        this.id = key.getValue();
        this.name = Text.translatable("biome." + this.id.getNamespace() + "." + this.id.getPath());
    }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * @param minecraftBiome The Minecraft Biome
     */
    public Biome(net.minecraft.registry.entry.RegistryEntry<net.minecraft.world.biome.Biome> minecraftBiome)
    {
        this.key = minecraftBiome.getKey().get();
        this.biome = minecraftBiome;
        this.id = minecraftBiome.getKey().get().getValue();
        this.name = Text.translatable("biome." + this.id.getNamespace() + "." + this.id.getPath());
    }
    
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * @param key The Minecraft Biome's registry key
     * @return A lazy-constructed Biome wrapper
     */
    public static Biome lazyConstruct(RegistryKey<net.minecraft.world.biome.Biome> key)
    {
        Biome biome = new Biome(key);
        LAZY_CONSTRUCTED.add(biome);
        return biome;
    }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * Looks up the biome corresponding to each lazy constructed Biome wrapper
     * @param world The World to use when finalizing construction
     */
    public static void finalizeLazyConstruction(World world)
    {
        RegistryWrapper<net.minecraft.world.biome.Biome> biomeLookup = world.createCommandRegistryWrapper(RegistryKeys.BIOME);
        for (Biome lazyConstructed : LAZY_CONSTRUCTED) lazyConstructed.biome = biomeLookup.getOrThrow(lazyConstructed.key);
        LAZY_CONSTRUCTED.clear();
    }
    
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * @return The Minecraft biome
     */
    public RegistryEntry<net.minecraft.world.biome.Biome> getMinecraftBiome() { return this.biome; }
    //endregion
    //region API
    /**
     * @return The biome ID
     */
    public String id()
    {
        return this.id.toString();
    }
    /**
     * @return The biome name
     */
    public String name()
    {
        return name.getString();
    }
    /**
     * @return The biome base temperature
     */
    public float temperature()
    {
        return biome.value().getTemperature();
    }
    /**
     * @return The temperature at a given block in the biome
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @param z The z-coordinate
     */
    public float temperature(int x, int y, int z)
    {
        return ((BiomeInvoker)(Object)biome.value()).invokeGetTemperature(new BlockPos(x, y, z));
    }
    //endregion

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Biome biome1 = (Biome) o;
        return this.id.equals(biome1.id);
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }
}
