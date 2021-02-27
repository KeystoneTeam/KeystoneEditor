package keystone.core.schematic;

import keystone.api.Keystone;
import keystone.api.wrappers.Block;
import keystone.core.modules.selection.boxes.SelectionBoundingBox;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
     * @param world The world that the schematic contents is read from
     * @return The generated {@link keystone.core.schematic.KeystoneSchematic}
     */
    public static KeystoneSchematic createFromSelection(SelectionBoundingBox box, World world)
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
                    BlockPos pos = new BlockPos(x + box.getMinCoords().getX(), y + box.getMinCoords().getY(), z + box.getMinCoords().getZ());
                    blocks[i] = new Block(world.getBlockState(pos), world.getTileEntity(pos));
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
}
