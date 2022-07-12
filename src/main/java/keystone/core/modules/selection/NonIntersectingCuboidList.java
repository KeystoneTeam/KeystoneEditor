package keystone.core.modules.selection;

import keystone.core.renderer.shapes.Cuboid;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class NonIntersectingCuboidList<T extends Cuboid>
{
    private static final byte NONE = (byte)0;
    private static final byte SPLITTING = (byte)1;
    private static final byte ORIGINAL = (byte)2;
    private static final byte INTERSECTION = (byte)3;

    public interface CuboidBuilder<T extends Cuboid>
    {
        T build(int minX, int minY, int minZ, int maxX, int maxY, int maxZ);
    }

    private List<T> cuboids = new ArrayList<>();
    private final CuboidBuilder<T> builder;

    public NonIntersectingCuboidList(CuboidBuilder<T> builder)
    {
        this.builder = builder;
    }

    public void addAll(Collection<T> cuboids) { for (T cuboid : cuboids) add(cuboid); }
    public void add(T cuboid)
    {
        List<T> removeList = new ArrayList<>();

        for (T test : cuboids)
        {
            if (envelops(test, cuboid)) return;
            else if (envelops(cuboid, test)) removeList.add(test);
            else if (intersects(cuboid, test))
            {
                cuboids.removeAll(removeList);
                addAll(split(cuboid, test));
                return;
            }
        }

        cuboids.removeAll(removeList);
        cuboids.add(cuboid);
    }
    public void finish() { cuboids = Collections.unmodifiableList(cuboids); }

    public List<T> getContents() { return cuboids; }

    private boolean envelops(T a, T b)
    {
        return a.getMin().getX() <= b.getMin().getX() && a.getMin().getY() <= b.getMin().getX() && a.getMin().getZ() <= b.getMin().getZ() &&
                a.getMax().getX() >= b.getMax().getX() && a.getMax().getY() >= b.getMax().getY() && a.getMax().getZ() >= b.getMax().getZ();
    }
    private boolean intersects(T a, T b)
    {
        return a.getBoundingBox().getMinecraftBoundingBox().intersects(b.getBoundingBox().getMinecraftBoundingBox());
    }
    private List<T> split(T splitting, T original)
    {
        Region bounds = Region.boundsOf(splitting, original);
        Region intersection = Region.fromIntersection(splitting, original);
        List<Region> regions = bounds.split(intersection, splitting, original, SPLITTING);

        List<T> cuboids = new ArrayList<>();
        for (Region region : regions)
        {
            cuboids.add(builder.build(region.minX, region.minY, region.minZ, region.maxX, region.maxY, region.maxZ));
        }
        return cuboids;
    }

    private static class Region
    {
        public final int minX, minY, minZ;
        public final int maxX, maxY, maxZ;
        public final Vec3i size;
        public byte value;

        private Region(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, byte value)
        {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
            this.size = new Vec3i(maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);
            this.value = value;
        }
        private Region(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Cuboid splitting, Cuboid original)
        {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
            this.size = new Vec3i(maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);

            Box box = new Box(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
            byte value = NONE;
            if (box.intersects(splitting.getBoundingBox().getMinecraftBoundingBox())) value += SPLITTING;
            if (box.intersects(original.getBoundingBox().getMinecraftBoundingBox())) value += ORIGINAL;
            this.value = value;
        }

        public static Region fromIntersection(Cuboid splitting, Cuboid original)
        {
            int minX = Math.max(splitting.getMin().getX(), original.getMin().getX());
            int minY = Math.max(splitting.getMin().getY(), original.getMin().getY());
            int minZ = Math.max(splitting.getMin().getZ(), original.getMin().getZ());
            int maxX = Math.min(splitting.getMax().getX(), original.getMax().getX());
            int maxY = Math.min(splitting.getMax().getY(), original.getMax().getY());
            int maxZ = Math.min(splitting.getMax().getZ(), original.getMax().getZ());

            return new Region(minX, minY, minZ, maxX, maxY, maxZ, INTERSECTION);
        }
        public static Region boundsOf(Cuboid splitting, Cuboid original)
        {
            int minX = Math.min(splitting.getMin().getX(), original.getMin().getX());
            int minY = Math.min(splitting.getMin().getY(), original.getMin().getY());
            int minZ = Math.min(splitting.getMin().getZ(), original.getMin().getZ());
            int maxX = Math.max(splitting.getMax().getX(), original.getMax().getX());
            int maxY = Math.max(splitting.getMax().getY(), original.getMax().getY());
            int maxZ = Math.max(splitting.getMax().getZ(), original.getMax().getZ());
            return new Region(minX, minY, minZ, maxX, maxY, maxZ, NONE);
        }

        public List<Region> split(Region intersection, Cuboid splitting, Cuboid original, byte finding)
        {
            // Get coordinate sets
            List<Integer> xCoordinateSet = new ArrayList<>();
            List<Integer> yCoordinateSet = new ArrayList<>();
            List<Integer> zCoordinateSet = new ArrayList<>();
            
            xCoordinateSet.add(minX);
            yCoordinateSet.add(minY);
            zCoordinateSet.add(minZ);
            
            if (minX < intersection.minX)
            {
                xCoordinateSet.add(intersection.minX - 1);
                xCoordinateSet.add(intersection.minX);
            }
            if (maxX > intersection.maxX)
            {
                xCoordinateSet.add(intersection.maxX);
                xCoordinateSet.add(intersection.maxX + 1);
            }
            if (minY < intersection.minY)
            {
                yCoordinateSet.add(intersection.minY - 1);
                yCoordinateSet.add(intersection.minY);
            }
            if (maxY > intersection.maxY)
            {
                yCoordinateSet.add(intersection.maxY);
                yCoordinateSet.add(intersection.maxY + 1);
            }
            if (minZ < intersection.minZ)
            {
                zCoordinateSet.add(intersection.minZ - 1);
                zCoordinateSet.add(intersection.minZ);
            }
            if (maxZ > intersection.maxZ)
            {
                zCoordinateSet.add(intersection.maxZ);
                zCoordinateSet.add(intersection.maxZ + 1);
            }

            xCoordinateSet.add(maxX);
            yCoordinateSet.add(maxY);
            zCoordinateSet.add(maxZ);

            // Build region grid
            Region[][][] grid = new Region[xCoordinateSet.size() / 2][yCoordinateSet.size() / 2][zCoordinateSet.size() / 2];
            for (int x = 0; x < grid.length; x++)
            {
                for (int y = 0; y < grid[x].length; y++)
                {
                    for (int z = 0; z < grid[x][y].length; z++)
                    {
                        grid[x][y][z] = new Region(xCoordinateSet.get(2 * x), yCoordinateSet.get(2 * y), zCoordinateSet.get(2 * z), xCoordinateSet.get(2 * x + 1), yCoordinateSet.get(2 * y + 1), zCoordinateSet.get(2 * z + 1), splitting, original);
                    }
                }
            }

            // Merge regions
            List<Region> regions = new ArrayList<>();
            int lastSize = 0;
            do
            {
                lastSize = regions.size();
                Region region = Region.find(grid, finding);
                if (region != null) regions.add(region);
            }
            while (regions.size() != lastSize);
            return regions;
        }

        private static Region find(Region[][][] grid, byte value)
        {
            boolean foundStart = false;
            int minRegionX = 0, minRegionY = 0, minRegionZ = 0;
            int maxRegionX = 0, maxRegionY = 0, maxRegionZ = 0;

            // Find start
            findStart:
            for (int x = 0; x < grid.length; x++)
            {
                for (int y = 0; y < grid[x].length; y++)
                {
                    for (int z = 0; z < grid[x][y].length; z++)
                    {
                        Region region = grid[x][y][z];
                        if (region.value == value)
                        {
                            minRegionX = x;
                            minRegionY = y;
                            minRegionZ = z;
                            maxRegionX = x;
                            maxRegionY = y;
                            maxRegionZ = z;
                            foundStart = true;
                            break findStart;
                        }
                    }
                }
            }
            if (!foundStart) return null;

            // Find maxRegionX
            for (int x = minRegionX + 1; x < grid.length; x++)
            {
                if (grid[x][minRegionY][minRegionZ].value == value) maxRegionX = x;
                else break;
            }
            // Find maxRegionY
            for (int y = minRegionY; y < grid[minRegionX].length; y++)
            {
                boolean fullRow = true;
                for (int x = minRegionX; x <= maxRegionX; x++)
                {
                    if (grid[x][y][minRegionZ].value != value)
                    {
                        fullRow = false;
                        break;
                    }
                }

                if (fullRow) maxRegionY = y;
                else break;
            }
            // Find maxRegionZ
            for (int z = minRegionZ; z < grid[minRegionX][minRegionY].length; z++)
            {
                boolean fullSlice = true;
                maxZLoop:
                for (int y = minRegionY; y <= maxRegionY; y++)
                {
                    for (int x = minRegionX; x <= maxRegionX; x++)
                    {
                        if (grid[x][y][z].value != value)
                        {
                            fullSlice = false;
                            break maxZLoop;
                        }
                    }
                }

                if (fullSlice) maxRegionZ = z;
                else break;
            }

            // Get min and max regions
            Region minRegion = grid[minRegionX][minRegionY][minRegionZ];
            Region maxRegion = grid[maxRegionX][maxRegionY][maxRegionZ];

            // Set values of regions to none
            for (int x = minRegionX; x <= maxRegionX; x++)
            {
                for (int y = minRegionY; y <= maxRegionY; y++)
                {
                    for (int z = minRegionZ; z <= maxRegionZ; z++)
                    {
                        grid[x][y][z].value = NONE;
                    }
                }
            }

            // Build merged region
            return new Region(minRegion.minX, minRegion.minY, minRegion.minZ, maxRegion.maxX, maxRegion.maxY, maxRegion.maxZ, value);
        }

        @Override
        public String toString()
        {
            int volume = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
            return String.format("Region[V:%s, O: %s]", volume, value);
        }
    }
}
