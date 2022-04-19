import keystone.api.WorldRegion;
import keystone.api.filters.StructureFilter;
import keystone.api.variables.FloatRange;
import keystone.api.variables.IntRange;
import keystone.api.variables.Variable;
import keystone.api.wrappers.blocks.BlockMask;
import keystone.api.wrappers.blocks.BlockPalette;
import keystone.api.wrappers.blocks.BlockType;
import keystone.api.wrappers.coordinates.Axis;
import keystone.api.wrappers.coordinates.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class Forester extends StructureFilter
{
    //region Enums
    public enum TreeType
    {
        //PROCEDURAL,
        NORMAL,
        BAMBOO,
        //STICKLY,
        ROUND,
        CONE,
        RAINFOREST,
        MANGROVE;

        public boolean requireButtresses() { return this == MANGROVE; }
    }
    public enum RootType
    {
        NORMAL,
        GROW_TO_STONE,
        HANGING,
        NONE;

        public BlockMask getMaterialCheck(Forester forester)
        {
            switch (this)
            {
                case GROW_TO_STONE: return forester.stoneMask;
                case HANGING: return forester.airMask;
                default: return null;
            }
        }
    }
    //endregion
    //region Tree Classes
    /**
     * Base tree class, used to define the shape of a tree's trunk and foliage
     */
    public abstract class Tree
    {
        protected final Forester forester;
        protected final BlockType log;
        protected final BlockType foliage;
        protected final int x;
        protected final int y;
        protected final int z;
        protected final int height;

        public Tree(Forester forester, int x, int y, int z)
        {
            this.forester = forester;
            BlockType[] palette = forester.resolvePalettes(forester.logPalette, forester.leavesPalette);
            this.log = palette[0];
            this.foliage = palette[1];

            this.x = x;
            this.y = y;
            this.z = z;
            this.height = forester.height + forester.random.nextInt(2 * forester.heightVariation + 1) - forester.heightVariation;
        }

        public void prepare(WorldRegion region) {}
        public abstract void makeTrunk(WorldRegion region);
        public abstract void makeFoliage(WorldRegion region);
    }

    /**
     * Abstract tree with a single width trunk
     */
    public abstract class StickTree extends Tree
    {
        public StickTree(Forester forester, int x, int y, int z) { super(forester, x, y, z); }

        @Override
        public void makeTrunk(WorldRegion region)
        {
            for (int i = 0; i < height; i++) region.setBlock(x, y + i, z, log);
        }
    }
    public class NormalTree extends StickTree
    {
        public NormalTree(Forester forester, int x, int y, int z) { super(forester, x, y, z); }

        @Override
        public void makeFoliage(WorldRegion region)
        {
            int topY = y + height - 1;
            int start = topY - 2;
            int end = topY + 1;
            for (int layer = start; layer <= end; layer++)
            {
                // First two layers have a radius of 2, last 2 have a radius of 1
                int radius = layer > start + 1 ? 1 : 2;
                for (int dx = -radius; dx <= radius; dx++)
                {
                    for (int dz = -radius; dz <= radius; dz++)
                    {
                        // Randomly ignore edges of blob
                        if (Math.abs(dx) == Math.abs(dz) && Math.abs(dx) == radius && forester.random.nextFloat() > 0.618f) continue;
                        region.setBlock(x + dx, layer, z + dz, foliage);
                    }
                }
            }
        }
    }
    public class BambooTree extends StickTree
    {
        public BambooTree(Forester forester, int x, int y, int z) { super(forester, x, y, z); }

        @Override
        public void makeFoliage(WorldRegion region)
        {
            for (int layer = y; layer <= y + height; layer++)
            {
                for (int i = 0; i < 2; i++)
                {
                    int dx = forester.random.nextBoolean() ? -1 : 1;
                    int dz = forester.random.nextBoolean() ? -1 : 1;
                    region.setBlock(x + dx, layer, z + dz, foliage);
                }
            }
        }
    }

    /**
     * Base tree class used for large tree types. Has roots, branches, and many
     * foliage clusters. Define shape by overriding {@link ProceduralTree#getFoliageRadius(int)},
     * {@link ProceduralTree#preparePass()}, and the foliageShape passed into the constructor
     */
    public abstract class ProceduralTree extends Tree
    {
        protected float trunkRadius;
        protected float trunkHeight;
        protected float branchDensity;
        protected float branchSlope;
        protected float[] foliageLayerRadii;
        protected List foliageCenters;

        public ProceduralTree(Forester forester, int x, int y, int z, float branchSlope, float[] foliageLayerRadii)
        {
            super(forester, x, y, z);
            this.branchSlope = branchSlope;
            this.foliageLayerRadii = foliageLayerRadii;
            this.foliageCenters = new ArrayList<>();
        }

        protected void placeCrossSection(float centerX, float centerY, float centerZ, float radius, Axis axis, BlockType blockType, WorldRegion region)
        {
            radius = (int)(radius + 0.618f);
            if (radius <= 0) return;

            for (float i = -radius + 0.5f; i <= radius + 0.5f; i++)
            {
                for (float j = -radius + 0.5f; j <= radius + 0.5f; j++)
                {
                    float distSqr = i * i + j * j;
                    if (distSqr > radius * radius) continue;

                    switch (axis)
                    {
                        case X: region.setBlock((int)Math.floor(centerX), (int)Math.floor(centerY + i), (int)Math.floor(centerZ + j), blockType); break;
                        case Y: region.setBlock((int)Math.floor(centerX + i), (int)Math.floor(centerY), (int)Math.floor(centerZ + j), blockType); break;
                        case Z: region.setBlock((int)Math.floor(centerX + i), (int)Math.floor(centerY + j), (int)Math.floor(centerZ), blockType); break;
                    }
                }
            }
        }

        /**
         * @param layer The layer of the tree the foliage is placed on
         * @return The radius of a foliage at a given layer
         */
        protected float getFoliageRadius(int layer)
        {
            if (forester.random.nextFloat() < 100.0f / (height * height) && layer < trunkHeight) return height * 0.12f;
            else return Float.NaN;
        }

        protected void placeFoliageCluster(float centerX, float centerY, float centerZ, WorldRegion region)
        {
            for (int layer = 0; layer < foliageLayerRadii.length; layer++)
            {
                placeCrossSection(centerX, centerY + layer, centerZ, foliageLayerRadii[layer], Axis.Y, foliage, region);
            }
        }

        protected void placeTaperedCylinder(float startX, float startY, float startZ, float endX, float endY, float endZ, float startRadius, float endRadius, BlockType blockType, WorldRegion region)
        {
            // Calculate difference between end and start
            int deltaX = (int)(endX - startX);
            int deltaY = (int)(endY - startY);
            int deltaZ = (int)(endZ - startZ);

            // Calculate max difference and which axis it is on, and which direction
            int maxDelta = Math.abs(deltaX);
            int maxDeltaSign = deltaX >= 0 ? 1 : -1;
            Axis axis = Axis.X;
            if (deltaY > maxDelta)
            {
                maxDelta = Math.abs(deltaY);
                maxDeltaSign = deltaY >= 0 ? 1 : -1;
                axis = Axis.Y;
            }
            if (deltaZ > maxDelta)
            {
                maxDelta = Math.abs(deltaZ);
                maxDeltaSign = deltaZ >= 0 ? 1 : -1;
                axis = Axis.Z;
            }

            // Calculate side deltas and side Deltas for every layer along the primary axis
            int sideDelta1 = 0, sideDelta2 = 0;
            switch (axis)
            {
                case X:
                    sideDelta1 = deltaY;
                    sideDelta2 = deltaZ;
                    break;
                case Y:
                    sideDelta1 = deltaX;
                    sideDelta2 = deltaZ;
                    break;
                case Z:
                    sideDelta1 = deltaX;
                    sideDelta2 = deltaY;
                    break;
            }
            float sideLayerDelta1 = (float)sideDelta1 / (maxDelta * maxDeltaSign);
            float sideLayerDelta2 = (float)sideDelta2 / (maxDelta * maxDeltaSign);

            // Place cross-sections for each layer of the cylinder along the primary axis
            int endOffset = maxDelta * maxDeltaSign + maxDeltaSign;
            float[] center = new float[3];
            for (int primaryOffset = 0; endOffset < 0 ? primaryOffset > endOffset : primaryOffset < endOffset; primaryOffset += maxDeltaSign)
            {
                switch (axis)
                {
                    case X:
                        center[0] = startX + primaryOffset;
                        center[1] = startY + primaryOffset * sideLayerDelta1;
                        center[2] = startZ + primaryOffset * sideLayerDelta2;
                        break;
                    case Y:
                        center[1] = startY + primaryOffset;
                        center[0] = startX + primaryOffset * sideLayerDelta1;
                        center[2] = startZ + primaryOffset * sideLayerDelta2;
                        break;
                    case Z:
                        center[2] = startZ + primaryOffset;
                        center[0] = startX + primaryOffset * sideLayerDelta1;
                        center[1] = startY + primaryOffset * sideLayerDelta2;
                        break;
                }
                float radius = endRadius + (startRadius - endRadius) * (float)Math.abs(maxDelta * maxDeltaSign - primaryOffset) / (float)maxDelta;
                placeCrossSection(center[0], center[1], center[2], radius, axis, blockType, region);
            }
        }

        @Override
        public void makeFoliage(WorldRegion region)
        {
            for (Object obj : foliageCenters)
            {
                float[] center = (float[])obj;
                placeFoliageCluster(center[0], center[1], center[2], region);
            }
            for (Object obj : foliageCenters)
            {
                float[] center = (float[])obj;
                region.setBlock((int)Math.floor(center[0]), (int)Math.floor(center[1]), (int)Math.floor(center[2]), foliage);
            }
        }

        public void makeBranches(WorldRegion region)
        {
            float topY = y + (int)(trunkHeight + 0.5f);
            float endRadius = trunkRadius * (1 - trunkHeight / height);
            if (endRadius < 1) endRadius = 1;

            for (Object obj : foliageCenters)
            {
                float[] foliageCenter = (float[])obj;
                float distance = (float)Math.sqrt((foliageCenter[0] - x) * (foliageCenter[0] - x) + (foliageCenter[2] - z) * (foliageCenter[2] - z));
                float yDistance = foliageCenter[1] - y;
                float value = (float)((branchDensity * 220 * height) / Math.pow(yDistance + distance, 3));
                if (value < forester.random.nextFloat()) continue;

                float branchY;
                float baseSize;
                float slope = branchSlope + (0.5f - forester.random.nextFloat()) * 0.16f;
                if (foliageCenter[1] - distance * slope > topY)
                {
                    if (forester.random.nextFloat() < 1.0f / height) continue;
                    branchY = topY;
                    baseSize = endRadius;
                }
                else
                {
                    branchY = foliageCenter[1] - distance * slope;
                    baseSize = (endRadius + (trunkRadius - endRadius) * (topY - branchY) / trunkHeight);
                }

                float startSize = (baseSize * (1 + forester.random.nextFloat()) * .618f * (float)Math.pow(distance / height, 0.618f));
                float randomRadius = (float)Math.sqrt(forester.random.nextFloat()) * baseSize * 0.618f;
                float angle = (float)(forester.random.nextFloat() * 2 * Math.PI);
                int centerX = (int)(randomRadius * Math.sin(angle) + 0.5) + x;
                int centerZ = (int)(randomRadius * Math.cos(angle) + 0.5) + z;

                if (startSize < 1) startSize = 1;
                placeTaperedCylinder(centerX, (int)branchY, centerZ, foliageCenter[0], foliageCenter[1], foliageCenter[2], startSize, 1.0f, log, region);
            }
        }

        public void makeRoots(List rootBases, WorldRegion region)
        {
            // Root amount is proportional to foliage amount
            for (Object obj : foliageCenters)
            {
                float[] foliageCenter = (float[])obj;
                float distance = (float)Math.sqrt((foliageCenter[0] - x) * (foliageCenter[0] - x) + (foliageCenter[2] - z) * (foliageCenter[2] - z));
                float yDistance = foliageCenter[1] - y;
                float value = (float)((branchDensity * 220 * height) / Math.pow(yDistance + distance, 3));
                if (value < forester.random.nextFloat()) continue;

                // Select Random Root Base
                float[] rootBase = (float[])rootBases.get(forester.random.nextInt(rootBases.size()));
                float rootX = rootBase[0];
                float rootZ = rootBase[1];
                float rootBaseRadius = rootBase[2];

                // Offset root origin by a random radial amount
                float randomRadius = (float)Math.sqrt(forester.random.nextFloat()) * rootBaseRadius * 0.618f;
                float angle = (float)(forester.random.nextFloat() * 2 * Math.PI);
                int centerX = (int)(randomRadius * Math.sin(angle) + 0.5);
                int centerZ = (int)(randomRadius * Math.cos(angle) + 0.5);
                int centerY = (int)(forester.random.nextFloat() * rootBaseRadius * 0.5f);
                float startX = rootX + centerX;
                float startY = y + centerY;
                float startZ = rootZ + centerZ;

                // Calculate distance from root base to tip
                float offsetX = startX - foliageCenter[0];
                float offsetY = startY - foliageCenter[1];
                float offsetZ = startZ - foliageCenter[2];

                // Make Mangrove tree roots longer
                if (forester.treeType == TreeType.MANGROVE)
                {
                    offsetX = offsetX * 1.618f - 1.5f;
                    offsetY = offsetY * 1.618f - 1.5f;
                    offsetZ = offsetZ * 1.168f - 1.5f;
                }

                // Calculate root end and size
                float endX = startX + offsetX;
                float endY = startY + offsetY;
                float endZ = startZ + offsetZ;
                float rootStartSize = (rootBaseRadius * 0.618f * Math.abs(offsetY) / (height * 0.618f));
                if (rootStartSize < 1) rootStartSize = 1;
                float rootEndSize = 1;

                // Perform material check along distance for certain root types
                BlockMask materialCheck = forester.rootType.getMaterialCheck(forester);
                if (materialCheck != null)
                {
                    float offsetLength = (float)Math.sqrt(offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ);
                    if (offsetLength < 1) continue;

                    // Calculate material search settings and perform search
                    float rootMid = rootEndSize;
                    float directionX = offsetX / offsetLength;
                    float directionY = offsetY / offsetLength;
                    float directionZ = offsetZ / offsetLength;
                    float startDistance = (int)(forester.random.nextFloat() * 6 * Math.sqrt(rootStartSize) + 2.8);
                    float searchStartX = startX + startDistance * directionX;
                    float searchStartY = startY + startDistance * directionY;
                    float searchStartZ = startZ + startDistance * directionZ;
                    float searchDistance = startDistance + forester.distanceToBlock(searchStartX, searchStartY, searchStartZ, directionX, directionY, directionZ, materialCheck, region, offsetLength);

                    // If the distance is less than the root length, change root end point
                    if (searchDistance < offsetLength)
                    {
                        rootMid += (rootStartSize - rootEndSize) * (1 - searchDistance / offsetLength);
                        endX = startX + (int)(offsetX * searchDistance);
                        endY = startY + (int)(offsetY * searchDistance);
                        endZ = startZ + (int)(offsetZ * searchDistance);

                        // If root type is hanging, place remaining distance straight downward
                        if (forester.rootType == RootType.HANGING)
                        {
                            float remainingDistance = offsetLength - searchDistance;
                            float bottomX = endX;
                            float bottomY = endY + -(int)remainingDistance;
                            float bottomZ = endZ;
                            placeTaperedCylinder(endX, endY, endZ, bottomX, bottomY, bottomZ, rootMid, rootEndSize, log, region);
                        }

                        // Place main root part
                        placeTaperedCylinder(startX, startY, startZ, endX, endY, endZ, rootStartSize, rootMid, log, region);
                    }
                }
                else placeTaperedCylinder(startX, startY, startZ, endX, endY, endZ, rootStartSize, rootEndSize, log, region);
            }
        }

        @Override
        public void makeTrunk(WorldRegion region)
        {
            // Calculate trunk shape
            float midY = y + (int)(trunkHeight * 0.382f);
            float topY = y + (int)(trunkHeight + 0.5f);
            float endSizeFactor = trunkHeight / height;
            float startRadius;
            float midRadius = trunkRadius * (1 - endSizeFactor * 0.5f);
            float endRadius = trunkRadius * (1 - endSizeFactor);
            if (endRadius < 1) endRadius = 1;
            if (midRadius < endRadius) midRadius = endRadius;
            List rootBases = new ArrayList<>();

            // Make root buttresses if necessary
            if (forester.rootButtresses || forester.treeType.requireButtresses())
            {
                startRadius = trunkRadius * 0.8f;
                rootBases.add(new float[] { x, z, startRadius });
                float buttressRadius = trunkRadius * 0.382f;
                float posRadius = trunkRadius;
                if (forester.treeType == TreeType.MANGROVE) posRadius *= 2.618f;
                int buttressCount = (int)(Math.sqrt(trunkRadius) + 3.5);

                for (int i = 0; i < buttressCount; i++)
                {
                    float angle = (float)(forester.random.nextFloat() * 2 * Math.PI);
                    float thisPosRadius = posRadius * (0.9f + forester.random.nextFloat() * 0.2f);
                    float thisX = x + (int)(thisPosRadius * Math.sin(angle));
                    float thisZ = z + (int)(thisPosRadius * Math.cos(angle));
                    float thisButtressRadius = buttressRadius * (0.618f + forester.random.nextFloat());
                    if (thisButtressRadius < 1) thisButtressRadius = 1;
                    placeTaperedCylinder(thisX, y, thisZ, x, midY, z, thisButtressRadius, thisButtressRadius, log, region);
                    rootBases.add(new float[] { thisX, thisZ, thisButtressRadius });
                }
            }
            // If buttresses are disabled, set trunk radius to normal size
            else
            {
                startRadius = trunkRadius;
                rootBases.add(new float[] { x, z, startRadius });
            }

            // Build trunk
            placeTaperedCylinder(x, y, z, x, midY, z, startRadius, midRadius, log, region);
            placeTaperedCylinder(x, midY, z, x, topY, z, midRadius, endRadius, log, region);

            // Build branches and roots, if enabled
            makeBranches(region);
            if (forester.rootType != RootType.NONE) makeRoots(rootBases, region);

            // Hollow trunk, if enabled and large enough
            if (trunkRadius > 2 && forester.hollowTrunk)
            {
                float wallThickness = (1 + trunkRadius * 0.1f * forester.random.nextFloat());
                if (wallThickness < 1.3f) wallThickness = 1.3f;

                float baseRadius = trunkRadius - wallThickness;
                if (baseRadius < 1) baseRadius = 1;
                midRadius = midRadius - wallThickness;
                endRadius = endRadius - wallThickness;

                int baseOffset = (int)wallThickness;
                float startX = x + forester.random.nextInt(2 * baseOffset + 1) - baseOffset;
                float startZ = z + forester.random.nextInt(2 * baseOffset + 1) - baseOffset;
                placeTaperedCylinder(startX, y, startZ, x, midY, z, baseRadius, midRadius, forester.air, region);
                int hollowTopY = (int)(topY + trunkRadius + 1.5f);
                placeTaperedCylinder(x, midY, z, x, hollowTopY, z, midRadius, endRadius, air, region);
            }
        }

        @Override
        public void prepare(WorldRegion region)
        {
            // Set Trunk Radius and Height
            trunkRadius = (float)(0.618 * Math.sqrt(height * forester.trunkThickness));
            if (trunkRadius < 1) trunkRadius = 1;
            trunkHeight = height;
            int yEnd = (int)(y + trunkHeight);

            // Set branch and foliage settings
            branchDensity = forester.branchDensity / forester.foliageDensity;
            float topY = y + (int)(trunkHeight + 0.5f);
            foliageCenters.clear();
            int foliageClustersPerLayer = (int)(1.5f + (forester.foliageDensity * height / 19.0f) * (forester.foliageDensity * height / 19.0f));
            if (foliageClustersPerLayer < 1) foliageClustersPerLayer = 1;

            for (int layer = yEnd; layer > y; layer--)
            {
                for (int cluster = 0; cluster < foliageClustersPerLayer; cluster++)
                {
                    float foliageRadius = getFoliageRadius(layer - y);
                    if (Float.isNaN(foliageRadius)) continue;

                    float radius = (float)(Math.sqrt(forester.random.nextFloat()) + 0.328f) * foliageRadius;
                    float theta = (float)(forester.random.nextFloat() * 2 * Math.PI);
                    int centerX = (int)(radius * Math.sin(theta)) + x;
                    int centerZ = (int)(radius * Math.cos(theta)) + z;
                    foliageCenters.add(new float[] { centerX, layer, centerZ });
                }
            }
        }
    }
    public class RoundTree extends ProceduralTree
    {
        public RoundTree(Forester forester, int x, int y, int z)
        {
            super(forester, x, y, z, 0.382f, new float[] { 2, 3, 3, 2.5f, 1.6f });
        }

        @Override
        public void prepare(WorldRegion region)
        {
            super.prepare(region);
            trunkRadius = trunkRadius * 0.8f;
            trunkHeight *= forester.trunkHeight;
        }

        @Override
        protected float getFoliageRadius(int layer)
        {
            float twigs = super.getFoliageRadius(layer);
            if (!Float.isNaN(twigs)) return twigs;
            if (layer < height * (0.282f + 0.1f * (float)Math.sqrt(forester.random.nextFloat()))) return Float.NaN;
            float radius = height / 2.0f;
            float adj = height / 2.0f - layer;

            float dist;
            if (adj == 0) dist = radius;
            else if (Math.abs(adj) >= radius) dist = 0;
            else dist = (float)Math.sqrt(radius * radius - adj * adj);

            dist *= 0.618f;
            return dist;
        }
    }
    public class ConeTree extends ProceduralTree
    {
        public ConeTree(Forester forester, int x, int y, int z)
        {
            super(forester, x, y, z, 0.15f, new float[] { 3, 2.6f, 2, 1 });
        }

        @Override
        public void prepare(WorldRegion region)
        {
            super.prepare(region);
            trunkRadius = trunkRadius * 0.5f;
        }

        @Override
        protected float getFoliageRadius(int layer)
        {
            float twigs = super.getFoliageRadius(layer);
            if (!Float.isNaN(twigs)) return twigs;

            if (layer < height * (0.25f + 0.05f * Math.sqrt(forester.random.nextFloat()))) return Float.NaN;

            float radius = (height - layer) * 0.382f;
            if (radius < 0) radius = 0;
            return radius;
        }
    }
    public class RainforestTree extends ProceduralTree
    {
        public RainforestTree(Forester forester, int x, int y, int z)
        {
            super(forester, x, y, z, 1.0f, new float[] { 3.4f, 2.6f });
        }

        @Override
        public void prepare(WorldRegion region)
        {
            super.prepare(region);
            trunkRadius = trunkRadius * 0.382f;
            trunkHeight = trunkHeight * 0.9f;
        }

        @Override
        protected float getFoliageRadius(int layer)
        {
            if (layer < height * 0.8f)
            {
                if (forester.height < height)
                {
                    float twigs = super.getFoliageRadius(layer);
                    if (!Float.isNaN(twigs) && forester.random.nextFloat() < 0.07f) return twigs;
                }
                return Float.NaN;
            }
            else
            {
                float width = height * 0.382f;
                float topDistance = (height - layer) / (height * 0.2f);
                float distance = width * (0.618f + topDistance) * (0.618f + forester.random.nextFloat()) * 0.382f;
                return distance;
            }
        }
    }
    public class MangroveTree extends RoundTree
    {
        public MangroveTree(Forester forester, int x, int y, int z) { super(forester, x, y, z); }

        @Override
        public void prepare(WorldRegion region)
        {
            branchSlope = 1.0f;
            trunkRadius = trunkRadius * 0.618f;
        }

        @Override
        protected float getFoliageRadius(int layer)
        {
            float val = super.getFoliageRadius(layer);
            if (Float.isNaN(val)) return val;
            val = val * 1.618f;
            return val;
        }
    }
    //endregion
    //region Variables
    @Variable public BlockMask groundMask = whitelist("minecraft:grass_block");
    @Variable public BlockMask stoneMask = whitelist("minecraft:stone", "minecraft:granite", "minecraft:diorite", "minecraft:andesite");
    @Variable public BlockPalette logPalette = palette("minecraft:oak_log");
    @Variable public BlockPalette leavesPalette = palette("minecraft:oak_leaves");
    @Variable public TreeType treeType = TreeType.NORMAL;
    @Variable public RootType rootType = RootType.NORMAL;
    @Variable @IntRange(min = 1, max = 100, scrollStep = 5) public int treeDistance = 20;
    @Variable public int height = 35;
    @Variable public int heightVariation = 12;
    @Variable @FloatRange(min = 0.0f, max = 1.0f, scrollStep = 0.1f) public float trunkHeight = 0.7f;
    @Variable @FloatRange(min = 0.0f, max = 10.0f, scrollStep = 0.1f) public float trunkThickness = 1.0f;
    @Variable @FloatRange(min = 0.0f, max = 100.0f, scrollStep = 0.5f) public float branchDensity = 1.0f;
    @Variable @FloatRange(min = 0.0f, max = 10.0f, scrollStep = 0.1f) public float foliageDensity = 1.0f;
    @Variable public boolean hollowTrunk = false;
    @Variable public boolean rootButtresses = false;

    public BlockMask airMask = whitelist("minecraft:air");
    public BlockType air = block("minecraft:air").blockType();

    private List trees = new ArrayList();
    //endregion

    @Override public int getStructureSeparation() { return treeDistance; }
    @Override public int getStructureSteps() { return 4; }
    @Override public void preparePass() { trees.clear(); }

    @Override
    public void processStructure(Vector2f coordinate, WorldRegion region)
    {
        int y = region.getTopBlock((int)Math.floor(coordinate.x), (int)Math.floor(coordinate.y));
        trees.add(getTreeInstance(coordinate.x, y, coordinate.y));
    }
    @Override
    public void postProcessStructures(WorldRegion region)
    {
        for (Object obj : trees)
        {
            Tree tree = (Tree)obj;

            tree.prepare(region);
            nextStep();

            tree.makeFoliage(region);
            nextStep();
        }

        for (Object obj : trees)
        {
            Tree tree = (Tree)obj;
            tree.makeTrunk(region);
            nextStep();
        }
    }

    public int distanceToBlock(float startX, float startY, float startZ, float dirX, float dirY, float dirZ, BlockMask mask, WorldRegion region, float limit)
    {
        float currentX = startX + 0.5f;
        float currentY = startY + 0.5f;
        float currentZ = startZ + 0.5f;
        int iterations = 0;

        while (true)
        {
            int x = (int)Math.floor(currentX);
            int y = (int)Math.floor(currentY);
            int z = (int)Math.floor(currentZ);
            BlockType blockType = region.getBlockType(x, y, z);

            if (mask.valid(blockType)) break;
            else
            {
                currentX += dirX;
                currentY += dirY;
                currentZ += dirZ;
                iterations++;
            }

            if (limit > 0 && iterations > limit) break;
        }

        return iterations;
    }
    public Tree getTreeInstance(float x, float y, float z)
    {
        x = (int)Math.floor(x);
        y = (int)Math.floor(y);
        z = (int)Math.floor(z);

        switch (treeType)
        {
            case NORMAL: return new NormalTree(this, (int)x, (int)y, (int)z);
            case BAMBOO: return new BambooTree(this, (int)x, (int)y, (int)z);
            case ROUND: return new RoundTree(this, (int)x, (int)y, (int)z);
            case CONE: return new ConeTree(this, (int)x, (int)y, (int)z);
            case RAINFOREST: return new RainforestTree(this, (int)x, (int)y, (int)z);
            case MANGROVE: return new MangroveTree(this, (int)x, (int)y, (int)z);
            default: return null;
        }
    }
}