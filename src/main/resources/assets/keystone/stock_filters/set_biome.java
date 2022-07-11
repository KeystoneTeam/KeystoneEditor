import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.variables.Variable;
import keystone.api.wrappers.blocks.BlockMask;

public class SetBiome extends KeystoneFilter
{
    @Variable BlockMask mask = blacklist();
    @Variable String biomeIdentifier = "minecraft:plains";

    @Override
    public void processBlock(int x, int y, int z, WorldRegion region)
    {
        if (mask.valid(region.getBlockType(x, y, z))) region.setBiome(x, y, z, biome(biomeIdentifier));
    }
}