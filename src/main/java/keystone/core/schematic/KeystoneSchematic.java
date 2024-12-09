package keystone.core.schematic;

import keystone.api.Keystone;
import keystone.api.enums.RetrievalMode;
import keystone.api.wrappers.entities.Entity;
import keystone.core.client.Player;
import keystone.core.math.BlockPosMath;
import keystone.core.modules.selection.SelectionBoundingBox;
import keystone.core.modules.world.WorldModifierModules;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.renderer.blocks.legacy.world.GhostBlocksWorld;
import keystone.core.schematic.extensions.ISchematicExtension;
import keystone.core.schematic.formats.KeystoneSchematicFormat;
import keystone.core.utils.PalettedArray;
import keystone.core.utils.RegistryLookups;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.apache.commons.lang3.function.TriConsumer;

import java.util.*;
import java.util.function.Consumer;

/**
 * A schematic containing block and entity data. Used for cloning and import/export operations
 */
public class KeystoneSchematic
{
    private final Vec3i size;
    private final PalettedArray<BlockState> blocks;
    private final Map<BlockPos, NbtCompound> tileEntities;
    private final Entity[] entities;
    private final Map<Identifier, ISchematicExtension> extensions;

    /**
     * @param size The size of the schematic
     * @param blocks The block state contents of the schematic
     * @param tileEntities A map of all tile entities in the schematic
     * @param entities The {@link Entity} contents of the schematic
     * @param extensions The {@link ISchematicExtension Extensions} stored in this schematic
     */
    public KeystoneSchematic(Vec3i size, PalettedArray<BlockState> blocks, Map<BlockPos, NbtCompound> tileEntities, Entity[] entities, Map<Identifier, ISchematicExtension> extensions)
    {
        this.size = size;
        this.blocks = blocks;
        this.tileEntities = tileEntities;
        this.entities = entities == null ? new Entity[0] : entities;
        for (Entity entity : this.entities) entity.breakMinecraftEntityConnection();
        this.extensions = extensions;
    }
    /**
     * Create a schematic from a selection box
     * @param box The {@link SelectionBoundingBox} to create the schematic from
     * @param worldModifiers The {@link WorldModifierModules} that the schematic contents is read from
     * @param retrievalMode The {@link RetrievalMode} used in reading the schematic contents
     * @return The generated {@link KeystoneSchematic}
     */
    public static KeystoneSchematic createFromSelection(SelectionBoundingBox box, WorldModifierModules worldModifiers, RetrievalMode retrievalMode)
    {
        return createFromCorners(box.getCorner1(), box.getCorner2(), worldModifiers, retrievalMode);
    }
    /**
     * Create a schematic from two corners
     * @param corner1 The first corner
     * @param corner2 The second corner
     * @param worldModifiers The {@link WorldModifierModules} that the schematic contents is read from
     * @param retrievalMode The {@link RetrievalMode} used in reading the schematic contents
     * @return The generated {@link KeystoneSchematic}
     */
    public static KeystoneSchematic createFromCorners(Vec3i corner1, Vec3i corner2, WorldModifierModules worldModifiers, RetrievalMode retrievalMode)
    {
        BlockPos min = new BlockPos(Math.min(corner1.getX(), corner2.getX()), Math.min(corner1.getY(), corner2.getY()), Math.min(corner1.getZ(), corner2.getZ()));
        BlockPos max = new BlockPos(Math.max(corner1.getX(), corner2.getX()), Math.max(corner1.getY(), corner2.getY()), Math.max(corner1.getZ(), corner2.getZ()));

        // Get size
        Vec3i size = new Vec3i(max.getX() - min.getX() + 1, max.getY() - min.getY() + 1, max.getZ() - min.getZ() + 1);

        // Get blocks
        PalettedArray<BlockState> blocks = new PalettedArray<>(size.getX() * size.getY() * size.getZ());
        Map<BlockPos, NbtCompound> tileEntities = new HashMap<>();
        int i = 0;
        for (int x = 0; x < size.getX(); x++)
        {
            for (int y = 0; y < size.getY(); y++)
            {
                for (int z = 0; z < size.getZ(); z++)
                {
                    BlockState blockState = worldModifiers.blocks.getBlockState(x + min.getX(), y + min.getY(), z + min.getZ(), retrievalMode);
                    blocks.set(i, blockState);
                    if (blockState.hasBlockEntity())
                    {
                        NbtCompound tileEntity = worldModifiers.blocks.getBlockData(x + min.getX(), y + min.getY(), z + min.getZ(), retrievalMode);
                        tileEntities.put(new BlockPos(x, y, z), tileEntity);
                    }
                    i++;
                }
            }
        }

        // Get entities
        Box box = new Box(Vec3d.of(min), Vec3d.of(max).add(1, 1, 1));
        List<Entity> entityList = worldModifiers.entities.getEntities(box, RetrievalMode.ORIGINAL);
        Entity[] entities = new Entity[entityList.size()];
        entities = entityList.toArray(entities);
        for (Entity entity : entities)
        {
            entity.move(-min.getX(), -min.getY(), -min.getZ());
            entity.breakMinecraftEntityConnection();
        }

        // Create schematic from data
        BlockBox blockBox = new BlockBox(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1);
        World world = Keystone.getModule(WorldCacheModule.class).getDimensionWorld(Player.getDimension());
        return new KeystoneSchematic(size, blocks, tileEntities, entities, KeystoneSchematicFormat.createExtensions(world, blockBox));
    }

    /**
     * Create a new {@link keystone.core.schematic.KeystoneSchematic} with the same size and contents as this one
     * @return The cloned {@link keystone.core.schematic.KeystoneSchematic}
     */
    public KeystoneSchematic clone()
    {
        return new KeystoneSchematic(new Vec3i(size.getX(), size.getY(), size.getZ()), blocks.copy(), Collections.unmodifiableMap(tileEntities), Arrays.copyOf(entities, entities.length), Collections.unmodifiableMap(new HashMap<>(extensions)));
    }

    /**
     * Convert a relative block position to an array index
     * @param relativePos The relative block position
     * @return The array index, or -1 if the position is outside the schematic
     */
    private int getIndex(BlockPos relativePos)
    {
        if (relativePos.getX() < 0 || relativePos.getX() >= size.getX() ||
                relativePos.getY() < 0 || relativePos.getY() >= size.getY() ||
                relativePos.getZ() < 0 || relativePos.getZ() >= size.getZ())
        {
            Keystone.LOGGER.error("Trying to get block outside of schematic bounds!");
            return -1;
        }
        return relativePos.getZ() + relativePos.getY() * size.getZ() + relativePos.getX() * size.getZ() * size.getY();
    }

    /**
     * @return The size of the schematic
     */
    public Vec3i getSize()
    {
        return size;
    }
    /**
     * @return A {@link PalettedArray} containing the block states of the schematic
     */
    public PalettedArray<BlockState> getBlocks() { return blocks; }
    /**
     * @return The number of {@link Entity Entities} in the schematic
     */
    public int getEntityCount() { return entities.length; }
    /**
     * @return A map of all tile entities in the schematic
     */
    public Map<BlockPos, NbtCompound> getTileEntities() { return tileEntities; }
    /**
     * @return A Set containing the Resource Locations of all extensions that are added to this schematic
     */
    public Set<Identifier> getExtensionIDs() { return extensions.keySet(); }
    /**
     * Get the block state at a relative block position in the schematic
     * @param relativePos The relative block position
     * @return The block state at the position, or air if it is outside the schematic. Can return
     * null if there is no block at that location
     */
    public BlockState getBlockState(BlockPos relativePos)
    {
        int index = getIndex(relativePos);
        if (index < 0) return Blocks.VOID_AIR.getDefaultState();
        else return blocks.get(getIndex(relativePos));
    }
    public NbtCompound getTileEntity(BlockPos relativePos)
    {
        return tileEntities.getOrDefault(relativePos, null);
    }
    /**
     * @param id The Resource Location of the extension
     * @return The {@link ISchematicExtension Extension} to this schematic with a given ID
     */
    public ISchematicExtension getExtension(Identifier id)
    {
        return extensions.get(id);
    }
    /**
     * Run a function for every block position and state in the schematic
     * @param consumer The function to run
     */
    public void forEachBlock(TriConsumer<BlockPos, BlockState, NbtCompound> consumer)
    {
        int i = 0;
        for (int x = 0; x < size.getX(); x++)
        {
            for (int y = 0; y < size.getY(); y++)
            {
                for (int z = 0; z < size.getZ(); z++)
                {
                    BlockState blockState = blocks.get(i++);
                    if (blockState == null) continue;
                    BlockPos pos = new BlockPos(x, y, z);
                    consumer.accept(pos, blockState, tileEntities.getOrDefault(pos, null));
                }
            }
        }
    }
    /**
     * Run a function for every entity in the schematic
     * @param consumer The function to run
     */
    public void forEachEntity(Consumer<Entity> consumer)
    {
        for (Entity entity : entities) consumer.accept(entity);
    }
    public void forEachExtension(Consumer<ISchematicExtension> consumer) { extensions.values().forEach(consumer); }

    /**
     * Place the schematic at a given {@link BlockPos} in a given {@link GhostBlocksWorld}
     * @param ghostWorld The {@link GhostBlocksWorld} to place the schematic in
     * @param scale The scale to place the schematic with
     */
    public void place(GhostBlocksWorld ghostWorld, int scale)
    {
        scale = Math.max(1, scale);

        for (Entity entityTemplate : entities)
        {
            Entity scaled = entityTemplate.clone();
            scaled.position(scaled.x() * scale, scaled.y() * scale, scaled.z() * scale);
            scaled.spawn(ghostWorld);
        }

        int i = 0;
        for (int x = 0; x < size.getX(); x++)
        {
            for (int y = 0; y < size.getY(); y++)
            {
                for (int z = 0; z < size.getZ(); z++)
                {
                    BlockState blockState = blocks.get(i++);
                    if (blockState == null || blockState.getBlock().equals(Blocks.STRUCTURE_VOID)) continue;
                    NbtCompound tileEntity = tileEntities.getOrDefault(new BlockPos(x, y, z), null);

                    for (int sx = 0; sx < scale; sx++)
                    {
                        for (int sy = 0; sy < scale; sy++)
                        {
                            for (int sz = 0; sz < scale; sz++)
                            {
                                BlockPos localPos = new BlockPos(x * scale + sx, y * scale + sy, z * scale + sz);
                                ghostWorld.setBlockState(localPos, blockState);
                                if (tileEntity != null)
                                {
                                    NbtCompound tileEntityData = tileEntity.copy();
                                    tileEntityData.putInt("x", x);
                                    tileEntityData.putInt("y", y);
                                    tileEntityData.putInt("z", z);

                                    BlockEntity instance = ghostWorld.getBlockEntity(localPos);
                                    if (instance != null) instance.read(tileEntityData, RegistryLookups.registryLookup());
                                }
                            }
                        }
                    }
                }
            }
        }

        extensions.values().forEach(extension ->
        {
            if (extension.placeByDefault()) extension.place(this, ghostWorld);
        });
    }

    /**
     * Place the schematic at a given {@link BlockPos}
     * @param worldModifiers The {@link WorldModifierModules} to place the schematic with
     * @param anchor The minimum {@link BlockPos} to place the schematic at
     */
    public void place(WorldModifierModules worldModifiers, BlockPos anchor)
    {
        place(worldModifiers, anchor, BlockRotation.NONE, BlockMirror.NONE, 1, new HashMap<>(), true);
    }
    /**
     * Place the schematic at a given {@link BlockPos}
     * @param worldModifiers The {@link WorldModifierModules} to place the schematic with
     * @param anchor The minimum {@link BlockPos} to place the schematic at
     * @param rotation The {@link BlockRotation Rotation} of the schematic
     * @param mirror The {@link BlockMirror Mirror} of the schematic
     * @param scale The scale of the schematic
     * @param extensionsToPlace A Map containing which extensions should be placed in the world and which should be ignored
     * @param placeAir If false, air will not be placed
     */
    public void place(WorldModifierModules worldModifiers, BlockPos anchor, BlockRotation rotation, BlockMirror mirror, int scale, Map<Identifier, Boolean> extensionsToPlace, boolean placeAir)
    {
        int clampedScale = Math.max(scale, 1);

        for (Entity entityTemplate : entities)
        {
            Entity oriented = entityTemplate.getOrientedEntity(Vec3d.of(anchor), rotation, mirror, size, clampedScale);
            worldModifiers.entities.commitEntityChanges(oriented);
        }

        int i = 0;
        for (int x = 0; x < size.getX(); x++)
        {
            for (int y = 0; y < size.getY(); y++)
            {
                for (int z = 0; z < size.getZ(); z++)
                {
                    BlockState blockState = blocks.get(i++);
                    if (blockState == null || blockState.getBlock().equals(Blocks.STRUCTURE_VOID)) continue;
                    if (blockState.isAir() && !placeAir) continue;
                    
                    blockState = blockState.mirror(mirror).rotate(rotation);
                    NbtCompound tileEntity = tileEntities.getOrDefault(new BlockPos(x, y, z), null);

                    for (int sx = 0; sx < clampedScale; sx++)
                    {
                        for (int sy = 0; sy < clampedScale; sy++)
                        {
                            for (int sz = 0; sz < clampedScale; sz++)
                            {
                                BlockPos localPos = new BlockPos(x * clampedScale + sx, y * clampedScale + sy, z * clampedScale + sz);
                                BlockPos worldPos = BlockPosMath.getOrientedBlockPos(localPos, size, rotation, mirror, clampedScale).add(anchor);
                                worldModifiers.blocks.setBlockType(worldPos.getX(), worldPos.getY(), worldPos.getZ(), blockState);
                                if (tileEntity != null) worldModifiers.blocks.setBlockData(worldPos.getX(), worldPos.getY(), worldPos.getZ(), tileEntity);
                            }
                        }
                    }
                }
            }
        }

        extensions.values().forEach(extension ->
        {
            Boolean place = extensionsToPlace.get(extension.id());
            if (place == null) place = extension.placeByDefault();
            if (place) extension.place(this, worldModifiers, anchor, rotation, mirror, clampedScale);
        });
    }
}
