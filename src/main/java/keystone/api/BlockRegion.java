package keystone.api;

import keystone.api.enums.BlockRetrievalMode;
import keystone.api.filters.KeystoneFilter;
import keystone.api.wrappers.Block;
import keystone.api.wrappers.BlockPalette;
import keystone.api.wrappers.BlockPos;
import keystone.api.wrappers.Vector3i;
import keystone.core.modules.blocks.BlocksModule;
import keystone.core.renderer.common.models.Coords;

public class BlockRegion
{
    //region Function Types
    public interface BlockConsumer
    {
        void accept(int x, int y, int z, Block block);
    }
    //endregion

    private final BlocksModule blocks;

    public boolean allowBlocksOutside = false;

    public final BlockPos min;
    public final BlockPos max;
    public final Vector3i size;

    public BlockRegion(Coords min, Coords max)
    {
        this.blocks = Keystone.getModule(BlocksModule.class);
        this.min = new BlockPos(min.getX(), min.getY(), min.getZ());
        this.max = new BlockPos(max.getX(), max.getY(), max.getZ());
        this.size = new Vector3i(max.getX() - min.getX() + 1, max.getY() - min.getY() + 1, max.getZ() - min.getZ() + 1);
    }
    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param min The minimum corner of the region
     * @param max The maximum corner of the region
     * @param blocks The block module to use for getting and setting blocks
     */
    public BlockRegion(net.minecraft.util.math.BlockPos min, net.minecraft.util.math.BlockPos max, BlocksModule blocks)
    {
        this.blocks = blocks;
        this.min = new BlockPos(min);
        this.max = new BlockPos(max);
        this.size = new Vector3i(max.subtract(min).offset(1, 1, 1));
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
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @param z The z-coordinate
     * @return Whether the position is in the box
     */
    public boolean isPositionInBox(int x, int y, int z)
    {
        return x >= min.x && x <= max.x && y >= min.y && y <= max.y && z >= min.z && z <= max.z;
    }

    /**
     * Retrieve the top-most block of a column in the filter box that is not air
     * @param x The x coordinate
     * @param z The z coordinate
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
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return The block at the given coordinates
     */
    public Block getBlock(int x, int y, int z) { return blocks.getBlock(x, y, z, BlockRetrievalMode.LAST_SWAPPED); }

    /**
     * Get the block at a position in the filter box
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param retrievalMode The {@link BlockRetrievalMode} to use when getting the block
     * @return The block at the given coordinates
     */
    public Block getBlock(int x, int y, int z, BlockRetrievalMode retrievalMode)
    {
        return blocks.getBlock(x, y, z, retrievalMode);
    }

    /**
     * Set the block at a position in the filter box. This will only work if the position is within the filter box
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param block The block to change the position to. [e.g. "stone", "minecraft:stone", "stone_slab[type=top]", "chest{Items:[]}"]
     * @return Whether the change was successful
     */
    public boolean setBlock(int x, int y, int z, String block)
    {
        return setBlock(x, y, z, KeystoneFilter.block(block));
    }

    /**
     * Set the block at a position in the filter box to a random entry in a {@link keystone.api.wrappers.BlockPalette}.
     * This will only work if the position is within the filter box
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param palette The {@link keystone.api.wrappers.BlockPalette} to change the position to
     * @return Whether the change was successful
     */
    public boolean setBlock(int x, int y, int z, BlockPalette palette) { return setBlock(x, y, z, palette.randomBlock()); }

    /**
     * Set the block at a position in the filter box to a {@link keystone.api.wrappers.Block}.
     * This will only work if the position is within the filter box
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param block The {@link keystone.api.wrappers.Block} to change the position to
     * @return Whether the change was successful
     */
    public boolean setBlock(int x, int y, int z, Block block)
    {
        if (allowBlocksOutside || isPositionInBox(x, y, z))
        {
            blocks.setBlock(x, y, z, block);
            return true;
        }
        else return false;
    }

    /**
     * Run a {@link BlockRegion.BlockConsumer} on every block in the filter box
     * @param consumer The {@link BlockRegion.BlockConsumer} to run
     */
    public void forEachBlock(BlockConsumer consumer)
    {
        forEachBlock(consumer, BlockRetrievalMode.LAST_SWAPPED);
    }
    /**
     * Run a {@link BlockRegion.BlockConsumer} on every block in the filter box
     * @param consumer The {@link BlockRegion.BlockConsumer} to run
     * @param retrievalMode The {@link BlockRetrievalMode} to use when getting block states
     */
    public void forEachBlock(BlockConsumer consumer, BlockRetrievalMode retrievalMode)
    {
        for (int x = min.x; x <= max.x; x++)
        {
            for (int y = min.y; y <= max.y; y++)
            {
                for (int z = min.z; z <= max.z; z++)
                {
                    consumer.accept(x, y, z, getBlock(x, y, z, retrievalMode));
                }
            }
        }
    }
}
