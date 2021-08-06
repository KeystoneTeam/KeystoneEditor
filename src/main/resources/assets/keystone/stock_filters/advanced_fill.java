import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.variables.Variable;
import keystone.api.wrappers.blocks.BlockMask;
import keystone.api.wrappers.blocks.BlockPalette;

public class AdvancedFill extends KeystoneFilter
{
    @Variable BlockMask mask = blacklist();
    @Variable BlockPalette palette = palette("minecraft:stone");

    @Override
    public void processBlock(int x, int y, int z, WorldRegion box)
    {
        if (mask.valid(box.getBlock(x, y, z))) box.setBlock(x, y, z, palette);
    }
}