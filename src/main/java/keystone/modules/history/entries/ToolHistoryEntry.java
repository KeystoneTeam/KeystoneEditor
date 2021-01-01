package keystone.modules.history.entries;

import keystone.api.SelectionBox;
import keystone.modules.history.IHistoryEntry;
import net.minecraft.world.World;

public class ToolHistoryEntry implements IHistoryEntry
{
    private World world;
    private SelectionBox[] boxes;

    public ToolHistoryEntry(World world, SelectionBox[] boxes)
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
        for (SelectionBox box : boxes) box.applyChanges(world);
    }
}
