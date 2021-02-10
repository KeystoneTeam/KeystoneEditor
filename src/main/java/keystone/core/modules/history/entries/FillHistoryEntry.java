package keystone.core.modules.history.entries;

import keystone.api.SelectionBox;
import keystone.core.modules.history.IHistoryEntry;
import net.minecraft.world.World;

public class FillHistoryEntry implements IHistoryEntry
{
    protected World world;
    protected SelectionBox[] boxes;

    public FillHistoryEntry(World world, SelectionBox[] boxes)
    {
        this.world = world;
        this.boxes = boxes;
    }

    @Override
    public void undo()
    {
        for (SelectionBox box : boxes)
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
        for (SelectionBox box : boxes)
        {
            box.forEachBlock(pos ->
            {
                world.setBlockState(pos, box.getBlock(pos, false));
            });
        }
    }
}
