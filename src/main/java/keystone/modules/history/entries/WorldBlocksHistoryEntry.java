package keystone.modules.history.entries;

import keystone.api.IBlockBox;
import keystone.modules.history.IHistoryEntry;
import net.minecraft.world.World;

public class WorldBlocksHistoryEntry implements IHistoryEntry
{
    protected World world;
    protected IBlockBox[] boxes;

    public WorldBlocksHistoryEntry(World world, IBlockBox[] boxes)
    {
        this.world = world;
        this.boxes = boxes;
    }

    @Override
    public void undo()
    {
        for (IBlockBox box : boxes)
        {
            box.forEachBlock(pos ->
            {
                world.setBlockState(pos, box.getBlock(pos, true));
            });
        }
    }
    @Override
    public void redo()
    {
        for (IBlockBox box : boxes)
        {
            box.forEachBlock(pos ->
            {
                world.setBlockState(pos, box.getBlock(pos, false));
            });
        }
    }
}
