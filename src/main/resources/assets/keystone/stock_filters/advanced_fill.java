import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.variables.Variable;
import keystone.api.wrappers.blocks.BlockMask;
import keystone.api.wrappers.blocks.BlockPalette;

public class AdvancedFill extends KeystoneFilter
{
    @Variable BlockMask mask = whitelist("minecraft:air");
    @Variable BlockPalette palette = palette("minecraft:stone");
    @Variable boolean useMask = false;

    @Override
    public void processBlock(int x, int y, int z, WorldRegion box)
    {
        if (!useMask || mask.valid(box.getBlock(x, y, z))) box.setBlock(x, y, z, palette);
    }
}