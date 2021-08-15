package keystone.core.modules.history;

import keystone.api.Keystone;
import keystone.api.enums.RetrievalMode;
import keystone.api.wrappers.Biome;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.entities.Entity;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.renderer.common.models.DimensionId;
import keystone.core.utils.NBTSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
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
    public final int chunkX;
    public final int chunkY;
    public final int chunkZ;
    private final IServerWorld world;

    private final Block[] oldBlocks;
    private final Block[] blockBuffer1;
    private final Block[] blockBuffer2;

    private final Biome[] oldBiomes;
    private final Biome[] biomeBuffer1;
    private final Biome[] biomeBuffer2;

    private final Map<UUID, Entity> oldEntities;
    private final Map<UUID, Entity> entityBuffer1;
    private final Map<UUID, Entity> entityBuffer2;
    private final Map<UUID, Entity> allEntities;

    private boolean swappedBlocks;
    private boolean swappedBiomes;
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

        oldBiomes = new Biome[4096];
        biomeBuffer1 = new Biome[4096];
        biomeBuffer2 = new Biome[4096];

        int i = 0;
        for (int x = chunkX * 16; x < (chunkX + 1) * 16; x++)
        {
            for (int y = chunkY * 16; y < (chunkY + 1) * 16; y++)
            {
                for (int z = chunkZ * 16; z < (chunkZ + 1) * 16; z++)
                {
                    BlockPos pos = new BlockPos(x, y, z);
                    oldBlocks[i] = new Block(world.getBlockState(pos), world.getBlockEntity(pos));
                    oldBiomes[i] = new Biome(world.getBiome(pos));
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
    public WorldHistoryChunk(CompoundNBT nbt)
    {
        WorldCacheModule worldCache = Keystone.getModule(WorldCacheModule.class);
        int[] chunkPos = nbt.getIntArray("ChunkPos");
        chunkX = chunkPos[0];
        chunkY = chunkPos[1];
        chunkZ = chunkPos[2];
        world = worldCache.getDimensionServerWorld(DimensionId.from(new ResourceLocation(nbt.getString("World"))));

        oldBlocks = NBTSerializer.deserializeBlocks(nbt.getCompound("OldBlocks"));
        blockBuffer1 = NBTSerializer.deserializeBlocks(nbt.getCompound("BlockBuffer1"));
        blockBuffer2 = NBTSerializer.deserializeBlocks(nbt.getCompound("BlockBuffer2"));
        swappedBlocks = nbt.getBoolean("SwappedBlocks");

        oldBiomes = NBTSerializer.deserializeBiomes(nbt.getCompound("OldBiomes"));
        biomeBuffer1 = NBTSerializer.deserializeBiomes(nbt.getCompound("BiomeBuffer1"));
        biomeBuffer2 = NBTSerializer.deserializeBiomes(nbt.getCompound("BiomeBuffer2"));
        swappedBiomes = nbt.getBoolean("SwappedBiomes");

        oldEntities = NBTSerializer.deserializeEntities(nbt.getCompound("OldEntities"));
        entityBuffer1 = NBTSerializer.deserializeEntities(nbt.getCompound("EntityBuffer1"));
        entityBuffer2 = NBTSerializer.deserializeEntities(nbt.getCompound("EntityBuffer2"));
        allEntities = NBTSerializer.deserializeEntities(nbt.getCompound("AllEntities"));
        swappedEntities = nbt.getBoolean("SwappedEntities");
    }

    public CompoundNBT serialize()
    {
        CompoundNBT nbt = new CompoundNBT();

        nbt.putIntArray("ChunkPos", new int[] { chunkX, chunkY, chunkZ });
        nbt.putString("World", world.getLevel().dimension().location().toString());

        nbt.put("OldBlocks", NBTSerializer.serializeBlocks(oldBlocks));
        nbt.put("BlockBuffer1", NBTSerializer.serializeBlocks(blockBuffer1));
        nbt.put("BlockBuffer2", NBTSerializer.serializeBlocks(blockBuffer2));
        nbt.putBoolean("SwappedBlocks", swappedBlocks);

        nbt.put("OldBiomes", NBTSerializer.serializeBiomes(oldBiomes));
        nbt.put("BiomeBuffer1", NBTSerializer.serializeBiomes(biomeBuffer1));
        nbt.put("BiomeBuffer2", NBTSerializer.serializeBiomes(biomeBuffer2));
        nbt.putBoolean("SwappedBiomes", swappedBiomes);

        nbt.put("OldEntities", NBTSerializer.serializeEntities(oldEntities));
        nbt.put("EntityBuffer1", NBTSerializer.serializeEntities(entityBuffer1));
        nbt.put("EntityBuffer2", NBTSerializer.serializeEntities(entityBuffer2));
        nbt.put("AllEntities", NBTSerializer.serializeEntities(allEntities));
        nbt.putBoolean("SwappedEntities", swappedEntities);

        return nbt;
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
    public Biome getBiome(int x, int y, int z, RetrievalMode retrievalMode)
    {
        int index = getIndex(x, y, z);
        Biome biome = null;

        switch (retrievalMode)
        {
            case ORIGINAL: return oldBiomes[index];
            case LAST_SWAPPED: biome = swappedBiomes ? biomeBuffer1[index] : biomeBuffer2[index]; break;
            case CURRENT: biome = swappedBiomes ? biomeBuffer2[index] : biomeBuffer1[index]; break;
        }

        if (biome == null) return oldBiomes[index];
        else return biome;
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
    public void setBiome(int x, int y, int z, Biome biome)
    {
        if (swappedBiomes) biomeBuffer2[getIndex(x, y, z)] = biome;
        else biomeBuffer1[getIndex(x, y, z)] = biome;
    }
    public void setEntity(Entity entity)
    {
        if (swappedEntities) entityBuffer2.put(entity.keystoneUUID(), entity);
        else entityBuffer1.put(entity.keystoneUUID(), entity);
        allEntities.put(entity.keystoneUUID(), entity);
    }

    //TODO: Add biome setting support
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
                    Biome biome = oldBiomes[i];

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
    public void swapBiomeBuffers(boolean copy)
    {
        swappedBiomes = !swappedBiomes;
        if (copy)
        {
            System.arraycopy(swappedBiomes ? biomeBuffer1 : biomeBuffer2, 0, swappedBiomes ? biomeBuffer2 : biomeBuffer1, 0, biomeBuffer1.length);
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
