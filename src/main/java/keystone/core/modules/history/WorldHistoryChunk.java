package keystone.core.modules.history;

import keystone.api.enums.RetrievalMode;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.entities.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IServerWorld;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WorldHistoryChunk
{
    private int chunkX;
    private int chunkY;
    private int chunkZ;
    private IServerWorld world;

    private Block[] oldBlocks;
    private Block[] blockBuffer1;
    private Block[] blockBuffer2;

    private Map<UUID, Entity> oldEntities;
    private Map<UUID, Entity> entityBuffer1;
    private Map<UUID, Entity> entityBuffer2;
    private Map<UUID, Entity> allEntities;

    private boolean swappedBlocks;
    private boolean swappedEntities;

    public WorldHistoryChunk(Vector3i chunkPosition, IServerWorld world)
    {
        this.chunkX = chunkPosition.getX();
        this.chunkY = chunkPosition.getY();
        this.chunkZ = chunkPosition.getZ();
        this.world = world;

        oldBlocks = new Block[4096];
        blockBuffer1 = new Block[4096];
        blockBuffer2 = new Block[4096];

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

        oldEntities = new HashMap<>();
        entityBuffer1 = new HashMap<>();
        entityBuffer2 = new HashMap<>();
        allEntities = new HashMap<>();

        int startX = chunkX << 4;
        int startY = chunkY << 4;
        int startZ = chunkZ << 4;
        AxisAlignedBB bb = new AxisAlignedBB(startX, startY, startZ, startX + 16, startY + 16, startZ + 16);
        List<net.minecraft.entity.Entity> mcEntities = world.getEntitiesOfClass(net.minecraft.entity.Entity.class, bb);
        for (net.minecraft.entity.Entity mcEntity : mcEntities)
        {
            Entity entity = new Entity(mcEntity);
            oldEntities.put(entity.keystoneUUID(), entity);
            entityBuffer1.put(entity.keystoneUUID(), entity);
            entityBuffer2.put(entity.keystoneUUID(), entity);
            allEntities.put(entity.keystoneUUID(), entity);
        }
    }

    public Block getBlock(int x, int y, int z, RetrievalMode retrievalMode)
    {
        int index = getIndex(x, y, z);
        Block block = null;

        switch (retrievalMode)
        {
            case ORIGINAL: return oldBlocks[index];
            case LAST_SWAPPED: block = swappedBlocks ? blockBuffer1[index] : blockBuffer2[index]; break;
            case CURRENT: block = swappedBlocks ? blockBuffer2[index] : blockBuffer1[index]; break;
        }

        if (block == null) return oldBlocks[index];
        else return block;
    }
    public Entity getEntity(UUID keystoneUUID, RetrievalMode retrievalMode)
    {
        Entity entity = null;

        switch (retrievalMode)
        {
            case ORIGINAL: return oldEntities.get(keystoneUUID);
            case LAST_SWAPPED: entity = swappedEntities ? entityBuffer1.get(keystoneUUID) : entityBuffer2.get(keystoneUUID); break;
            case CURRENT: entity = swappedEntities ? entityBuffer2.get(keystoneUUID) : entityBuffer1.get(keystoneUUID); break;
        }

        if (entity == null) return oldEntities.get(keystoneUUID);
        else return entity;
    }
    public int getEntities(List<Entity> buffer, BoundingBox boundingBox, RetrievalMode retrievalMode)
    {
        Map<UUID, Entity> retrievalBuffer = null;
        switch (retrievalMode)
        {
            case ORIGINAL: retrievalBuffer = oldEntities; break;
            case LAST_SWAPPED: retrievalBuffer = swappedEntities ? entityBuffer1 : entityBuffer2; break;
            case CURRENT: retrievalBuffer = swappedEntities ? entityBuffer2 : entityBuffer1; break;
        }

        int entityCount = 0;
        for (Entity entity : retrievalBuffer.values())
        {
            if (boundingBox.contains(entity.x(), entity.y(), entity.z()))
            {
                buffer.add(entity);
                entityCount++;
            }
        }
        return entityCount;
    }

    public void setBlock(int x, int y, int z, Block block)
    {
        if (swappedBlocks) blockBuffer2[getIndex(x, y, z)] = block;
        else blockBuffer1[getIndex(x, y, z)] = block;
    }
    public void setEntity(Entity entity)
    {
        if (swappedEntities) entityBuffer2.put(entity.keystoneUUID(), entity);
        else entityBuffer1.put(entity.keystoneUUID(), entity);
        allEntities.put(entity.keystoneUUID(), entity);
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

                    world.getLevel().setBlockAndUpdate(pos, block.getMinecraftBlock());
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

        for (UUID entityID : allEntities.keySet())
        {
            if (!oldEntities.containsKey(entityID))
            {
                Entity entity = allEntities.get(entityID);
                net.minecraft.entity.Entity mcEntity = world.getLevel().getEntity(entity.minecraftUUID());
                if (mcEntity != null)
                {
                    mcEntity.remove();
                    entity.breakMinecraftEntityConnection();
                }
            }
        }
        for (Entity entity : oldEntities.values()) entity.updateMinecraftEntity(world);
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
                    Block block = swappedBlocks ? blockBuffer2[i] : blockBuffer1[i];
                    if (block != null && block.getMinecraftBlock() != null)
                    {
                        BlockPos pos = new BlockPos(x, y, z);

                        world.getLevel().setBlockAndUpdate(pos, block.getMinecraftBlock());
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

        Map<UUID, Entity> activeEntityBuffer = swappedEntities ? entityBuffer2 : entityBuffer1;
        for (UUID entityID : allEntities.keySet())
        {
            if (!activeEntityBuffer.containsKey(entityID))
            {
                Entity entity = allEntities.get(entityID);
                net.minecraft.entity.Entity mcEntity = world.getLevel().getEntity(entity.minecraftUUID());
                if (mcEntity != null)
                {
                    mcEntity.remove();
                    entity.breakMinecraftEntityConnection();
                }
            }
        }
        for (Entity entity : activeEntityBuffer.values()) entity.updateMinecraftEntity(world);
    }
    public void swapBlockBuffers(boolean copy)
    {
        swappedBlocks = !swappedBlocks;
        if (copy)
        {
            System.arraycopy(swappedBlocks ? blockBuffer1 : blockBuffer2, 0, swappedBlocks ? blockBuffer2 : blockBuffer1, 0, blockBuffer1.length);
        }
    }
    public void swapEntityBuffers(boolean copy)
    {
        swappedEntities = !swappedEntities;
        if (copy)
        {
            Map<UUID, Entity> entitySource = swappedEntities ? entityBuffer1 : entityBuffer2;
            Map<UUID, Entity> entityDestination = swappedEntities ? entityBuffer2 : entityBuffer1;
            entityDestination.clear();
            for (Map.Entry<UUID, Entity> entry : entitySource.entrySet()) entityDestination.put(entry.getKey(), entry.getValue());
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
