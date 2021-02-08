package keystone.api.filters;

import keystone.api.SelectionBox;
import keystone.api.wrappers.Block;
import keystone.api.wrappers.BlockPalette;
import keystone.api.wrappers.BlockPos;
import keystone.api.wrappers.Vector3i;
import net.minecraft.block.Blocks;
import net.minecraft.world.World;

public class FilterBox
{
    //region Function Types
    public interface BlockConsumer
    {
        void accept(int x, int y, int z);
    }
    //endregion

    private static final Block air = new Block(Blocks.AIR.getDefaultState());

    private final KeystoneFilter filter;
    private final World world;

    public final BlockPos min;
    public final BlockPos max;
    public final Vector3i size;

    private Block[] oldBlocks;
    private Block[] newBlocks;

    /**
     * Filter Box constructor. INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param world The world this filter box references
     * @param box The selection box that this filter box is based off of
     * @param filter The filter that will process this filter box
     */
    public FilterBox(World world, SelectionBox box, KeystoneFilter filter)
    {
        this.filter = filter;
        this.world = world;
        this.min = new BlockPos(box.getMin());
        this.max = new BlockPos(box.getMax());
        this.size = new Vector3i(box.getSize());

        this.oldBlocks = new Block[this.size.x * this.size.y * this.size.z];
        this.newBlocks = new Block[this.size.x * this.size.y * this.size.z];

        int i = 0;
        for (int x = min.x; x <= max.x; x++)
        {
            for (int y = min.y; y <= max.y; y++)
            {
                for (int z = min.z; z <= max.z; z++)
                {
                    BlockPos pos = new BlockPos(x, y, z);
                    Block block = new Block(world.getBlockState(pos.getMinecraftBlockPos()), world.getTileEntity(pos.getMinecraftBlockPos()));

                    oldBlocks[i] = block;
                    newBlocks[i] = block;
                    i++;
                }
            }
        }
    }

    /**
     * Convert a block position to an array index
     * @param x
     * @param y
     * @param z
     * @return The index of the position in the blocks array, or -1 if the position is not in the filter box
     */
    private int getBlockIndex(int x, int y, int z)
    {
        int normalizedX = x - min.x;
        int normalizedY = y - min.y;
        int normalizedZ = z - min.z;

        if (normalizedX < 0 || normalizedX >= size.x ||
                normalizedY < 0 || normalizedY >= size.y ||
                normalizedZ < 0 || normalizedZ >= size.z)
        {
            return -1;
        }

        return normalizedZ + normalizedY * size.z + normalizedX * size.z * size.y;
    }

    /**
     * @return The minimum corner of the filter box
     */
    public BlockPos getMin() { return this.min; }

    /**
     * @return The maximum corner of the filter box
     */
    public BlockPos getMax() { return this.max; }

    /**
     * @return The size of the filter box
     */
    public Vector3i getSize() { return this.size; }

    /**
     * Retrieve the top-most block of a column in the filter box that is not air
     * @param x
     * @param z
     * @return The highest non-air block in the filter box
     */
    public int getTopBlock(int x, int z)
    {
        int y = this.max.y;
        Block block = getBlock(x, y, z);
        while (block.isAir())
        {
            y--;
            block = getBlock(x, y, z);
        }
        return y;
    }

    /**
     * Get the block at a position in the filter box, before any changes were made by the filter
     * @param x
     * @param y
     * @param z
     * @return The block at the given coordinates
     */
    public Block getBlock(int x, int y, int z) { return getBlock(x, y, z, true); }

    /**
     * Get the block at a position in the filter box
     * @param x
     * @param y
     * @param z
     * @param getOriginalState If true, it will return the original block before the filter changed it
     * @return The block at the given coordinates
     */
    public Block getBlock(int x, int y, int z, boolean getOriginalState)
    {
        int index = getBlockIndex(x, y, z);
        if (index < 0)
        {
            net.minecraft.util.math.BlockPos pos = new net.minecraft.util.math.BlockPos(x, y, z);
            return new Block(world.getBlockState(pos), world.getTileEntity(pos));
        }
        else return getOriginalState ? oldBlocks[index] : newBlocks[index];
    }

    /**
     * Set the block at a position in the filter box. This will only work if the position is within the filter box
     * @param x
     * @param y
     * @param z
     * @param block The block to change the position to. [e.g. "stone", "minecraft:stone", "stone_slab[type=top]", "chest{Items:[]}"]
     * @return Whether the change was successful
     */
    public boolean setBlock(int x, int y, int z, String block)
    {
        return setBlock(x, y, z, filter.block(block));
    }

    /**
     * Set the block at a position in the filter box to a random entry in a {@link keystone.api.wrappers.BlockPalette}.
     * This will only work if the position is within the filter box
     * @param x
     * @param y
     * @param z
     * @param palette The {@link keystone.api.wrappers.BlockPalette} to change the position to
     * @return Whether the change was successful
     */
    public boolean setBlock(int x, int y, int z, BlockPalette palette) { return setBlock(x, y, z, palette.randomBlock()); }

    /**
     * Set the block at a position in the filter box to a {@link keystone.api.wrappers.Block}.
     * This will only work if the position is within the filter box
     * @param x
     * @param y
     * @param z
     * @param block The {@link keystone.api.wrappers.Block} to change the position to
     * @return Whether the change was successful
     */
    public boolean setBlock(int x, int y, int z, Block block)
    {
        int index = getBlockIndex(x, y, z);
        if (index < 0) return false;

        newBlocks[index] = block;
        return true;
    }

    /**
     * Run a {@link keystone.api.filters.FilterBox.BlockConsumer} on every block in the filter box
     * @param consumer The {@link keystone.api.filters.FilterBox.BlockConsumer} to run
     */
    public void forEachBlock(BlockConsumer consumer)
    {
        for (int x = min.x; x <= max.x; x++)
        {
            for (int y = min.y; y <= max.y; y++)
            {
                for (int z = min.z; z <= max.z; z++)
                {
                    consumer.accept(x, y, z);
                }
            }
        }
    }
}
