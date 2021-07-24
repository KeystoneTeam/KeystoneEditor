package keystone.core.schematic;

import keystone.api.Keystone;
import keystone.api.enums.RetrievalMode;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.entities.Entity;
import keystone.core.math.BlockPosMath;
import keystone.core.modules.blocks.BlocksModule;
import keystone.core.modules.entities.EntitiesModule;
import keystone.core.modules.selection.boxes.SelectionBoundingBox;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.renderer.blocks.world.GhostBlocksWorld;
import keystone.core.renderer.client.Player;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
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

    /**
     * @param size The size of the schematic
     * @param blocks The {@link Block} contents of the schematic
     */
    public KeystoneSchematic(Vector3i size, Block[] blocks, Entity[] entities)
    {
        this.size = size;
        this.blocks = blocks;
        this.entities = entities == null ? new Entity[0] : entities;
        for (Entity entity : this.entities) entity.breakMinecraftEntityConnection();
    }

    /**
     * Create a schematic from a selection box
     * @param box The {@link keystone.core.modules.selection.boxes.SelectionBoundingBox} to create the schematic from
     * @param blocksModule The {@link keystone.core.modules.blocks.BlocksModule} that the schematic contents is read from
     * @param entitiesModule The {@link EntitiesModule} that the schematic contents is read from
     * @return The generated {@link keystone.core.schematic.KeystoneSchematic}
     */
    public static KeystoneSchematic createFromSelection(SelectionBoundingBox box, BlocksModule blocksModule, EntitiesModule entitiesModule)
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
                    blocks[i] = blocksModule.getBlock(x + box.getMinCoords().getX(), y + box.getMinCoords().getY(), z + box.getMinCoords().getZ(), RetrievalMode.ORIGINAL);
                    i++;
                }
            }
        }

        // Get entities
        List<Entity> entityList = entitiesModule.getEntities(box.getBoundingBox(), RetrievalMode.ORIGINAL);
        Entity[] entities = new Entity[entityList.size()];
        entities = entityList.toArray(entities);
        for (Entity entity : entities)
        {
            entity.move(-box.getMinCoords().getX(), -box.getMinCoords().getY(), -box.getMinCoords().getZ());
            entity.breakMinecraftEntityConnection();
        }

        // Create schematic from data
        return new KeystoneSchematic(size, blocks, entities);
    }

    /**
     * Create a new {@link keystone.core.schematic.KeystoneSchematic} with the same size and contents as this one
     * @return The cloned {@link keystone.core.schematic.KeystoneSchematic}
     */
    public KeystoneSchematic clone()
    {
        return new KeystoneSchematic(new Vector3i(size.getX(), size.getY(), size.getZ()), Arrays.copyOf(blocks, blocks.length), Arrays.copyOf(entities, entities.length));
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
     * Get the {@link Block} at a relative block position in the schematic
     * @param relativePos The relative block position
     * @return The {@link Block} at the position, or air if it is outside the schematic
     */
    public Block getBlock(BlockPos relativePos)
    {
        int index = getIndex(relativePos);
        if (index < 0) return new Block(Blocks.AIR.defaultBlockState());
        else return blocks[getIndex(relativePos)];
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

    /**
     * Place the schematic at a given {@link BlockPos} in a given {@link GhostBlocksWorld}
     * @param ghostWorld The {@link GhostBlocksWorld} to place the schematic in
     */
    public void place(GhostBlocksWorld ghostWorld)
    {
        for (Entity entityTemplate : entities)
        {
            entityTemplate.spawnInWorld(ghostWorld);
        }

        int i = 0;
        for (int x = 0; x < size.getX(); x++)
        {
            for (int y = 0; y < size.getY(); y++)
            {
                for (int z = 0; z < size.getZ(); z++)
                {
                    BlockPos localPos = new BlockPos(x, y, z);

                    Block block = blocks[i];
                    BlockState state = block.getMinecraftBlock();
                    ghostWorld.setBlockAndUpdate(localPos, state);
                    if (block.getTileEntityData() != null)
                    {
                        CompoundNBT tileEntityData = block.getTileEntityData().copy();
                        tileEntityData.putInt("x", x);
                        tileEntityData.putInt("y", y);
                        tileEntityData.putInt("z", z);

                        TileEntity tileEntity = ghostWorld.getBlockEntity(localPos);
                        if (tileEntity != null) tileEntity.deserializeNBT(block.getMinecraftBlock(), tileEntityData);
                    }
                    i++;
                }
            }
        }
    }

    /**
     * Place the schematic at a given {@link BlockPos} in a given {@link World}
     * @param anchor The minimum {@link BlockPos} to place the schematic at
     * @param blocksModule The {@link BlocksModule} to place the schematic with
     * @param entitiesModule The {@link EntitiesModule} to place the schematic with
     */
    public void place(BlockPos anchor, BlocksModule blocksModule, EntitiesModule entitiesModule)
    {
        place(anchor, blocksModule, entitiesModule, Rotation.NONE, Mirror.NONE, 1);
    }
    /**
     * Place the schematic at a given {@link BlockPos} in a given {@link World}
     * @param anchor The minimum {@link BlockPos} to place the schematic at
     * @param blocksModule The {@link BlocksModule} to place the schematic with
     * @param entitiesModule The {@link EntitiesModule} to place the schematic with
     * @param rotation The {@link Rotation} of the schematic
     * @param mirror The {@link Mirror} of the schematic
     * @param scale The scale of the schematic
     */
    public void place(BlockPos anchor, BlocksModule blocksModule, EntitiesModule entitiesModule, Rotation rotation, Mirror mirror, int scale)
    {
        scale = scale < 1 ? 1 : scale;

        IServerWorld serverWorld = Keystone.getModule(WorldCacheModule.class).getDimensionServerWorld(Player.getDimensionId());
        for (Entity entityTemplate : entities)
        {
            Entity oriented = entityTemplate.getOrientedEntity(Vector3d.atLowerCornerOf(anchor), rotation, mirror, size, scale);
            entitiesModule.setEntity(oriented);
        }

        int i = 0;
        for (int x = 0; x < size.getX(); x++)
        {
            for (int y = 0; y < size.getY(); y++)
            {
                for (int z = 0; z < size.getZ(); z++)
                {
                    for (int sx = 0; sx < scale; sx++)
                    {
                        for (int sy = 0; sy < scale; sy++)
                        {
                            for (int sz = 0; sz < scale; sz++)
                            {
                                BlockPos localPos = new BlockPos(x * scale + sx, y * scale + sy, z * scale + sz);
                                BlockPos worldPos = BlockPosMath.getOrientedBlockPos(localPos, size, rotation, mirror, scale).offset(anchor);

                                Block block = blocks[i].clone();
                                block.setMinecraftBlock(block.getMinecraftBlock().rotate(blocksModule.getWorld(), worldPos, rotation).mirror(mirror));
                                blocksModule.setBlock(worldPos.getX(), worldPos.getY(), worldPos.getZ(), block);
                            }
                        }
                    }
                    i++;
                }
            }
        }
    }
}
