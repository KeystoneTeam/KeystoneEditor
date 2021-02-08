package keystone.api;

import keystone.core.renderer.common.models.Coords;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;

import java.util.function.Consumer;

/**
 * A selection box containing block state data
 */
public class SelectionBox
{
    private final BlockPos min;
    private final BlockPos max;
    private final Vector3i size;
    private final World world;

    private BlockState[] blocks;
    private BlockState[] buffer;

    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param min The minimum coordinates of the selection box
     * @param max The maximum coordinates of the selection box
     * @param world The {@link net.minecraft.world.World} the selection box is a part of
     */
    public SelectionBox(Coords min, Coords max, World world)
    {
        this.min = new BlockPos(min.getX(), min.getY(), min.getZ());
        this.max = new BlockPos(max.getX(), max.getY(), max.getZ());
        this.size = new Vector3i(max.getX() - min.getX() + 1, max.getY() - min.getY() + 1, max.getZ() - min.getZ() + 1);
        this.world = world;

        blocks = new BlockState[size.getX() * size.getY() * size.getZ()];
        buffer = new BlockState[size.getX() * size.getY() * size.getZ()];

        int i = 0;
        for (int x = min.getX(); x <= max.getX(); x++)
        {
            for (int y = min.getY(); y <= max.getY(); y++)
            {
                for (int z = min.getZ(); z <= max.getZ(); z++)
                {
                    BlockState block = world.getBlockState(new BlockPos(x, y, z));
                    blocks[i] = block;
                    buffer[i] = block;
                    i++;
                }
            }
        }
    }

    /**
     * Convert a block position into an array index
     * @param pos The block position
     * @return The converted array index
     */
    private int getBlockIndex(BlockPos pos)
    {
        Vector3i normalized = new Vector3i(pos.getX() - min.getX(), pos.getY() - min.getY(), pos.getZ() - min.getZ());

        if (normalized.getX() < 0 || normalized.getX() >= size.getX() ||
                normalized.getY() < 0 || normalized.getY() >= size.getY() ||
                normalized.getZ() < 0 || normalized.getZ() >= size.getZ())
        {
            return -1;
        }

        return normalized.getZ() + normalized.getY() * size.getZ() + normalized.getX() * size.getZ() * size.getY();
    }

    public BlockPos getMin() { return min; }
    public BlockPos getMax() { return max; }
    public Vector3i getSize() { return size; }

    /**
     * Get the block state at a position, before tools have modified it
     * @param pos The block position
     * @return The block state before tool execution
     */
    public BlockState getBlock(BlockPos pos) { return getBlock(pos, true); }
    /**
     * Get the block state at a position
     * @param pos The block position
     * @param getOriginalState Whether to get the state from before tool execution
     * @return The block state
     */
    public BlockState getBlock(BlockPos pos, boolean getOriginalState)
    {
        int index = getBlockIndex(pos);
        if (index < 0) return world.getBlockState(pos);
        else return getOriginalState ? blocks[index] : buffer[index];
    }

    /**
     * Set the block state at a position
     * @param pos The block position
     * @param block The block
     * @return Whether the operation was successful
     */
    public boolean setBlock(BlockPos pos, Block block) { return setBlock(pos, block.getDefaultState()); }
    /**
     * Set the block state at a position
     * @param pos The block position
     * @param block The block state
     * @return Whether the operation was successful
     */
    public boolean setBlock(BlockPos pos, BlockState block)
    {
        int index = getBlockIndex(pos);
        if (index < 0) return false;

        buffer[index] = block;
        return true;
    }

    /**
     * Run a function on every block position in the selection box
     * @param consumer The function to run
     */
    public void forEachBlock(Consumer<BlockPos> consumer)
    {
        for (int x = min.getX(); x <= max.getX(); x++)
        {
            for (int y = min.getY(); y <= max.getY(); y++)
            {
                for (int z = min.getZ(); z <= max.getZ(); z++)
                {
                    consumer.accept(new BlockPos(x, y, z));
                }
            }
        }
    }
}
