package keystone.modules.history.entries;

import keystone.api.filters.FilterBox;
import keystone.api.wrappers.Block;
import keystone.modules.history.IHistoryEntry;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FilterHistoryEntry implements IHistoryEntry
{
    protected World world;
    protected FilterBox[] boxes;

    public FilterHistoryEntry(World world, FilterBox[] boxes)
    {
        this.world = world;
        this.boxes = boxes;
    }

    @Override
    public void undo()
    {
        for (FilterBox box : boxes)
        {
            box.forEachBlock((x, y, z) ->
            {
                Block block = box.getBlock(x, y, z, true);
                BlockPos pos = new BlockPos(x, y, z);

                world.setBlockState(pos, block.getMinecraftBlock());
                if (block.getTileEntityData() != null)
                {
                    CompoundNBT tileEntityData = block.getTileEntityData().copy();
                    tileEntityData.putInt("x", x);
                    tileEntityData.putInt("y", y);
                    tileEntityData.putInt("z", z);

                    TileEntity tileEntity = world.getTileEntity(pos);
                    if (tileEntity != null) tileEntity.read(block.getMinecraftBlock(), tileEntityData);
                }
            });
        }
    }
    @Override
    public void redo()
    {
        for (FilterBox box : boxes)
        {
            box.forEachBlock((x, y, z) ->
            {
                Block block = box.getBlock(x, y, z, false);
                BlockPos pos = new BlockPos(x, y, z);

                world.setBlockState(pos, block.getMinecraftBlock());
                if (block.getTileEntityData() != null)
                {
                    CompoundNBT tileEntityData = block.getTileEntityData().copy();
                    tileEntityData.putInt("x", x);
                    tileEntityData.putInt("y", y);
                    tileEntityData.putInt("z", z);

                    TileEntity tileEntity = world.getTileEntity(pos);
                    if (tileEntity != null) tileEntity.read(block.getMinecraftBlock(), tileEntityData);
                }
            });
        }
    }
}
