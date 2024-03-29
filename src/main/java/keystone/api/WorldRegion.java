package keystone.api;

import keystone.api.enums.RetrievalMode;
import keystone.api.wrappers.Biome;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.blocks.BlockPalette;
import keystone.api.wrappers.blocks.BlockType;
import keystone.api.wrappers.coordinates.BlockPos;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.coordinates.Vector3i;
import keystone.api.wrappers.entities.Entity;
import keystone.core.modules.world.WorldModifierModules;
import keystone.core.modules.world_cache.WorldCacheModule;
import net.minecraft.util.math.Vec3i;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class WorldRegion
{
    //region Function Types
    public interface BlockConsumer
    {
        void accept(int x, int y, int z, BlockType blockType);
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

    public WorldRegion(Vec3i min, Vec3i max)
    {
        this(new BlockPos(min.getX(), min.getY(), min.getZ()), new BlockPos(max.getX(), max.getY(), max.getZ()));
    }
    public WorldRegion(BlockPos min, BlockPos max)
    {
        this.worldCache = Keystone.getModule(WorldCacheModule.class);
        this.worldModifiers = new WorldModifierModules();

        this.min = new BlockPos(min.x, min.y, min.z);
        this.max = new BlockPos(max.x, max.y, max.z);
        this.size = new Vector3i(max.x - min.x + 1, max.y - min.y + 1, max.z - min.z + 1);
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
     * @return The {@link WorldModifierModules} used by this world region. Should only
     * be used as a parameter for function calls, and should never be used for directly
     * modifying world contents
     */
    public WorldModifierModules getWorldModifiers() { return this.worldModifiers; }

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
        BlockType blockType = getBlockType(x, y, z);
        while (blockType.isAir())
        {
            y--;
            blockType = getBlockType(x, y, z);
        }
        return y;
    }

    /**
     * Get the {@link BlockType} at a position in the filter box, before any changes were made by the filter
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return The block at the given coordinates
     */
    public BlockType getBlockType(int x, int y, int z) { return worldModifiers.blocks.getBlockType(x, y, z, RetrievalMode.LAST_SWAPPED); }

    /**
     * Get the {@link BlockType} at a position in the filter box
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param retrievalMode The {@link RetrievalMode} to use when getting the block
     * @return The block at the given coordinates
     */
    public BlockType getBlockType(int x, int y, int z, RetrievalMode retrievalMode)
    {
        return worldModifiers.blocks.getBlockType(x, y, z, retrievalMode);
    }

    /**
     * Get the {@link Block} at a position in the filter box, before any changes were made by the filter
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return The block at the given coordinates
     */
    public Block getBlock(int x, int y, int z) { return worldModifiers.blocks.getBlock(x, y, z, RetrievalMode.LAST_SWAPPED); }

    /**
     * Get the {@link Block} at a position in the filter box
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
     * Get the biome at a position in the filter box
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @return The biome at the given coordinates
     */
    public Biome getBiome(int x, int y, int z)
    {
        return worldModifiers.biomes.getBiome(x, y, z, RetrievalMode.LAST_SWAPPED, true);
    }
    /**
     * Get the biome at a position in the filter box
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param retrievalMode The {@link RetrievalMode} to use when getting the biome
     * @return The biome at the given coordinates
     */
    public Biome getBiome(int x, int y, int z, RetrievalMode retrievalMode)
    {
        return worldModifiers.biomes.getBiome(x, y, z, retrievalMode, true);
    }
    /**
     * Get the biome at a position in the filter box
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param smooth Whether to smooth the biomes
     * @return The biome at the given coordinates
     */
    public Biome getBiome(int x, int y, int z, boolean smooth)
    {
        return worldModifiers.biomes.getBiome(x, y, z, RetrievalMode.LAST_SWAPPED, smooth);
    }
    /**
     * Get the biome at a position in the filter box
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param retrievalMode The {@link RetrievalMode} to use when getting the biome
     * @param smooth Whether to smooth the biomes
     * @return The biome at the given coordinates
     */
    public Biome getBiome(int x, int y, int z, RetrievalMode retrievalMode, boolean smooth)
    {
        return worldModifiers.biomes.getBiome(x, y, z, retrievalMode, smooth);
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
     * Set the block at a position in the filter box to a {@link BlockType}.
     * This will only work if the position is within the filter box or
     * allowBlocksOutside is true
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param blockType The {@link BlockType} to change the position to
     * @return Whether the change was successful
     */
    public boolean setBlock(int x, int y, int z, BlockType blockType)
    {
        if (allowBlocksOutside || isPositionInBox(x, y, z))
        {
            worldModifiers.blocks.setBlock(x, y, z, blockType);
            return true;
        }
        else return false;
    }
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
     * Set the block at a position in the filter box to a random entry in a {@link BlockPalette}.
     * This will only work if the position is within the filter box
     * @param pos The {@link BlockPos} to change
     * @param palette The {@link BlockPalette} to change the position to
     * @return Whether the change was successful
     */
    public boolean setBlock(BlockPos pos, BlockPalette palette) { return setBlock(pos.x, pos.y, pos.z, palette); }
    /**
     * Set the block at a position in the filter box to a {@link BlockType}.
     * This will only work if the position is within the filter box or
     * allowBlocksOutside is true
     * @param pos The {@link BlockPos} to change
     * @param blockType The {@link BlockType} to change the position to
     * @return Whether the change was successful
     */
    public boolean setBlock(BlockPos pos, BlockType blockType) { return setBlock(pos.x, pos.y, pos.z, blockType); }
    /**
     * Set the block at a position in the filter box to a {@link Block}.
     * This will only work if the position is within the filter box or
     * allowBlocksOutside is true
     * @param pos The {@link BlockPos} to change
     * @param block The {@link Block} to change the position to
     * @return Whether the change was successful
     */
    public boolean setBlock(BlockPos pos, Block block) { return setBlock(pos.x, pos.y, pos.z, block); }
    /**
     * Set the biome at a position in the filter box to a {@link Biome}.
     * This will only work if the position is within the filter box or
     * allowBlocksOutside is true
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param biome The {@link Biome} to change the position to
     * @return Whether the change was successful
     */
    public boolean setBiome(int x, int y, int z, Biome biome)
    {
        if (allowBlocksOutside || isPositionInBox(x, y, z))
        {
            worldModifiers.biomes.setBiome(x, y, z, biome);
            return true;
        }
        else return false;
    }
    /**
     * Add or modify an {@link Entity} in the region
     * @param entity The {@link Entity} to add or modify
     */
    public void commitEntityChanges(Entity entity)
    {
        worldModifiers.entities.commitEntityChanges(entity);
    }

    /**
     * Create a {@link Stream} of all {@link BlockPos block positions} within the region
     * @return The {@link Stream} of {@link BlockPos}
     */
    public Stream<BlockPos> streamBlocks()
    {
        return StreamSupport.stream(iterateBlocks(min, max).spliterator(), false);
    }

    /**
     * Create a {@link Stream} of all {@link BlockPos block positions} within a rectangular
     * region defined by a pair of corners
     * @param min The minimum {@link BlockPos}
     * @param max The maximum {@link BlockPos}
     * @return The {@link Stream} of {@link BlockPos}
     */
    public static Stream<BlockPos> streamBlocks(BlockPos min, BlockPos max)
    {
        return StreamSupport.stream(iterateBlocks(min, max).spliterator(), false);
    }
    /**
     * Create a {@link Iterable} of all {@link BlockPos block positions} within a rectangular
     * region defined by a pair of corners
     * @param min The minimum {@link BlockPos}
     * @param max The maximum {@link BlockPos}
     * @return The {@link Iterable} of {@link BlockPos}
     */
    public static Iterable<BlockPos> iterateBlocks(BlockPos min, BlockPos max)
    {
        return () -> new Iterator<>()
        {
            private final BlockPos size = new BlockPos(max.x - min.x + 1, max.y - min.y + 1, max.z - min.z + 1);
            private final int volume = size.x * size.y * size.z;
            private int index = 0;
            private int x = min.x;
            private int y = min.y;
            private int z = min.z;

            @Override
            public boolean hasNext()
            {
                return index < volume;
            }

            @Override
            public synchronized BlockPos next()
            {
                if (index >= volume)
                {
                    return null;
                }

                BlockPos pos = new BlockPos(x, y, z);

                index++;
                z++;
                if (z > max.z)
                {
                    z = min.z;
                    x++;
                    if (x > max.x)
                    {
                        x = min.x;
                        y++;
                        if (y > max.y)
                        {
                            y = min.y;
                        }
                    }
                }

                return pos;
            }
        };
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
                    consumer.accept(x, y, z, getBlockType(x, y, z, retrievalMode));
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
