import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.variables.Tooltip;
import keystone.api.variables.Variable;
import keystone.api.wrappers.Biome;
import keystone.api.wrappers.blocks.BlockMask;

public class SetBiome extends KeystoneFilter
{
    @Tooltip("Only blocks matching this mask will have their biomes changed.")
    @Variable BlockMask mask = blacklist();
    
    @Tooltip("The biome to change the selection to.")
    @Variable Biome biome = biome("minecraft:plains");

    @Override
    public void processBlock(int x, int y, int z, WorldRegion region)
    {
        if (mask.valid(region.getBlockType(x, y, z))) region.setBiome(x, y, z, biome);
    }
}