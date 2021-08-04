package keystone.core.schematic;

import keystone.api.Keystone;
import keystone.api.enums.RetrievalMode;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.entities.Entity;
import keystone.core.events.SchematicEvent;
import keystone.core.math.BlockPosMath;
import keystone.core.modules.selection.boxes.SelectionBoundingBox;
import keystone.core.modules.world.WorldModifierModules;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.renderer.blocks.world.GhostBlocksWorld;
import keystone.core.renderer.client.Player;
import keystone.core.schematic.extensions.ISchematicExtension;
import keystone.core.schematic.formats.KeystoneSchematicFormat;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A schematic containing block and entity data. Used for cloning and import/export operations
 */
public class KeystoneSchematic
{
    private Vector3i size;
    private Block[] blocks;
    private Entity[] entities;
    private Map<ResourceLocation, ISchematicExtension> extensions;

    /**
     * @param size The size of the schematic
     * @param blocks The {@link Block} contents of the schematic
     * @param entities The {@link Entity} contents of the schematic
     * @param extensions The {@link ISchematicExtension Extensions} stored in this schematic
     */
    public KeystoneSchematic(Vector3i size, Block[] blocks, Entity[] entities, Map<ResourceLocation, ISchematicExtension> extensions)
    {
        this.size = size;
        this.blocks = blocks;
        this.entities = entities == null ? new Entity[0] : entities;
        for (Entity entity : this.entities) entity.breakMinecraftEntityConnection();
        this.extensions = extensions;
    }

    /**
     * Create a schematic from a selection box
     * @param box The {@link keystone.core.modules.selection.boxes.SelectionBoundingBox} to create the schematic from
     * @param worldModifiers The {@link WorldModifierModules} that the schematic contents is read from
     * @return The generated {@link keystone.core.schematic.KeystoneSchematic}
     */
    public static KeystoneSchematic createFromSelection(SelectionBoundingBox box, WorldModifierModules worldModifiers)
    {
        // Get size
        Vector3i size = new Vector3i(box.getMaxCoords().getX() - box.getMinCoords().getX() + 1,
                box.getMaxCoords().getY() - box.getMinCoords().getY() + 1,
                box.getMaxCoords().getZ() - box.getMinCoords().getZ() + 1);

        // Get blocks
        Block[] blocks = new Block[size.getX() * size.getY() * size.getZ()];
        int i = 0;
        for (int x = 0; x < size.getX(); x++)
        {
            for (int y = 0; y < size.getY(); y++)
            {
                for (int z = 0; z < size.getZ(); z++)
                {
                    blocks[i] = worldModifiers.blocks.getBlock(x + box.getMinCoords().getX(), y + box.getMinCoords().getY(), z + box.getMinCoords().getZ(), RetrievalMode.ORIGINAL);
                    if (blocks[i].getMinecraftBlock().is(Blocks.STRUCTURE_VOID)) blocks[i] = null;
                    i++;
                }
            }
        }

        // Get entities
        List<Entity> entityList = worldModifiers.entities.getEntities(box.getBoundingBox(), RetrievalMode.ORIGINAL);
        Entity[] entities = new Entity[entityList.size()];
        entities = entityList.toArray(entities);
        for (Entity entity : entities)
        {
            entity.move(-box.getMinCoords().getX(), -box.getMinCoords().getY(), -box.getMinCoords().getZ());
            entity.breakMinecraftEntityConnection();
        }

        // Create schematic from data
        World world = Keystone.getModule(WorldCacheModule.class).getDimensionWorld(Player.getDimensionId());
        return new KeystoneSchematic(size, blocks, entities, KeystoneSchematicFormat.createExtensions(world, box.getBoundingBox()));
    }

    /**
     * Create a new {@link keystone.core.schematic.KeystoneSchematic} with the same size and contents as this one
     * @return The cloned {@link keystone.core.schematic.KeystoneSchematic}
     */
    public KeystoneSchematic clone()
    {
        return new KeystoneSchematic(new Vector3i(size.getX(), size.getY(), size.getZ()), Arrays.copyOf(blocks, blocks.length), Arrays.copyOf(entities, entities.length), Collections.unmodifiableMap(new HashMap<>(extensions)));
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
    public Vector3i getSize()
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
    public Set<ResourceLocation> getExtensionIDs() { return extensions.keySet(); }
    /**
     * Get the {@link Block} at a relative block position in the schematic
     * @param relativePos The relative block position
     * @return The {@link Block} at the position, or air if it is outside the schematic. Can return
     * null if there is no block at that location
     */
    public Block getBlock(BlockPos relativePos)
    {
        int index = getIndex(relativePos);
        if (index < 0) return new Block(Blocks.AIR.defaultBlockState());
        else return blocks[getIndex(relativePos)];
    }
    /**
     * @param id The Resource Location of the extension
     * @return The {@link ISchematicExtension Extension} to this schematic with a given ID
     */
    public ISchematicExtension getExtension(ResourceLocation id)
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
            scaled.spawnInWorld(ghostWorld);
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
                    SchematicEvent.ScaleBlock scaleEvent = new SchematicEvent.ScaleBlock(blocks[i], scale);
                    MinecraftForge.EVENT_BUS.post(scaleEvent);

                    for (int sx = 0; sx < scale; sx++)
                    {
                        for (int sy = 0; sy < scale; sy++)
                        {
                            for (int sz = 0; sz < scale; sz++)
                            {
                                BlockPos localPos = new BlockPos(x * scale + sx, y * scale + sy, z * scale + sz);
                                Block block = scaleEvent.getBlock(sx, sy, sz);

                                BlockState state = block.getMinecraftBlock();
                                ghostWorld.setBlockAndUpdate(localPos, state);
                                if (block.getTileEntityData() != null)
                                {
                                    CompoundNBT tileEntityData = block.getTileEntityData().copy();
                                    tileEntityData.putInt("x", x);
                                    tileEntityData.putInt("y", y);
                                    tileEntityData.putInt("z", z);

                                    TileEntity tileEntity = ghostWorld.getBlockEntity(localPos);
                                    if (tileEntity != null)
                                        tileEntity.deserializeNBT(block.getMinecraftBlock(), tileEntityData);
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
        place(worldModifiers, anchor, Rotation.NONE, Mirror.NONE, 1, new HashMap<>());
    }
    /**
     * Place the schematic at a given {@link BlockPos}
     * @param worldModifiers The {@link WorldModifierModules} to place the schematic with
     * @param anchor The minimum {@link BlockPos} to place the schematic at
     * @param rotation The {@link Rotation} of the schematic
     * @param mirror The {@link Mirror} of the schematic
     * @param scale The scale of the schematic
     * @param extensionsToPlace A Map containing which extensions should be placed in the world and which should be ignored
     */
    public void place(WorldModifierModules worldModifiers, BlockPos anchor, Rotation rotation, Mirror mirror, int scale, Map<ResourceLocation, Boolean> extensionsToPlace)
    {
        int clampedScale = Math.max(scale, 1);

        for (Entity entityTemplate : entities)
        {
            Entity oriented = entityTemplate.getOrientedEntity(Vector3d.atLowerCornerOf(anchor), rotation, mirror, size, clampedScale);
            worldModifiers.entities.setEntity(oriented);
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

                    SchematicEvent.ScaleBlock scaleEvent = new SchematicEvent.ScaleBlock(blocks[i], clampedScale);
                    MinecraftForge.EVENT_BUS.post(scaleEvent);

                    for (int sx = 0; sx < clampedScale; sx++)
                    {
                        for (int sy = 0; sy < clampedScale; sy++)
                        {
                            for (int sz = 0; sz < clampedScale; sz++)
                            {
                                BlockPos localPos = new BlockPos(x * clampedScale + sx, y * clampedScale + sy, z * clampedScale + sz);
                                BlockPos worldPos = BlockPosMath.getOrientedBlockPos(localPos, size, rotation, mirror, clampedScale).offset(anchor);
                                Block block = scaleEvent.getBlock(sx, sy, sz).clone();

                                block.setMinecraftBlock(block.getMinecraftBlock().rotate(worldModifiers.blocks.getWorld(), worldPos, rotation).mirror(mirror));
                                worldModifiers.blocks.setBlock(worldPos.getX(), worldPos.getY(), worldPos.getZ(), block);
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
