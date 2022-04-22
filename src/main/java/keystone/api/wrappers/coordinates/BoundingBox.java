package keystone.api.wrappers.coordinates;

import net.minecraft.util.math.Box;

/**
 * A wrapper for a Minecraft Box
 */
public class BoundingBox
{
    public interface CoordinateConsumer { void accept(int x, int y, int z); }

    private Box bb;
    public final double minX, minY, minZ;
    public final double maxX, maxY, maxZ;
    public final double centerX, centerY, centerZ;

    //region INTERNAL USE ONLY, DO NOT USE IN FILTERS
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * @param minecraftBB The Minecraft Box for the wrapper
     */
    public BoundingBox(Box minecraftBB)
    {
        this.bb = minecraftBB;
        this.minX = minecraftBB.minX;
        this.minY = minecraftBB.minY;
        this.minZ = minecraftBB.minZ;
        this.maxX = minecraftBB.maxX;
        this.maxY = minecraftBB.maxY;
        this.maxZ = minecraftBB.maxZ;
        this.centerX = minecraftBB.getCenter().x;
        this.centerY = minecraftBB.getCenter().y;
        this.centerZ = minecraftBB.getCenter().z;
    }

    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * @param min The minimum Minecraft BlockPos contained within the bounding box
     * @param max The maximum Minecraft BlockPos contained within the bounding box
     */
    public BoundingBox(net.minecraft.util.math.Vec3i min, net.minecraft.util.math.Vec3i max)
    {
        this(new Box(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1));
    }
    /**
     * <p>INTERNAL USE ONLY, DO NOT USE IN FILTERS</p>
     * @return The Minecraft Box
     */
    public Box getMinecraftBoundingBox()
    {
        return this.bb;
    }
    //endregion
    //region API
    /**
     * @param min The minimum {@link BlockPos} contained within the bounding box
     * @param max The maximum {@link BlockPos} contained within the bounding box
     */
    public BoundingBox(BlockPos min, BlockPos max)
    {
        this(new Box(min.getMinecraftBlockPos(), max.getMinecraftBlockPos().add(1, 1, 1)));
    }
    /**
     * @param minX The minimum x-coordinate contained within the bounding box
     * @param minY The minimum y-coordinate contained within the bounding box
     * @param minZ The minimum z-coordinate contained within the bounding box
     * @param maxX The maximum x-coordinate contained within the bounding box
     * @param maxY The maximum y-coordinate contained within the bounding box
     * @param maxZ The maximum z-coordinate contained within the bounding box
     */
    public BoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
    {
        this(new Box(minX, minY, minZ, maxX, maxY, maxZ));
    }

    /**
     * @param x The x-coordinate
     * @param y The y-coordinate
     * @param z The z-coordinate
     * @return Whether this bounding box contains a given point
     */
    public boolean contains(double x, double y, double z) { return bb.contains(x, y, z); }
    /**
     * @return The size of the bounding box along the x-axis
     */
    public double getXSize() { return maxX - minX; }
    /**
     * @return The size of the bounding box along the y-axis
     */
    public double getYSize() { return maxY - minY; }
    /**
     * @return The size of the bounding box along the z-axis
     */
    public double getZSize() { return maxZ - minZ; }

    /**
     * Run a {@link CoordinateConsumer} for each block coordinate within the bounding box
     * @param consumer The {@link CoordinateConsumer} to run
     */
    public void forEachCoordinate(CoordinateConsumer consumer)
    {
        for (int x = (int)minX; x < maxX; x++)
        {
            for (int y = (int)minY; y < maxY; y++)
            {
                for (int z = (int)minZ; z < maxZ; z++)
                {
                    consumer.accept(x, y, z);
                }
            }
        }
    }
    //endregion
}
