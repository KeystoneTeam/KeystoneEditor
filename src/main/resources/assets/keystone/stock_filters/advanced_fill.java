import keystone.api.filters.FilterBox;
import keystone.api.filters.KeystoneFilter;
import keystone.api.variables.Variable;
import keystone.api.wrappers.BlockMask;
import keystone.api.wrappers.BlockPalette;

public class AdvancedFill extends KeystoneFilter
{
    @Variable BlockMask mask = whitelist("minecraft:air");
    @Variable BlockPalette palette = palette("minecraft:stone");
    @Variable boolean useMask = false;

    @Override
    public void processBlock(int x, int y, int z, FilterBox box)
    {
        if (!useMask || mask.valid(box.getBlock(x, y, z))) box.setBlock(x, y, z, palette);
    }
}