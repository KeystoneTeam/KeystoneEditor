package keystone.modules.history.entries;

import keystone.api.SelectionBox;
import keystone.modules.history.IHistoryEntry;
import keystone.modules.selection.boxes.SelectionBoundingBox;
import net.minecraft.world.World;

public class WorldBlocksHistoryEntry implements IHistoryEntry
{
    protected World world;
    protected SelectionBox[] boxes;

    public WorldBlocksHistoryEntry(World world, SelectionBoundingBox[] boxes)
    {
        SelectionBox[] converted = new SelectionBox[boxes.length];
        for (int i = 0; i < boxes.length; i++) converted[i] = new SelectionBox(boxes[i].getMinCoords(), boxes[i].getMaxCoords(), world);
    }
    public WorldBlocksHistoryEntry(World world, SelectionBox[] boxes)
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
