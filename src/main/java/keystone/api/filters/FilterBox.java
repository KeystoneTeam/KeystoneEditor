package keystone.api.filters;

import keystone.api.IBlockBox;
import keystone.api.SelectionBox;
import keystone.api.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;

import java.util.function.Consumer;

public class FilterBox implements IBlockBox
{
    //region Function Types
    public interface BlockConsumer
    {
        void accept(int x, int y, int z);
    }
    //endregion

    private SelectionBox selectionBox;

    public FilterBox(SelectionBox box)
    {
        this.selectionBox = box;
    }

    public BlockPos getMin() { return selectionBox.getMin(); }
    public BlockPos getMax() { return selectionBox.getMax(); }
    public Vector3i getSize() { return selectionBox.getSize(); }

    public Block getBlock(int x, int y, int z) { return getBlock(x, y, z, true); }
    public Block getBlock(int x, int y, int z, boolean getOriginalState)
    {
        return new Block(selectionBox.getBlock(new BlockPos(x, y, z), getOriginalState));
    }

    public boolean setBlock(int x, int y, int z, Block block)
    {
        return selectionBox.setBlock(new BlockPos(x, y, z), block.getMinecraftBlock());
    }

    public void forEachBlock(BlockConsumer consumer)
    {
        selectionBox.forEachBlock(pos -> consumer.accept(pos.getX(), pos.getY(), pos.getZ()));
    }

    @Override
    public BlockState getBlock(BlockPos pos, boolean getOriginalState)
    {
        return selectionBox.getBlock(pos, getOriginalState);
    }
    @Override
    public boolean setBlock(BlockPos pos, BlockState block)
    {
        return selectionBox.setBlock(pos, block);
    }
    @Override
    public void forEachBlock(Consumer<BlockPos> consumer)
    {
        selectionBox.forEachBlock(consumer);
    }
}
