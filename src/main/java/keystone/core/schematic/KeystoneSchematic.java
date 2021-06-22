package keystone.core.schematic;

import keystone.api.Keystone;
import keystone.api.enums.BlockRetrievalMode;
import keystone.api.wrappers.Block;
import keystone.core.math.BlockPosMath;
import keystone.core.modules.blocks.BlocksModule;
import keystone.core.modules.selection.boxes.SelectionBoundingBox;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.function.BiConsumer;

/**
 * A schematic containing block state data. Used for cloning and import/export operations
 */
public class KeystoneSchematic
{
    private Vector3i size;
    private Block[] blocks;

    /**
     * @param size The size of the schematic
     * @param blocks The {@link keystone.api.wrappers.Block} contents of the schematic
     */
    private KeystoneSchematic(Vector3i size, Block[] blocks)
    {
        this.size = size;
        this.blocks = blocks;
    }

    /**
     * Create a schematic from a selection box
     * @param box The {@link keystone.core.modules.selection.boxes.SelectionBoundingBox} to create the schematic from
     * @param blocksModule The {@link keystone.core.modules.blocks.BlocksModule} that the schematic contents is read from
     * @return The generated {@link keystone.core.schematic.KeystoneSchematic}
     */
    public static KeystoneSchematic createFromSelection(SelectionBoundingBox box, BlocksModule blocksModule)
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
                    blocks[i] = blocksModule.getBlock(x + box.getMinCoords().getX(), y + box.getMinCoords().getY(), z + box.getMinCoords().getZ(), BlockRetrievalMode.ORIGINAL);
                    i++;
                }
            }
        }

        // Create schematic from data
        return new KeystoneSchematic(size, blocks);
    }

    /**
     * Create a new {@link keystone.core.schematic.KeystoneSchematic} with the same size and contents as this one
     * @return The cloned {@link keystone.core.schematic.KeystoneSchematic}
     */
    public KeystoneSchematic clone()
    {
        return new KeystoneSchematic(new Vector3i(size.getX(), size.getY(), size.getZ()), Arrays.copyOf(blocks, blocks.length));
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
     * Get the {@link keystone.api.wrappers.Block} at a relative block position in the schematic
     * @param relativePos The relative block position
     * @return The {@link keystone.api.wrappers.Block} at the position, or air if it is outside the schematic
     */
    public Block getBlock(BlockPos relativePos)
    {
        int index = getIndex(relativePos);
        if (index < 0) return new Block(Blocks.AIR.getDefaultState());
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
     * Place the schematic at a given {@link BlockPos} in a given {@link World}
     * @param anchor The minimum {@link BlockPos} to place the schematic at
     * @param world The {@link World} to place the schematic in
     */
    public void place(BlockPos anchor, World world)
    {
        place(anchor, world, Rotation.NONE, Mirror.NONE, 1);
    }
    /**
     * Place the schematic at a given {@link BlockPos} in a given {@link World}
     * @param anchor The minimum {@link BlockPos} to place the schematic at
     * @param world The {@link World} to place the schematic in
     * @param rotation The {@link Rotation} of the schematic
     * @param mirror The {@link Mirror} of the schematic
     * @param scale The scale of the schematic
     */
    public void place(BlockPos anchor, World world, Rotation rotation, Mirror mirror, int scale)
    {
        scale = scale < 1 ? 1 : scale;

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
                                BlockPos worldPos = BlockPosMath.getOrientedBlockPos(localPos, size, rotation, mirror, scale).add(anchor);

                                Block block = blocks[i];
                                BlockState state = block.getMinecraftBlock().rotate(world, worldPos, rotation).mirror(mirror);
                                world.setBlockState(worldPos, state);
                                if (block.getTileEntityData() != null)
                                {
                                    CompoundNBT tileEntityData = block.getTileEntityData().copy();
                                    tileEntityData.putInt("x", x);
                                    tileEntityData.putInt("y", y);
                                    tileEntityData.putInt("z", z);

                                    TileEntity tileEntity = world.getTileEntity(worldPos);
                                    if (tileEntity != null) tileEntity.read(block.getMinecraftBlock(), tileEntityData);
                                }
                            }
                        }
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
     */
    public void place(BlockPos anchor, BlocksModule blocksModule)
    {
        place(anchor, blocksModule, Rotation.NONE, Mirror.NONE, 1);
    }
    /**
     * Place the schematic at a given {@link BlockPos} in a given {@link World}
     * @param anchor The minimum {@link BlockPos} to place the schematic at
     * @param blocksModule The {@link BlocksModule} to place the schematic with
     * @param rotation The {@link Rotation} of the schematic
     * @param mirror The {@link Mirror} of the schematic
     * @param scale The scale of the schematic
     */
    public void place(BlockPos anchor, BlocksModule blocksModule, Rotation rotation, Mirror mirror, int scale)
    {
        scale = scale < 1 ? 1 : scale;

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
                                BlockPos worldPos = BlockPosMath.getOrientedBlockPos(localPos, size, rotation, mirror, scale).add(anchor);

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
