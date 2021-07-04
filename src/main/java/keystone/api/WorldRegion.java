package keystone.api;

import keystone.api.enums.BlockRetrievalMode;
import keystone.api.filters.KeystoneFilter;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.blocks.BlockPalette;
import keystone.api.wrappers.coordinates.BlockPos;
import keystone.api.wrappers.coordinates.Vector3i;
import keystone.api.wrappers.entities.Entity;
import keystone.core.modules.blocks.BlocksModule;
import keystone.core.modules.entities.EntitiesModule;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.common.models.Coords;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IServerWorld;

import java.util.ArrayList;
import java.util.List;

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
    private final BlocksModule blocks;
    private final List<Entity> entities;

    public boolean allowBlocksOutside = false;

    public final BlockPos min;
    public final BlockPos max;
    public final Vector3i size;

    public WorldRegion(Coords min, Coords max)
    {
        this.worldCache = Keystone.getModule(WorldCacheModule.class);
        this.blocks = Keystone.getModule(BlocksModule.class);
        EntitiesModule entitiesModule = Keystone.getModule(EntitiesModule.class);

        this.min = new BlockPos(min.getX(), min.getY(), min.getZ());
        this.max = new BlockPos(max.getX(), max.getY(), max.getZ());
        this.size = new Vector3i(max.getX() - min.getX() + 1, max.getY() - min.getY() + 1, max.getZ() - min.getZ() + 1);

        this.entities = new ArrayList<>();
        this.entities.addAll(entitiesModule.getEntities(this.min, this.max));
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
            blocks.setBlock(x, y, z, block);
            return true;
        }
        else return false;
    }

    /**
     * Add an {@link Entity} to the region
     * @param entity The {@link Entity} to add
     */
    public void addEntity(Entity entity)
    {
        this.entities.add(entity);
    }

    /**
     * Run a {@link WorldRegion.BlockConsumer} on every block in the filter box
     * @param consumer The {@link WorldRegion.BlockConsumer} to run
     */
    public void forEachBlock(BlockConsumer consumer)
    {
        forEachBlock(consumer, BlockRetrievalMode.LAST_SWAPPED);
    }
    /**
     * Run a {@link WorldRegion.BlockConsumer} on every block in the filter box
     * @param consumer The {@link WorldRegion.BlockConsumer} to run
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

    /**
     * Run an {@link EntityConsumer} on every entity in the filter box
     * @param consumer The {@link EntityConsumer} to run
     */
    public void forEachEntity(EntityConsumer consumer)
    {
        for (Entity entity : this.entities) consumer.accept(entity);
    }

    /**
     * Call {@link Entity#updateMinecraftEntity(IServerWorld)} for all entities in
     * this region
     */
    public void updateEntities()
    {
        IServerWorld world = worldCache.getDimensionServerWorld(Player.getDimensionId());
        for (Entity entity : this.entities) entity.updateMinecraftEntity(world);
    }
}
