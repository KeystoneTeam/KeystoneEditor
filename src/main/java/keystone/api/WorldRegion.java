package keystone.api;

import keystone.api.enums.RetrievalMode;
import keystone.api.filters.KeystoneFilter;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.blocks.BlockPalette;
import keystone.api.wrappers.coordinates.BlockPos;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.coordinates.Vector3i;
import keystone.api.wrappers.entities.Entity;
import keystone.core.modules.WorldModifierModules;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.renderer.common.models.Coords;

public class WorldRegion
{
    //region Function Types
    public interface BlockConsumer
    {
        void accept(int x, int y, int z, Block block);
    }
    public interface EntityConsumer
    {
        void accept(Entity entity);
    }
    //endregion

    private final WorldCacheModule worldCache;
    private final WorldModifierModules worldModifiers;

    public boolean allowBlocksOutside = false;

    public final BlockPos min;
    public final BlockPos max;
    public final Vector3i size;
    public final BoundingBox bounds;

    public WorldRegion(Coords min, Coords max)
    {
        this.worldCache = Keystone.getModule(WorldCacheModule.class);
        this.worldModifiers = new WorldModifierModules();

        this.min = new BlockPos(min.getX(), min.getY(), min.getZ());
        this.max = new BlockPos(max.getX(), max.getY(), max.getZ());
        this.size = new Vector3i(max.getX() - min.getX() + 1, max.getY() - min.getY() + 1, max.getZ() - min.getZ() + 1);
        this.bounds = new BoundingBox(this.min, this.max);
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
    public Block getBlock(int x, int y, int z) { return worldModifiers.blocks.getBlock(x, y, z, RetrievalMode.LAST_SWAPPED); }

    /**
     * Get the block at a position in the filter box
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param retrievalMode The {@link RetrievalMode} to use when getting the block
     * @return The block at the given coordinates
     */
    public Block getBlock(int x, int y, int z, RetrievalMode retrievalMode)
    {
        return worldModifiers.blocks.getBlock(x, y, z, retrievalMode);
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
     * Set the block at a position in the filter box to a random entry in a {@link BlockPalette}.
     * This will only work if the position is within the filter box
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param palette The {@link BlockPalette} to change the position to
     * @return Whether the change was successful
     */
    public boolean setBlock(int x, int y, int z, BlockPalette palette) { return setBlock(x, y, z, palette.randomBlock()); }

    /**
     * Set the block at a position in the filter box to a {@link Block}.
     * This will only work if the position is within the filter box or
     * allowBlocksOutside is true
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param block The {@link Block} to change the position to
     * @return Whether the change was successful
     */
    public boolean setBlock(int x, int y, int z, Block block)
    {
        if (allowBlocksOutside || isPositionInBox(x, y, z))
        {
            worldModifiers.blocks.setBlock(x, y, z, block);
            return true;
        }
        else return false;
    }

    /**
     * Add or modify an {@link Entity} in the region
     * @param entity The {@link Entity} to add or modify
     */
    public void setEntity(Entity entity)
    {
        worldModifiers.entities.setEntity(entity);
    }

    /**
     * Run a {@link WorldRegion.BlockConsumer} on every block in the filter box
     * @param consumer The {@link WorldRegion.BlockConsumer} to run
     */
    public void forEachBlock(BlockConsumer consumer)
    {
        forEachBlock(consumer, RetrievalMode.LAST_SWAPPED);
    }
    /**
     * Run a {@link WorldRegion.BlockConsumer} on every block in the filter box
     * @param consumer The {@link WorldRegion.BlockConsumer} to run
     * @param retrievalMode The {@link RetrievalMode} to use when getting block states
     */
    public void forEachBlock(BlockConsumer consumer, RetrievalMode retrievalMode)
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

    /**
     * Run an {@link EntityConsumer} on every entity in the filter box
     * @param consumer The {@link EntityConsumer} to run
     */
    public void forEachEntity(EntityConsumer consumer)
    {
        forEachEntity(consumer, RetrievalMode.LAST_SWAPPED);
    }
    /**
     * Run an {@link EntityConsumer} on every entity in the filter box
     * @param consumer The {@link EntityConsumer} to run
     * @param retrievalMode The {@link RetrievalMode} to use when getting entities
     */
    public void forEachEntity(EntityConsumer consumer, RetrievalMode retrievalMode)
    {
        worldModifiers.entities.getEntities(this.bounds, retrievalMode).forEach(entity -> consumer.accept(entity));
    }
}
