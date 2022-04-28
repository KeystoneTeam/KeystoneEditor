package keystone.core.modules.history;

import com.mojang.serialization.Codec;
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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;

import java.util.*;

public class WorldHistoryChunk
{
    private static final Codec<PalettedContainer<BlockState>> BLOCK_STATES_CODEC = PalettedContainer.createCodec(net.minecraft.block.Block.STATE_IDS, BlockState.CODEC, PalettedContainer.PaletteProvider.BLOCK_STATE, Blocks.AIR.getDefaultState());

    public final int chunkX;
    public final int chunkY;
    public final int chunkZ;
    private final ServerWorldAccess world;

    private final PalettedContainer<BlockState> oldBlockStates;
    private PalettedContainer<BlockState> blockStateBuffer1;
    private PalettedContainer<BlockState> blockStateBuffer2;

    private final Map<BlockPos, NBTCompound> oldTileEntities;
    private final Map<BlockPos, NBTCompound> tileEntityBuffer1;
    private final Map<BlockPos, NBTCompound> tileEntityBuffer2;

    private final Map<UUID, Entity> oldEntities;
    private final Map<UUID, Entity> entityBuffer1;
    private final Map<UUID, Entity> entityBuffer2;
    private final Map<UUID, Entity> allEntities;

    private boolean swappedBlocks;
    private boolean swappedEntities;

    public WorldHistoryChunk(Vec3i chunkPosition, ServerWorldAccess world)
    {
        this.chunkX = chunkPosition.getX();
        this.chunkY = chunkPosition.getY();
        this.chunkZ = chunkPosition.getZ();
        this.world = world;

        Chunk chunk = world.getChunk(chunkX, chunkZ);
        ChunkSection section = world.getChunk(chunkX, chunkZ).getSection(chunk.getSectionIndex(chunkY << 4));

        if (!section.isEmpty())
        {
            this.oldBlockStates = section.getBlockStateContainer().copy();
            this.blockStateBuffer1 = section.getBlockStateContainer().copy();
            this.blockStateBuffer2 = section.getBlockStateContainer().copy();

            this.oldTileEntities = new HashMap<>();
            this.tileEntityBuffer1 = new HashMap<>();
            this.tileEntityBuffer2 = new HashMap<>();

            Set<BlockPos> tileEntityPositions = chunk.getBlockEntityPositions();
            for (BlockPos pos : tileEntityPositions)
            {
                if (ChunkSectionPos.getSectionCoord(pos.getY()) != chunkY) continue;
                BlockEntity tileEntity = chunk.getBlockEntity(pos);
                NbtCompound nbt = tileEntity.createNbtWithIdentifyingData();
                oldTileEntities.put(pos, new NBTCompound(nbt.copy()));
                tileEntityBuffer1.put(pos, new NBTCompound(nbt.copy()));
                tileEntityBuffer2.put(pos, new NBTCompound(nbt.copy()));
            }
        }
        else
        {
            this.oldBlockStates = new PalettedContainer<>(net.minecraft.block.Block.STATE_IDS, Blocks.AIR.getDefaultState(), PalettedContainer.PaletteProvider.BLOCK_STATE);
            this.blockStateBuffer1 = new PalettedContainer<>(net.minecraft.block.Block.STATE_IDS, Blocks.AIR.getDefaultState(), PalettedContainer.PaletteProvider.BLOCK_STATE);
            this.blockStateBuffer2 = new PalettedContainer<>(net.minecraft.block.Block.STATE_IDS, Blocks.AIR.getDefaultState(), PalettedContainer.PaletteProvider.BLOCK_STATE);

            this.oldTileEntities = new HashMap<>();
            this.tileEntityBuffer1 = new HashMap<>();
            this.tileEntityBuffer2 = new HashMap<>();
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

        if (nbt.contains("Blocks", NbtElement.COMPOUND_TYPE))
        {
            NbtCompound blocksNBT = nbt.getCompound("Blocks");

            NbtCompound oldNBT = blocksNBT.getCompound("Old");
            NbtCompound buffer1NBT = blocksNBT.getCompound("Buffer1");
            NbtCompound buffer2NBT = blocksNBT.getCompound("Buffer2");

            this.oldBlockStates = BLOCK_STATES_CODEC.parse(NbtOps.INSTANCE, oldNBT).promotePartial(this::onRecoverableError).getOrThrow(false, Keystone.LOGGER::error);
            this.blockStateBuffer1 = BLOCK_STATES_CODEC.parse(NbtOps.INSTANCE, buffer1NBT).promotePartial(this::onRecoverableError).getOrThrow(false, Keystone.LOGGER::error);
            this.blockStateBuffer2 = BLOCK_STATES_CODEC.parse(NbtOps.INSTANCE, buffer2NBT).promotePartial(this::onRecoverableError).getOrThrow(false, Keystone.LOGGER::error);

            this.swappedBlocks = blocksNBT.getBoolean("Swapped");
        }
        else
        {
            this.oldBlockStates = new PalettedContainer<>(net.minecraft.block.Block.STATE_IDS, Blocks.AIR.getDefaultState(), PalettedContainer.PaletteProvider.BLOCK_STATE);
            this.blockStateBuffer1 = new PalettedContainer<>(net.minecraft.block.Block.STATE_IDS, Blocks.AIR.getDefaultState(), PalettedContainer.PaletteProvider.BLOCK_STATE);
            this.blockStateBuffer2 = new PalettedContainer<>(net.minecraft.block.Block.STATE_IDS, Blocks.AIR.getDefaultState(), PalettedContainer.PaletteProvider.BLOCK_STATE);

            this.swappedBlocks = false;
        }

        if (nbt.contains("TileEntities", NbtElement.COMPOUND_TYPE))
        {
            NbtCompound tileEntitiesNBT = nbt.getCompound("TileEntities");

            this.oldTileEntities = NBTSerializer.deserializeTileEntities(tileEntitiesNBT.getList("Old", NbtElement.COMPOUND_TYPE));
            this.tileEntityBuffer1 = NBTSerializer.deserializeTileEntities(tileEntitiesNBT.getList("Buffer1", NbtElement.COMPOUND_TYPE));
            this.tileEntityBuffer2 = NBTSerializer.deserializeTileEntities(tileEntitiesNBT.getList("Buffer2", NbtElement.COMPOUND_TYPE));
        }
        else
        {
            this.oldTileEntities = new HashMap<>();
            this.tileEntityBuffer1 = new HashMap<>();
            this.tileEntityBuffer2 = new HashMap<>();
        }

        if (nbt.contains("Entities", NbtElement.COMPOUND_TYPE))
        {
            NbtCompound entitiesNBT = nbt.getCompound("Entities");

            this.oldEntities = NBTSerializer.deserializeEntities(entitiesNBT.getCompound("Old"));
            this.entityBuffer1 = NBTSerializer.deserializeEntities(entitiesNBT.getCompound("Buffer1"));
            this.entityBuffer2 = NBTSerializer.deserializeEntities(entitiesNBT.getCompound("Buffer2"));
            this.allEntities = NBTSerializer.deserializeEntities(entitiesNBT.getCompound("All"));

            this.swappedEntities = nbt.getBoolean("Swapped");
        }
        else
        {
            this.oldEntities = new HashMap<>();
            this.entityBuffer1 = new HashMap<>();
            this.entityBuffer2 = new HashMap<>();
            this.allEntities = new HashMap<>();

            this.swappedEntities = false;
        }
    }
    private void onRecoverableError(String error)
    {
        Keystone.LOGGER.error("Recoverable error when loading WorldHistoryChunk [" + chunkX + ", " + chunkY + ", " + chunkZ + "]: " + error);
    }

    public NbtCompound serialize()
    {
        Registry<net.minecraft.world.biome.Biome> biomeRegistry = world.getRegistryManager().get(Registry.BIOME_KEY);
        Codec<PalettedContainer<RegistryEntry<net.minecraft.world.biome.Biome>>> biomeCodec = PalettedContainer.createCodec(biomeRegistry.getIndexedEntries(), biomeRegistry.createEntryCodec(), PalettedContainer.PaletteProvider.BIOME, biomeRegistry.entryOf(BiomeKeys.PLAINS));

        NbtCompound nbt = new NbtCompound();

        nbt.putIntArray("ChunkPos", new int[] { chunkX, chunkY, chunkZ });
        nbt.putString("World", world.toServerWorld().getRegistryKey().getValue().toString());

        NbtCompound blocksNBT = new NbtCompound();
        blocksNBT.put("Old", BLOCK_STATES_CODEC.encodeStart(NbtOps.INSTANCE, this.oldBlockStates).getOrThrow(false, Keystone.LOGGER::error));
        blocksNBT.put("Buffer1", BLOCK_STATES_CODEC.encodeStart(NbtOps.INSTANCE, this.blockStateBuffer1).getOrThrow(false, Keystone.LOGGER::error));
        blocksNBT.put("Buffer2", BLOCK_STATES_CODEC.encodeStart(NbtOps.INSTANCE, this.blockStateBuffer2).getOrThrow(false, Keystone.LOGGER::error));
        blocksNBT.putBoolean("Swapped", this.swappedBlocks);
        nbt.put("Blocks", blocksNBT);

        NbtCompound tileEntitiesNBT = new NbtCompound();
        tileEntitiesNBT.put("Old", NBTSerializer.serializeTileEntities(oldTileEntities));
        tileEntitiesNBT.put("Buffer1", NBTSerializer.serializeTileEntities(tileEntityBuffer1));
        tileEntitiesNBT.put("Buffer2", NBTSerializer.serializeTileEntities(tileEntityBuffer2));
        nbt.put("TileEntities", tileEntitiesNBT);

        NbtCompound entitiesNBT = new NbtCompound();
        entitiesNBT.put("Old", NBTSerializer.serializeEntities(oldEntities));
        entitiesNBT.put("Buffer1", NBTSerializer.serializeEntities(entityBuffer1));
        entitiesNBT.put("Buffer2", NBTSerializer.serializeEntities(entityBuffer2));
        entitiesNBT.putBoolean("Swapped", this.swappedEntities);
        nbt.put("Entities", entitiesNBT);

        return nbt;
    }

    public BlockType getBlockType(int x, int y, int z, RetrievalMode retrievalMode)
    {
        x -= chunkX * 16;
        y -= chunkY * 16;
        z -= chunkZ * 16;

        switch (retrievalMode)
        {
            case ORIGINAL: return BlockTypeRegistry.fromMinecraftBlock(this.oldBlockStates.get(x, y, z));
            case LAST_SWAPPED: return BlockTypeRegistry.fromMinecraftBlock(swappedBlocks ? blockStateBuffer1.get(x, y, z) : blockStateBuffer2.get(x, y, z));
            case CURRENT: return BlockTypeRegistry.fromMinecraftBlock(swappedBlocks ? blockStateBuffer2.get(x, y, z) : blockStateBuffer1.get(x, y, z));
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
        BlockPos pos = new BlockPos(x, y, z);
        BlockType blockType = null;
        NBTCompound tileEntity = null;

        x -= chunkX * 16;
        y -= chunkY * 16;
        z -= chunkZ * 16;

        switch (retrievalMode)
        {
            case ORIGINAL:
                blockType = BlockTypeRegistry.fromMinecraftBlock(oldBlockStates.get(x, y, z));
                tileEntity = oldTileEntities.getOrDefault(pos, null);
                break;
            case LAST_SWAPPED:
                if (swappedBlocks)
                {
                    blockType = BlockTypeRegistry.fromMinecraftBlock(blockStateBuffer1.get(x, y, z));
                    tileEntity = tileEntityBuffer1.getOrDefault(pos, null);
                }
                else
                {
                    blockType = BlockTypeRegistry.fromMinecraftBlock(blockStateBuffer2.get(x, y, z));
                    tileEntity = tileEntityBuffer2.getOrDefault(pos, null);
                }
                break;
            case CURRENT:
                if (swappedBlocks)
                {
                    blockType = BlockTypeRegistry.fromMinecraftBlock(blockStateBuffer2.get(x, y, z));
                    tileEntity = tileEntityBuffer2.getOrDefault(pos, null);
                }
                else
                {
                    blockType = BlockTypeRegistry.fromMinecraftBlock(blockStateBuffer1.get(x, y, z));
                    tileEntity = tileEntityBuffer1.getOrDefault(pos, null);
                }
                break;
        }

        return new Block(blockType, tileEntity != null ? tileEntity.clone() : null);
    }

    public Biome getBiome(int x, int y, int z, RetrievalMode retrievalMode)
    {
        return new Biome(world.getBiome(new BlockPos(x, y, z)));
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
            tileEntityBuffer2.remove(new BlockPos(x, y, z));

            x -= chunkX * 16;
            y -= chunkY * 16;
            z -= chunkZ * 16;

            blockStateBuffer2.swapUnsafe(x, y, z, blockType.getMinecraftBlock());
        }
        else
        {
            tileEntityBuffer1.remove(new BlockPos(x, y, z));

            x -= chunkX * 16;
            y -= chunkY * 16;
            z -= chunkZ * 16;

            blockStateBuffer1.swapUnsafe(x, y, z, blockType.getMinecraftBlock());
        }
    }
    public void setBlock(int x, int y, int z, Block block)
    {
        BlockPos pos = new BlockPos(x, y, z);
        if (swappedBlocks)
        {
            if (block.tileEntity() == null) tileEntityBuffer2.remove(pos);
            else tileEntityBuffer2.put(pos, block.tileEntity());

            x -= chunkX * 16;
            y -= chunkY * 16;
            z -= chunkZ * 16;

            blockStateBuffer2.swapUnsafe(x, y, z, block.blockType().getMinecraftBlock());
        }
        else
        {
            if (block.tileEntity() == null) tileEntityBuffer1.remove(pos);
            else tileEntityBuffer1.put(pos, block.tileEntity());

            x -= chunkX * 16;
            y -= chunkY * 16;
            z -= chunkZ * 16;

            blockStateBuffer1.swapUnsafe(x, y, z, block.blockType().getMinecraftBlock());
        }
    }
    public void setEntity(Entity entity)
    {
        if (swappedEntities) entityBuffer2.put(entity.keystoneUUID(), entity);
        else entityBuffer1.put(entity.keystoneUUID(), entity);
        allEntities.put(entity.keystoneUUID(), entity);
    }

    public void swapBlockBuffers(boolean copy)
    {
        swappedBlocks = !swappedBlocks;
        if (copy)
        {
            if (swappedBlocks) this.blockStateBuffer2 = this.blockStateBuffer1.copy();
            else this.blockStateBuffer1 = this.blockStateBuffer2.copy();

            Map<BlockPos, NBTCompound> tileEntitySource = swappedBlocks ? tileEntityBuffer1 : tileEntityBuffer2;
            Map<BlockPos, NBTCompound> tileEntityDestination = swappedBlocks ? tileEntityBuffer2 : tileEntityBuffer1;
            tileEntityDestination.clear();
            tileEntityDestination.putAll(tileEntitySource);
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

    public void undo()
    {
        BlockPos start = new BlockPos(chunkX << 4, chunkY << 4, chunkZ << 4);
        for (int x = 0; x < 16; x++)
        {
            for (int y = 0; y < 16; y++)
            {
                for (int z = 0; z < 16; z++)
                {
                    BlockPos pos = start.add(x, y, z);
                    BlockState state = oldBlockStates.get(x, y, z);

                    world.toServerWorld().setBlockState(pos, state);
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
        BlockPos start = new BlockPos(chunkX << 4, chunkY << 4, chunkZ << 4);
        PalettedContainer<BlockState> blockStates = swappedBlocks ? blockStateBuffer2 : blockStateBuffer1;
        Map<BlockPos, NBTCompound> tileEntities = swappedEntities ? tileEntityBuffer2 : tileEntityBuffer1;
        Map<UUID, Entity> entities = swappedEntities ? entityBuffer2 : entityBuffer1;

        for (int x = 0; x < 16; x++)
        {
            for (int y = 0; y < 16; y++)
            {
                for (int z = 0; z < 16; z++)
                {
                    BlockPos pos = start.add(x, y, z);
                    BlockState state = blockStates.get(x, y, z);

                    world.toServerWorld().setBlockState(pos, state);
                    NBTCompound blockData = tileEntities.getOrDefault(pos, null);
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
            }
        }

        for (UUID entityID : allEntities.keySet())
        {
            if (!entities.containsKey(entityID))
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
        for (Entity entity : entities.values()) entity.updateMinecraftEntity(world);
    }
}