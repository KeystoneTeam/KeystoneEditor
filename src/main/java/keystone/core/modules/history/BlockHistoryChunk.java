package keystone.core.modules.history;

import keystone.api.wrappers.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;

public class BlockHistoryChunk
{
    private int chunkX;
    private int chunkY;
    private int chunkZ;

    private World world;
    private Block[] oldBlocks;
    private Block[] newBlocks;

    public BlockHistoryChunk(Vector3i chunkPosition, World world)
    {
        this.chunkX = chunkPosition.getX();
        this.chunkY = chunkPosition.getY();
        this.chunkZ = chunkPosition.getZ();
        this.world = world;

        oldBlocks = new Block[4096];
        newBlocks = new Block[4096];

        int i = 0;
        for (int x = chunkX * 16; x < (chunkX + 1) * 16; x++)
        {
            for (int y = chunkY * 16; y < (chunkY + 1) * 16; y++)
            {
                for (int z = chunkZ * 16; z < (chunkZ + 1) * 16; z++)
                {
                    BlockPos pos = new BlockPos(x, y, z);
                    oldBlocks[i] = new Block(world.getBlockState(pos), world.getTileEntity(pos));
                    i++;
                }
            }
        }
    }

    public void setBlock(int x, int y, int z, Block block)
    {
        newBlocks[getIndex(x, y, z)] = block;
    }
    public void undo()
    {
        int i = 0;
        for (int x = chunkX * 16; x < (chunkX + 1) * 16; x++)
        {
            for (int y = chunkY * 16; y < (chunkY + 1) * 16; y++)
            {
                for (int z = chunkZ * 16; z < (chunkZ + 1) * 16; z++)
                {
                    BlockPos pos = new BlockPos(x, y, z);
                    Block block = oldBlocks[i];

                    world.setBlockState(pos, block.getMinecraftBlock());
                    if (block.getTileEntityData() != null)
                    {
                        CompoundNBT tileEntityData = block.getTileEntityData().copy();
                        tileEntityData.putInt("x", x);
                        tileEntityData.putInt("y", y);
                        tileEntityData.putInt("z", z);

                        TileEntity tileEntity = world.getTileEntity(pos);
                        if (tileEntity != null) tileEntity.read(block.getMinecraftBlock(), tileEntityData);
                    }
                    i++;
                }
            }
        }
    }
    public void redo()
    {
        int i = 0;
        for (int x = chunkX * 16; x < (chunkX + 1) * 16; x++)
        {
            for (int y = chunkY * 16; y < (chunkY + 1) * 16; y++)
            {
                for (int z = chunkZ * 16; z < (chunkZ + 1) * 16; z++)
                {
                    if (newBlocks[i] != null)
                    {
                        BlockPos pos = new BlockPos(x, y, z);
                        Block block = newBlocks[i];

                        world.setBlockState(pos, block.getMinecraftBlock());
                        if (block.getTileEntityData() != null)
                        {
                            CompoundNBT tileEntityData = block.getTileEntityData().copy();
                            tileEntityData.putInt("x", x);
                            tileEntityData.putInt("y", y);
                            tileEntityData.putInt("z", z);

                            TileEntity tileEntity = world.getTileEntity(pos);
                            if (tileEntity != null) tileEntity.read(block.getMinecraftBlock(), tileEntityData);
                        }
                    }
                    i++;
                }
            }
        }
    }

    private int getIndex(int x, int y, int z)
    {
        x -= chunkX * 16;
        y -= chunkY * 16;
        z -= chunkZ * 16;

        return z + y * 16 + x * 256;
    }
}
