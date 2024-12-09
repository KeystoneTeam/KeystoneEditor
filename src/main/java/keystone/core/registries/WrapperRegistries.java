package keystone.core.registries;

import keystone.api.wrappers.BlockType;
import keystone.api.wrappers.Biome;
import keystone.core.events.keystone.KeystoneLifecycleEvents;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;

public class WrapperRegistries
{
    private static WrapperRegistry<BlockState, BlockType> blocks;
    private static WrapperRegistry<RegistryEntry<net.minecraft.world.biome.Biome>, Biome> biomes;
    
    public static void init()
    {
        KeystoneLifecycleEvents.OPEN_WORLD.register(WrapperRegistries::buildWrapperRegistries);
    }
    public static WrapperRegistry<BlockState, BlockType> getBlocks() { return blocks; }
    public static WrapperRegistry<RegistryEntry<net.minecraft.world.biome.Biome>, Biome> getBiomes() { return biomes; }
    
    private static void buildWrapperRegistries(World world)
    {
        blocks = new WrapperRegistry<>(world.getRegistryManager().get(RegistryKeys.BLOCK).stream().flatMap(block -> block.getStateManager().getStates().stream()), BlockType::new);
        biomes = new WrapperRegistry<>(world.getRegistryManager().get(RegistryKeys.BIOME).streamEntries().map(entry -> entry), Biome::new);
    }
}
