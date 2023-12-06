package keystone.api.wrappers;

import keystone.core.mixins.BiomeInvoker;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * A wrapper for a Minecraft biome.
 */
public class Biome
{
    private RegistryEntry<net.minecraft.world.biome.Biome> biome;
    private Identifier id;
    private Text name;

    //region INTERNAL USE ONLY, DO NOT USE IN FILTERS
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * @param minecraftBiome The Minecraft Biome
     */
    public Biome(net.minecraft.registry.entry.RegistryEntry<net.minecraft.world.biome.Biome> minecraftBiome)
    {
        this.biome = minecraftBiome;
        this.id = minecraftBiome.getKey().get().getValue();
        this.name = Text.translatable("biome." + this.id.getNamespace() + "." + this.id.getPath());
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
