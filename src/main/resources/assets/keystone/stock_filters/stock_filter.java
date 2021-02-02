public class StockFilter extends KeystoneFilter
{
    @FilterVariable BlockMask mask = mask("minecraft:air").blacklist();
    @FilterVariable BlockPalette palette = palette("minecraft:stone 7", "minecraft:andesite 2", "minecraft:cobblestone 1");

    @Override
    public void processBlock(int x, int y, int z, FilterBox box)
    {
        if (mask.valid(box.getBlock(x, y, z))) box.setBlock(x, y, z, palette);
    }
}