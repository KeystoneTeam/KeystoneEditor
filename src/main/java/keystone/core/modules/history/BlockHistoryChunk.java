package keystone.core.modules.history;

import keystone.api.enums.BlockRetrievalMode;
import keystone.api.wrappers.Block;
import net.minecraft.block.DispenserBlock;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.DispenserTileEntity;
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
    private Block[] buffer1;
    private Block[] buffer2;
    private boolean swapped;

    public BlockHistoryChunk(Vector3i chunkPosition, World world)
    {
        this.chunkX = chunkPosition.getX();
        this.chunkY = chunkPosition.getY();
        this.chunkZ = chunkPosition.getZ();
        this.world = world;

        oldBlocks = new Block[4096];
        buffer1 = new Block[4096];
        buffer2 = new Block[4096];

        int i = 0;
        for (int x = chunkX * 16; x < (chunkX + 1) * 16; x++)
        {
            for (int y = chunkY * 16; y < (chunkY + 1) * 16; y++)
            {
                for (int z = chunkZ * 16; z < (chunkZ + 1) * 16; z++)
                {
                    BlockPos pos = new BlockPos(x, y, z);
                    oldBlocks[i] = new Block(world.getBlockState(pos), world.getBlockEntity(pos));
                    i++;
                }
            }
        }
    }

    public Block getBlock(int x, int y, int z, BlockRetrievalMode retrievalMode)
    {
        int index = getIndex(x, y, z);
        Block block = null;

        switch (retrievalMode)
        {
            case ORIGINAL: return oldBlocks[index];
            case LAST_SWAPPED: block = swapped ? buffer1[index] : buffer2[index]; break;
            case CURRENT: block = swapped ? buffer2[index] : buffer1[index]; break;
        }

        if (block == null) return oldBlocks[index];
        else return block;
    }
    public void setBlock(int x, int y, int z, Block block)
    {
        if (swapped) buffer2[getIndex(x, y, z)] = block;
        else buffer1[getIndex(x, y, z)] = block;
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

                    world.setBlockAndUpdate(pos, block.getMinecraftBlock());
                    if (block.getTileEntityData() != null)
                    {
                        CompoundNBT tileEntityData = block.getTileEntityData().copy();
                        tileEntityData.putInt("x", x);
                        tileEntityData.putInt("y", y);
                        tileEntityData.putInt("z", z);

                        TileEntity tileEntity = world.getBlockEntity(pos);
                        if (tileEntity != null) tileEntity.deserializeNBT(block.getMinecraftBlock(), tileEntityData);
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
                    Block block = swapped ? buffer2[i] : buffer1[i];
                    if (block != null && block.getMinecraftBlock() != null)
                    {
                        BlockPos pos = new BlockPos(x, y, z);

                        world.setBlockAndUpdate(pos, block.getMinecraftBlock());
                        if (block.getTileEntityData() != null)
                        {
                            CompoundNBT tileEntityData = block.getTileEntityData().copy();
                            tileEntityData.putInt("x", x);
                            tileEntityData.putInt("y", y);
                            tileEntityData.putInt("z", z);

                            TileEntity tileEntity = world.getBlockEntity(pos);
                            if (tileEntity != null) tileEntity.deserializeNBT(block.getMinecraftBlock(), tileEntityData);
                        }
                    }
                    i++;
                }
            }
        }
    }
    public void swapBuffers(boolean copy)
    {
        swapped = !swapped;
        System.arraycopy(swapped ? buffer1 : buffer2, 0, swapped ? buffer2 : buffer1, 0, buffer1.length);
    }

    private int getIndex(int x, int y, int z)
    {
        x -= chunkX * 16;
        y -= chunkY * 16;
        z -= chunkZ * 16;

        return z + y * 16 + x * 256;
    }
}
