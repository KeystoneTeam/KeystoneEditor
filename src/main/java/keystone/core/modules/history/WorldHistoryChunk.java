package keystone.core.modules.history;

import keystone.api.Keystone;
import keystone.api.enums.RetrievalMode;
import keystone.api.wrappers.Biome;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.blocks.BlockType;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.entities.Entity;
import keystone.api.wrappers.nbt.NBTCompound;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.registries.BlockTypeRegistry;
import keystone.core.utils.NBTSerializer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.ServerWorldAccess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WorldHistoryChunk
{
    public final int chunkX;
    public final int chunkY;
    public final int chunkZ;
    private final ServerWorldAccess world;

    private final BlockType[] oldBlockTypes;
    private final BlockType[] blockTypeBuffer1;
    private final BlockType[] blockTypeBuffer2;
    private final Map<BlockPos, NBTCompound> oldTileEntities;
    private final Map<BlockPos, NBTCompound> tileEntityBuffer1;
    private final Map<BlockPos, NBTCompound> tileEntityBuffer2;

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

    public WorldHistoryChunk(Vec3i chunkPosition, ServerWorldAccess world)
    {
        this.chunkX = chunkPosition.getX();
        this.chunkY = chunkPosition.getY();
        this.chunkZ = chunkPosition.getZ();
        this.world = world;

        oldBlockTypes = new BlockType[4096];
        blockTypeBuffer1 = new BlockType[4096];
        blockTypeBuffer2 = new BlockType[4096];
        oldTileEntities = new HashMap<>();
        tileEntityBuffer1 = new HashMap<>();
        tileEntityBuffer2 = new HashMap<>();

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
                    oldBlockTypes[i] = BlockTypeRegistry.fromMinecraftBlock(world.getBlockState(pos));
                    blockTypeBuffer1[i] = oldBlockTypes[i];
                    blockTypeBuffer2[i] = oldBlockTypes[i];
                    oldBiomes[i] = new Biome(world.getBiome(pos));
                    biomeBuffer1[i] = oldBiomes[i];
                    biomeBuffer2[i] = oldBiomes[i];
                    i++;

                    BlockEntity tileEntity = world.getBlockEntity(pos);
                    if (tileEntity != null)
                    {
                        NbtCompound nbt = tileEntity.createNbtWithIdentifyingData();
                        oldTileEntities.put(pos, new NBTCompound(nbt.copy()));
                        tileEntityBuffer1.put(pos, new NBTCompound(nbt.copy()));
                        tileEntityBuffer2.put(pos, new NBTCompound(nbt.copy()));
                    }
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
        Box bb = new Box(startX, startY, startZ, startX + 16, startY + 16, startZ + 16);
        List<net.minecraft.entity.Entity> mcEntities = world.getNonSpectatingEntities(net.minecraft.entity.Entity.class, bb);
        for (net.minecraft.entity.Entity mcEntity : mcEntities)
        {
            Entity entity = new Entity(mcEntity);
            oldEntities.put(entity.keystoneUUID(), entity);
            entityBuffer1.put(entity.keystoneUUID(), entity);
            entityBuffer2.put(entity.keystoneUUID(), entity);
            allEntities.put(entity.keystoneUUID(), entity);
        }
    }
    public WorldHistoryChunk(NbtCompound nbt)
    {
        WorldCacheModule worldCache = Keystone.getModule(WorldCacheModule.class);
        int[] chunkPos = nbt.getIntArray("ChunkPos");
        chunkX = chunkPos[0];
        chunkY = chunkPos[1];
        chunkZ = chunkPos[2];
        world = worldCache.getDimensionWorld(WorldCacheModule.getDimensionKey(new Identifier(nbt.getString("World"))));

        oldBlockTypes = NBTSerializer.deserializeBlockTypes(nbt.getList("OldBlocks", NbtElement.SHORT_TYPE));
        blockTypeBuffer1 = NBTSerializer.deserializeBlockTypes(nbt.getList("BlockBuffer1", NbtElement.SHORT_TYPE));
        blockTypeBuffer2 = NBTSerializer.deserializeBlockTypes(nbt.getList("BlockBuffer2", NbtElement.SHORT_TYPE));
        oldTileEntities = NBTSerializer.deserializeTileEntities(nbt.getList("OldTileEntities", NbtElement.COMPOUND_TYPE));
        tileEntityBuffer1 = NBTSerializer.deserializeTileEntities(nbt.getList("BlockEntityBuffer1", NbtElement.COMPOUND_TYPE));
        tileEntityBuffer2 = NBTSerializer.deserializeTileEntities(nbt.getList("BlockEntityBuffer2", NbtElement.COMPOUND_TYPE));
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

    public NbtCompound serialize()
    {
        NbtCompound nbt = new NbtCompound();

        nbt.putIntArray("ChunkPos", new int[] { chunkX, chunkY, chunkZ });
        nbt.putString("World", world.toServerWorld().getRegistryKey().getValue().toString());

        nbt.put("OldBlocks", NBTSerializer.serializeBlockTypes(oldBlockTypes));
        nbt.put("BlockBuffer1", NBTSerializer.serializeBlockTypes(blockTypeBuffer1));
        nbt.put("BlockBuffer2", NBTSerializer.serializeBlockTypes(blockTypeBuffer2));
        nbt.put("OldTileEntities", NBTSerializer.serializeTileEntities(oldTileEntities));
        nbt.put("BlockEntityBuffer1", NBTSerializer.serializeTileEntities(tileEntityBuffer1));
        nbt.put("BlockEntityBuffer2", NBTSerializer.serializeTileEntities(tileEntityBuffer2));
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

    public BlockType getBlockType(int x, int y, int z, RetrievalMode retrievalMode)
    {
        int index = getIndex(x, y, z);
        switch (retrievalMode)
        {
            case ORIGINAL: return oldBlockTypes[index];
            case LAST_SWAPPED: return swappedBlocks ? blockTypeBuffer1[index] : blockTypeBuffer2[index];
            case CURRENT: return swappedBlocks ? blockTypeBuffer2[index] : blockTypeBuffer1[index];
        }
        return null;
    }
    public NBTCompound getBlockEntity(int x, int y, int z, RetrievalMode retrievalMode)
    {
        BlockPos pos = new BlockPos(x, y, z);
        NBTCompound tileEntity = null;
        switch (retrievalMode)
        {
            case ORIGINAL: tileEntity = oldTileEntities.getOrDefault(pos, null); break;
            case LAST_SWAPPED: tileEntity = swappedBlocks ? tileEntityBuffer1.getOrDefault(pos, null) : tileEntityBuffer2.getOrDefault(pos, null); break;
            case CURRENT: tileEntity = swappedBlocks ? tileEntityBuffer2.getOrDefault(pos, null) : tileEntityBuffer1.getOrDefault(pos, null); break;
        }

        if (tileEntity != null) return tileEntity.clone();
        else return null;
    }
    public Block getBlock(int x, int y, int z, RetrievalMode retrievalMode)
    {
        int index = getIndex(x, y, z);
        BlockPos pos = new BlockPos(x, y, z);
        BlockType blockType = null;
        NBTCompound tileEntity = null;

        switch (retrievalMode)
        {
            case ORIGINAL:
                blockType = oldBlockTypes[index];
                tileEntity = oldTileEntities.getOrDefault(pos, null);
                break;
            case LAST_SWAPPED:
                if (swappedBlocks)
                {
                    blockType = blockTypeBuffer1[index];
                    tileEntity = tileEntityBuffer1.getOrDefault(pos, null);
                }
                else
                {
                    blockType = blockTypeBuffer2[index];
                    tileEntity = tileEntityBuffer2.getOrDefault(pos, null);
                }
                break;
            case CURRENT:
                if (swappedBlocks)
                {
                    blockType = blockTypeBuffer2[index];
                    tileEntity = tileEntityBuffer2.getOrDefault(pos, null);
                }
                else
                {
                    blockType = blockTypeBuffer1[index];
                    tileEntity = tileEntityBuffer1.getOrDefault(pos, null);
                }
                break;
        }

        return new Block(blockType, tileEntity != null ? tileEntity.clone() : null);
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

    public void setBlock(int x, int y, int z, BlockType blockType)
    {
        if (swappedBlocks)
        {
            blockTypeBuffer2[getIndex(x, y, z)] = blockType;
            tileEntityBuffer2.remove(new BlockPos(x, y, z));
        }
        else
        {
            blockTypeBuffer1[getIndex(x, y, z)] = blockType;
            tileEntityBuffer1.remove(new BlockPos(x, y, z));
        }
    }
    public void setBlock(int x, int y, int z, Block block)
    {
        BlockPos pos = new BlockPos(x, y, z);
        if (swappedBlocks)
        {
            blockTypeBuffer2[getIndex(x, y, z)] = block.blockType();
            if (block.tileEntity() == null) tileEntityBuffer2.remove(pos);
            else tileEntityBuffer2.put(pos, block.tileEntity());
        }
        else
        {
            blockTypeBuffer1[getIndex(x, y, z)] = block.blockType();
            if (block.tileEntity() == null) tileEntityBuffer1.remove(pos);
            else tileEntityBuffer1.put(pos, block.tileEntity());
        }
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
                    BlockType blockType = oldBlockTypes[i];

                    world.toServerWorld().setBlockState(pos, blockType.getMinecraftBlock());
                    NBTCompound blockData = oldTileEntities.getOrDefault(pos, null);
                    if (blockData != null)
                    {
                        NbtCompound tileEntityData = blockData.getMinecraftNBT().copy();
                        tileEntityData.putInt("x", x);
                        tileEntityData.putInt("y", y);
                        tileEntityData.putInt("z", z);

                        BlockEntity tileEntity = world.getBlockEntity(pos);
                        if (tileEntity != null) tileEntity.readNbt(tileEntityData);
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
                net.minecraft.entity.Entity mcEntity = world.toServerWorld().getEntity(entity.minecraftUUID());
                if (mcEntity != null)
                {
                    mcEntity.discard();
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
                    BlockType blockType = swappedBlocks ? blockTypeBuffer2[i] : blockTypeBuffer1[i];
                    if (blockType != null && blockType.getMinecraftBlock() != null)
                    {
                        BlockPos pos = new BlockPos(x, y, z);
                        world.toServerWorld().setBlockState(pos, blockType.getMinecraftBlock());

                        NBTCompound blockData = (swappedBlocks ? tileEntityBuffer2 : tileEntityBuffer1).getOrDefault(pos, null);
                        if (blockData != null)
                        {
                            NbtCompound tileEntityData = blockData.getMinecraftNBT().copy();
                            tileEntityData.putInt("x", x);
                            tileEntityData.putInt("y", y);
                            tileEntityData.putInt("z", z);

                            BlockEntity tileEntity = world.getBlockEntity(pos);
                            if (tileEntity != null) tileEntity.readNbt(tileEntityData);
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
                net.minecraft.entity.Entity mcEntity = world.toServerWorld().getEntity(entity.minecraftUUID());
                if (mcEntity != null)
                {
                    mcEntity.discard();
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
            System.arraycopy(swappedBlocks ? blockTypeBuffer1 : blockTypeBuffer2, 0, swappedBlocks ? blockTypeBuffer2 : blockTypeBuffer1, 0, blockTypeBuffer1.length);
            Map<BlockPos, NBTCompound> tileEntitySource = swappedBlocks ? tileEntityBuffer1 : tileEntityBuffer2;
            Map<BlockPos, NBTCompound> tileEntityDestination = swappedBlocks ? tileEntityBuffer2 : tileEntityBuffer1;
            tileEntityDestination.clear();
            tileEntityDestination.putAll(tileEntitySource);
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
            entityDestination.putAll(entitySource);
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
