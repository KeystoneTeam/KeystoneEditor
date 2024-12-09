import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.variables.Variable;
import keystone.api.BlockPalette;

public class Corners extends KeystoneFilter
{
    @Variable BlockPalette palette = palette("minecraft:gold_block");
    
    @Override
    public void processRegion(WorldRegion region)
    {
        region.setBlockType(region.min, palette);
        region.setBlockType(region.max, palette);
    }
}