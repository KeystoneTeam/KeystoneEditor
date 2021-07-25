package keystone.api.wrappers.coordinates;

import net.minecraft.util.math.AxisAlignedBB;

/**
 * A wrapper for a Minecraft AxisAlignedBB
 */
public class BoundingBox
{
    private AxisAlignedBB bb;
    public final double minX, minY, minZ;
    public final double maxX, maxY, maxZ;

    //region INTERNAL USE ONLY, DO NOT USE IN FILTERS
    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param minecraftBB The Minecraft AxisAlignedBB for the wrapper
     */
    public BoundingBox(AxisAlignedBB minecraftBB)
    {
        this.bb = minecraftBB;
        this.minX = minecraftBB.minX;
        this.minY = minecraftBB.minY;
        this.minZ = minecraftBB.minZ;
        this.maxX = minecraftBB.maxX;
        this.maxY = minecraftBB.maxY;
        this.maxZ = minecraftBB.maxZ;
    }

    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @param min The minimum Minecraft BlockPos contained within the bounding box
     * @param max The maximum Minecraft BlockPos contained within the bounding box
     */
    public BoundingBox(net.minecraft.util.math.BlockPos min, net.minecraft.util.math.BlockPos max)
    {
        this(new AxisAlignedBB(min, max.offset(1, 1, 1)));
    }
    /**
     * INTERNAL USE ONLY, DO NOT USE IN FILTERS
     * @return The Minecraft AxisAlignedBB
     */
    public AxisAlignedBB getMinecraftBoundingBox()
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
        this(new AxisAlignedBB(min.getMinecraftBlockPos(), max.getMinecraftBlockPos().offset(1, 1, 1)));
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
        this(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
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
    //endregion
}
