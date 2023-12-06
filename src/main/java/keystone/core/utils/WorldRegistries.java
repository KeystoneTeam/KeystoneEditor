package keystone.core.utils;

import keystone.api.Keystone;
import keystone.core.client.Player;
import keystone.core.modules.world_cache.WorldCacheModule;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public final class WorldRegistries
{
    public static Registry<Biome> getBiomeRegistry()
    {
        return getBiomeRegistry(Keystone.getModule(WorldCacheModule.class).getDimensionWorld(Player.getDimension()));
    }
    public static Registry<Biome> getBiomeRegistry(World world)
    {
        return world != null ? world.getRegistryManager().get(RegistryKeys.BIOME) : BuiltinRegistries.BIOME;
    }
}