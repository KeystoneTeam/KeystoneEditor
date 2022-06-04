package keystone.core.schematic;

import keystone.api.Keystone;
import keystone.api.enums.RetrievalMode;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.blocks.BlockType;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.entities.Entity;
import keystone.core.client.Player;
import keystone.core.math.BlockPosMath;
import keystone.core.modules.selection.SelectionBoundingBox;
import keystone.core.modules.world.WorldModifierModules;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.registries.BlockTypeRegistry;
import keystone.core.renderer.blocks.world.GhostBlocksWorld;
import keystone.core.schematic.extensions.ISchematicExtension;
import keystone.core.schematic.formats.KeystoneSchematicFormat;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A schematic containing block and entity data. Used for cloning and import/export operations
 */
public class KeystoneSchematic
{
    private Vec3i size;
    private Block[] blocks;
    private Entity[] entities;
    private Map<Identifier, ISchematicExtension> extensions;

    /**
     * @param size The size of the schematic
     * @param blocks The {@link Block} contents of the schematic
     * @param entities The {@link Entity} contents of the schematic
     * @param extensions The {@link ISchematicExtension Extensions} stored in this schematic
     */
    public KeystoneSchematic(Vec3i size, Block[] blocks, Entity[] entities, Map<Identifier, ISchematicExtension> extensions)
    {
        this.size = size;
        this.blocks = blocks;
        this.entities = entities == null ? new Entity[0] : entities;
        for (Entity entity : this.entities) entity.breakMinecraftEntityConnection();
        this.extensions = extensions;
    }
    /**
     * Create a schematic from a selection box
     * @param box The {@link SelectionBoundingBox} to create the schematic from
     * @param worldModifiers The {@link WorldModifierModules} that the schematic contents is read from
     * @param retrievalMode The {@link RetrievalMode} used in reading the schematic contents
     * @param structureVoid The {@link BlockState} that represents structure voids
     * @return The generated {@link KeystoneSchematic}
     */
    public static KeystoneSchematic createFromSelection(SelectionBoundingBox box, WorldModifierModules worldModifiers, RetrievalMode retrievalMode, BlockState structureVoid)
    {
        return createFromCorners(box.getCorner1(), box.getCorner2(), worldModifiers, retrievalMode, structureVoid);
    }
    /**
     * Create a schematic from two corners
     * @param corner1 The first corner
     * @param corner2 The second corner
     * @param worldModifiers The {@link WorldModifierModules} that the schematic contents is read from
     * @param retrievalMode The {@link RetrievalMode} used in reading the schematic contents
     * @param structureVoid The {@link BlockState} that represents structure voids
     * @return The generated {@link KeystoneSchematic}
     */
    public static KeystoneSchematic createFromCorners(Vec3i corner1, Vec3i corner2, WorldModifierModules worldModifiers, RetrievalMode retrievalMode, BlockState structureVoid)
    {
        BlockPos min = new BlockPos(Math.min(corner1.getX(), corner2.getX()), Math.min(corner1.getY(), corner2.getY()), Math.min(corner1.getZ(), corner2.getZ()));
        BlockPos max = new BlockPos(Math.max(corner1.getX(), corner2.getX()), Math.max(corner1.getY(), corner2.getY()), Math.max(corner1.getZ(), corner2.getZ()));

        // Get size
        Vec3i size = new Vec3i(max.getX() - min.getX() + 1, max.getY() - min.getY() + 1, max.getZ() - min.getZ() + 1);

        // Get blocks
        Block[] blocks = new Block[size.getX() * size.getY() * size.getZ()];
        int i = 0;
        for (int x = 0; x < size.getX(); x++)
        {
            for (int y = 0; y < size.getY(); y++)
            {
                for (int z = 0; z < size.getZ(); z++)
                {
                    blocks[i] = worldModifiers.blocks.getBlock(x + min.getX(), y + min.getY(), z + min.getZ(), retrievalMode);
                    if (blocks[i].blockType().getMinecraftBlock() == structureVoid) blocks[i] = null;
                    i++;
                }
            }
        }

        // Get entities
        BoundingBox box = new BoundingBox(min, max);
        List<Entity> entityList = worldModifiers.entities.getEntities(box, RetrievalMode.ORIGINAL);
        Entity[] entities = new Entity[entityList.size()];
        entities = entityList.toArray(entities);
        for (Entity entity : entities)
        {
            entity.move(-min.getX(), -min.getY(), -min.getZ());
            entity.breakMinecraftEntityConnection();
        }

        // Create schematic from data
        World world = Keystone.getModule(WorldCacheModule.class).getDimensionWorld(Player.getDimension());
        return new KeystoneSchematic(size, blocks, entities, KeystoneSchematicFormat.createExtensions(world, box));
    }

    /**
     * Create a new {@link keystone.core.schematic.KeystoneSchematic} with the same size and contents as this one
     * @return The cloned {@link keystone.core.schematic.KeystoneSchematic}
     */
    public KeystoneSchematic clone()
    {
        return new KeystoneSchematic(new Vec3i(size.getX(), size.getY(), size.getZ()), Arrays.copyOf(blocks, blocks.length), Arrays.copyOf(entities, entities.length), Collections.unmodifiableMap(new HashMap<>(extensions)));
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
     * @return The number of {@link Entity Entities} in the schematic
     */
    public int getEntityCount() { return entities.length; }
    /**
     * @return A Set containing the Resource Locations of all extensions that are added to this schematic
     */
    public Set<Identifier> getExtensionIDs() { return extensions.keySet(); }
    /**
     * Get the {@link Block} at a relative block position in the schematic
     * @param relativePos The relative block position
     * @return The {@link Block} at the position, or air if it is outside the schematic. Can return
     * null if there is no block at that location
     */
    public Block getBlock(BlockPos relativePos)
    {
        int index = getIndex(relativePos);
        if (index < 0) return new Block(Blocks.AIR.getDefaultState());
        else return blocks[getIndex(relativePos)];
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
    public void forEachBlock(BiConsumer<BlockPos, Block> consumer)
    {
        int i = 0;
        for (int x = 0; x < size.getX(); x++)
        {
            for (int y = 0; y < size.getY(); y++)
            {
                for (int z = 0; z < size.getZ(); z++)
                {
                    if (blocks[i] == null)
                    {
                        i++;
                        continue;
                    }
                    consumer.accept(new BlockPos(x, y, z), blocks[i]);
                    i++;
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
                    if (blocks[i] == null)
                    {
                        i++;
                        continue;
                    }
                    Block block = blocks[i];

                    for (int sx = 0; sx < scale; sx++)
                    {
                        for (int sy = 0; sy < scale; sy++)
                        {
                            for (int sz = 0; sz < scale; sz++)
                            {
                                BlockPos localPos = new BlockPos(x * scale + sx, y * scale + sy, z * scale + sz);
                                BlockType blockType = block.blockType();

                                BlockState state = blockType.getMinecraftBlock();
                                ghostWorld.setBlockState(localPos, state);
                                if (block.tileEntity() != null)
                                {
                                    NbtCompound tileEntityData = block.tileEntity().getMinecraftNBT().copy();
                                    tileEntityData.putInt("x", x);
                                    tileEntityData.putInt("y", y);
                                    tileEntityData.putInt("z", z);

                                    BlockEntity tileEntity = ghostWorld.getBlockEntity(localPos);
                                    if (tileEntity != null) tileEntity.readNbt(tileEntityData);
                                }
                            }
                        }
                    }
                    i++;
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
                    Block block = blocks[i];
                    if (block == null || (block.blockType().getMinecraftBlock().isAir() && !placeAir))
                    {
                        i++;
                        continue;
                    }

                    for (int sx = 0; sx < clampedScale; sx++)
                    {
                        for (int sy = 0; sy < clampedScale; sy++)
                        {
                            for (int sz = 0; sz < clampedScale; sz++)
                            {
                                BlockPos localPos = new BlockPos(x * clampedScale + sx, y * clampedScale + sy, z * clampedScale + sz);
                                BlockPos worldPos = BlockPosMath.getOrientedBlockPos(localPos, size, rotation, mirror, clampedScale).add(anchor);
                                BlockType blockType = block.blockType();
                                blockType = BlockTypeRegistry.fromMinecraftBlock(blockType.getMinecraftBlock().rotate(rotation).mirror(mirror));
                                Block oriented = new Block(blockType, block.tileEntity());
                                worldModifiers.blocks.setBlock(worldPos.getX(), worldPos.getY(), worldPos.getZ(), oriented);
                            }
                        }
                    }
                    i++;
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
