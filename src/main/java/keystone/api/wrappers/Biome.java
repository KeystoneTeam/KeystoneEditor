package keystone.api.wrappers;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * A wrapper for a Minecraft biome.
 */
public class Biome
{
    private net.minecraft.world.biome.Biome biome;
    private TranslationTextComponent name;

    //region INTERNAL USE ONLY, DO NOT USE IN FILTERS
    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param minecraftBiome The Minecraft Biome
     */
    public Biome(net.minecraft.world.biome.Biome minecraftBiome)
    {
        this.biome = minecraftBiome;
        this.name = new TranslationTextComponent("biome." + biome.getRegistryName().getNamespace() + "." + biome.getRegistryName().getPath());
    }
    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @return The Minecraft biome
     */
    public net.minecraft.world.biome.Biome getMinecraftBiome() { return this.biome; }
    //endregion
    //region API
    /**
     * @return The biome ID
     */
    public String id()
    {
        return biome.getRegistryName().toString();
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
    public float baseTemperature()
    {
        return biome.getBaseTemperature();
    }
    /**
     * @return The temperature at a given block in the biome
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @param z The z-coordinate
     */
    public float temperature(int x, int y, int z)
    {
        return biome.getTemperature(new BlockPos(x, y, z));
    }
    /**
     * @return The category of the biome. Can be [none, taiga, extreme_hills, jungle, mesa, plains, savanna,
     * icy, the_end, beach, forest, ocean, desert, river, swamp, mushroom, nether]
     */
    public String category()
    {
        return biome.getBiomeCategory().getName();
    }
    /**
     * @return The type of rain in the biome. Can be [none, rain, snow]
     */
    public String rainType()
    {
        return biome.getPrecipitation().getName();
    }
    //endregion

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Biome biome1 = (Biome) o;
        return biome.getRegistryName().equals(biome1.biome.getRegistryName());
    }

    @Override
    public int hashCode()
    {
        return biome.getRegistryName().hashCode();
    }
}
