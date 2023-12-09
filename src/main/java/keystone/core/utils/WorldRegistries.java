package keystone.core.utils;

import keystone.api.Keystone;
import keystone.core.modules.world_cache.WorldCacheModule;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public final class WorldRegistries
{
    public static RegistryWrapper<Block> blockLookup()
    {
        World world = Keystone.getModule(WorldCacheModule.class).getPrimaryWorld();
        if (world == null) return Registries.BLOCK.getReadOnlyWrapper();
        else return world.createCommandRegistryWrapper(RegistryKeys.BLOCK);
    }
    public static RegistryWrapper<Item> itemLookup()
    {
        World world = Keystone.getModule(WorldCacheModule.class).getPrimaryWorld();
        if (world == null) return Registries.ITEM.getReadOnlyWrapper();
        else return world.createCommandRegistryWrapper(RegistryKeys.ITEM);
    }
    
    public static Registry<Biome> biomes()
    {
        World world = Keystone.getModule(WorldCacheModule.class).getPrimaryWorld();
        if (world == null)
        {
            Keystone.LOGGER.error("Trying to get biome registry with null world!");
            return null;
        }
        else return biomes(world);
    }
    public static Registry<Biome> biomes(World world)
    {
        return world.getRegistryManager().get(RegistryKeys.BIOME);
    }
}