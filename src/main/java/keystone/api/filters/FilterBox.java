package keystone.api.filters;

import keystone.api.Keystone;
import keystone.api.SelectionBox;
import keystone.api.wrappers.Block;
import keystone.api.wrappers.BlockPos;
import keystone.api.wrappers.Vector3i;
import net.minecraft.world.World;

public class FilterBox
{
    //region Function Types
    public interface BlockConsumer
    {
        void accept(int x, int y, int z);
    }
    //endregion

    private final KeystoneFilter filter;
    private final World world;
    private final BlockPos min;
    private final BlockPos max;
    private final Vector3i size;

    private Block[] oldBlocks;
    private Block[] newBlocks;

    public FilterBox(World world, SelectionBox box, KeystoneFilter filter)
    {
        this.filter = filter;
        this.world = world;
        this.min = new BlockPos(box.getMin());
        this.max = new BlockPos(box.getMax());
        this.size = new Vector3i(box.getSize());

        this.oldBlocks = new Block[this.size.getX() * this.size.getY() * this.size.getZ()];
        this.newBlocks = new Block[this.size.getX() * this.size.getY() * this.size.getZ()];

        int i = 0;
        for (int x = min.getX(); x <= max.getX(); x++)
        {
            for (int y = min.getY(); y <= max.getY(); y++)
            {
                for (int z = min.getZ(); z <= max.getZ(); z++)
                {
                    BlockPos pos = new BlockPos(x, y, z);
                    Block block = new Block(world.getBlockState(pos.getMinecraftBlockPos()), world.getTileEntity(pos.getMinecraftBlockPos()));

                    oldBlocks[i] = block;
                    newBlocks[i] = block;
                    i++;
                }
            }
        }
    }

    private int getBlockIndex(int x, int y, int z)
    {
        int normalizedX = x - min.getX();
        int normalizedY = y - min.getY();
        int normalizedZ = z - min.getZ();

        if (normalizedX < 0 || normalizedX >= size.getX() ||
                normalizedY < 0 || normalizedY >= size.getY() ||
                normalizedZ < 0 || normalizedZ >= size.getZ())
        {
            Keystone.LOGGER.error("Trying to get block outside of selection bounds!");
            return -1;
        }

        return normalizedZ + normalizedY * size.getZ() + normalizedX * size.getZ() * size.getY();
    }

    public BlockPos getMin() { return this.min; }
    public BlockPos getMax() { return this.max; }
    public Vector3i getSize() { return this.size; }

    public Block getBlock(int x, int y, int z) { return getBlock(x, y, z, true); }
    public Block getBlock(int x, int y, int z, boolean getOriginalState)
    {
        if (x < min.getX() || x > max.getX() ||
                y < min.getY() || y > max.getY() ||
                z < min.getZ() || z > max.getZ())
        {
            net.minecraft.util.math.BlockPos pos = new net.minecraft.util.math.BlockPos(x, y, z);
            return new Block(world.getBlockState(pos), world.getTileEntity(pos));
        }

        int index = getBlockIndex(x, y, z);
        if (index < 0) return filter.air();
        else return getOriginalState ? oldBlocks[index] : newBlocks[index];
    }

    public boolean setBlock(int x, int y, int z, String block)
    {
        return setBlock(x, y, z, filter.block(block));
    }
    public boolean setBlock(int x, int y, int z, Block block)
    {
        int index = getBlockIndex(x, y, z);
        if (index < 0) return false;

        newBlocks[index] = block;
        return true;
    }

    public void forEachBlock(BlockConsumer consumer)
    {
        for (int x = min.getX(); x <= max.getX(); x++)
        {
            for (int y = min.getY(); y <= max.getY(); y++)
            {
                for (int z = min.getZ(); z <= max.getZ(); z++)
                {
                    consumer.accept(x, y, z);
                }
            }
        }
    }
}
