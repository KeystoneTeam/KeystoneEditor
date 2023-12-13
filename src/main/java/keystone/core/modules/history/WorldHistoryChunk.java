package keystone.core.modules.history;

import keystone.api.Keystone;
import keystone.api.enums.RetrievalMode;
import keystone.api.wrappers.Biome;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.blocks.BlockType;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.entities.Entity;
import keystone.api.wrappers.nbt.NBTCompound;
import keystone.core.KeystoneGlobalState;
import keystone.core.mixins.ChunkSectionAccessor;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.registries.BlockTypeRegistry;
import keystone.core.utils.NBTSerializer;
import keystone.core.utils.PalettedArray;
import keystone.core.utils.WorldRegistries;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WorldHistoryChunk
{
    private static final int PLACE_FLAGS = net.minecraft.block.Block.NOTIFY_LISTENERS | net.minecraft.block.Block.SKIP_DROPS;
    private static final int UPDATE_FLAGS = net.minecraft.block.Block.NOTIFY_ALL | net.minecraft.block.Block.SKIP_DROPS;
    
    public final int chunkX;
    public final int chunkY;
    public final int chunkZ;
    
    private final HistoryStackFrame historyEntry;
    private final ServerWorldAccess world;
    private final Chunk chunk;
    private final ChunkSection chunkSection;

    private final PalettedArray<BlockState> oldBlockStates;
    private PalettedArray<BlockState> blockStateBuffer1;
    private PalettedArray<BlockState> blockStateBuffer2;

    private final ConcurrentHashMap<BlockPos, NBTCompound> oldTileEntities;
    private final ConcurrentHashMap<BlockPos, NBTCompound> tileEntityBuffer1;
    private final ConcurrentHashMap<BlockPos, NBTCompound> tileEntityBuffer2;

    private final PalettedArray<RegistryEntry<net.minecraft.world.biome.Biome>> oldBiomes;
    private PalettedArray<RegistryEntry<net.minecraft.world.biome.Biome>> biomeBuffer1;
    private PalettedArray<RegistryEntry<net.minecraft.world.biome.Biome>> biomeBuffer2;
    private boolean biomesChanged;

    private final ConcurrentHashMap<UUID, Entity> oldEntities;
    private final ConcurrentHashMap<UUID, Entity> entityBuffer1;
    private final ConcurrentHashMap<UUID, Entity> entityBuffer2;
    private final ConcurrentHashMap<UUID, Entity> allEntities;

    private boolean swappedBlocks;
    private boolean swappedEntities;
    private boolean swappedBiomes;

    public WorldHistoryChunk(HistoryStackFrame historyEntry, Vec3i chunkPosition, @NotNull ServerWorldAccess world)
    {
        this.chunkX = chunkPosition.getX();
        this.chunkY = chunkPosition.getY();
        this.chunkZ = chunkPosition.getZ();

        this.historyEntry = historyEntry;
        this.world = world;
        this.chunk = world.getChunk(chunkX, chunkZ);
        this.chunkSection = world.getChunk(chunkX, chunkZ).getSection(chunk.getSectionIndex(chunkY << 4));

        if (!chunkSection.isEmpty())
        {
            this.oldBlockStates = copyContainer(chunkSection.getBlockStateContainer(), 16, 16, 16);
            this.blockStateBuffer1 = this.oldBlockStates.copy();
            this.blockStateBuffer2 = this.oldBlockStates.copy();

            this.oldTileEntities = new ConcurrentHashMap<>();
            this.tileEntityBuffer1 = new ConcurrentHashMap<>();
            this.tileEntityBuffer2 = new ConcurrentHashMap<>();

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
            this.oldBlockStates = new PalettedArray<>(4096, 1, Blocks.AIR.getDefaultState());
            this.blockStateBuffer1 = new PalettedArray<>(4096, 1, Blocks.AIR.getDefaultState());
            this.blockStateBuffer2 = new PalettedArray<>(4096, 1, Blocks.AIR.getDefaultState());

            this.oldTileEntities = new ConcurrentHashMap<>();
            this.tileEntityBuffer1 = new ConcurrentHashMap<>();
            this.tileEntityBuffer2 = new ConcurrentHashMap<>();
        }

        this.oldBiomes = copyContainer(chunkSection.getBiomeContainer() instanceof PalettedContainer<RegistryEntry<net.minecraft.world.biome.Biome>> container ? container : chunkSection.getBiomeContainer().slice(), 4, 4, 4);
        this.biomeBuffer1 = this.oldBiomes.copy();
        this.biomeBuffer2 = this.oldBiomes.copy();

        oldEntities = new ConcurrentHashMap<>();
        entityBuffer1 = new ConcurrentHashMap<>();
        entityBuffer2 = new ConcurrentHashMap<>();
        allEntities = new ConcurrentHashMap<>();

        int startX = chunkX << 4;
        int startY = chunkY << 4;
        int startZ = chunkZ << 4;
        Box bb = new Box(startX, startY, startZ, startX + 16, startY + 16, startZ + 16);
        List<net.minecraft.entity.Entity> mcEntities = world.getNonSpectatingEntities(net.minecraft.entity.Entity.class, bb);
        for (net.minecraft.entity.Entity mcEntity : mcEntities)
        {
            Entity entity = new Entity(mcEntity);
            oldEntities.put(entity.keystoneUUID(), entity);
            entityBuffer1.put(entity.keystoneUUID(), entity.duplicate());
            entityBuffer2.put(entity.keystoneUUID(), entity.duplicate());
            allEntities.put(entity.keystoneUUID(), entity.duplicate());
        }
    }
    
    //region NBT Serialization
    public WorldHistoryChunk(HistoryStackFrame historyEntry, NbtCompound nbt)
    {
        WorldCacheModule worldCache = Keystone.getModule(WorldCacheModule.class);
        int[] chunkPos = nbt.getIntArray("ChunkPos");
        chunkX = chunkPos[0];
        chunkY = chunkPos[1];
        chunkZ = chunkPos[2];
    
        this.historyEntry = historyEntry;
        world = worldCache.getDimensionWorld(WorldCacheModule.getDimensionKey(new Identifier(nbt.getString("World"))));
        chunk = world.getChunk(chunkX, chunkZ);
        chunkSection = world.getChunk(chunkX, chunkZ).getSection(chunk.getSectionIndex(chunkY << 4));

        if (nbt.contains("Blocks", NbtElement.COMPOUND_TYPE))
        {
            NbtCompound blocksNBT = nbt.getCompound("Blocks");

            NbtCompound oldNBT = blocksNBT.getCompound("Old");
            NbtCompound buffer1NBT = blocksNBT.getCompound("Buffer1");
            NbtCompound buffer2NBT = blocksNBT.getCompound("Buffer2");

            this.oldBlockStates = new PalettedArray<>(oldNBT, serialized -> NbtHelper.toBlockState(world.createCommandRegistryWrapper(RegistryKeys.BLOCK), (NbtCompound)serialized));
            this.blockStateBuffer1 = new PalettedArray<>(buffer1NBT, serialized -> NbtHelper.toBlockState(world.createCommandRegistryWrapper(RegistryKeys.BLOCK), (NbtCompound)serialized));
            this.blockStateBuffer2 = new PalettedArray<>(buffer2NBT, serialized -> NbtHelper.toBlockState(world.createCommandRegistryWrapper(RegistryKeys.BLOCK), (NbtCompound)serialized));

            this.swappedBlocks = blocksNBT.getBoolean("Swapped");
        }
        else
        {
            this.oldBlockStates = new PalettedArray<>(4096, 1, Blocks.AIR.getDefaultState());
            this.blockStateBuffer1 = new PalettedArray<>(4096, 1, Blocks.AIR.getDefaultState());
            this.blockStateBuffer2 = new PalettedArray<>(4096, 1, Blocks.AIR.getDefaultState());

            this.swappedBlocks = false;
        }

        if (nbt.contains("TileEntities", NbtElement.COMPOUND_TYPE))
        {
            NbtCompound tileEntitiesNBT = nbt.getCompound("TileEntities");

            this.oldTileEntities = new ConcurrentHashMap<>(NBTSerializer.deserializeTileEntities(tileEntitiesNBT.getList("Old", NbtElement.COMPOUND_TYPE)));
            this.tileEntityBuffer1 = new ConcurrentHashMap<>(NBTSerializer.deserializeTileEntities(tileEntitiesNBT.getList("Buffer1", NbtElement.COMPOUND_TYPE)));
            this.tileEntityBuffer2 = new ConcurrentHashMap<>(NBTSerializer.deserializeTileEntities(tileEntitiesNBT.getList("Buffer2", NbtElement.COMPOUND_TYPE)));
        }
        else
        {
            this.oldTileEntities = new ConcurrentHashMap<>();
            this.tileEntityBuffer1 = new ConcurrentHashMap<>();
            this.tileEntityBuffer2 = new ConcurrentHashMap<>();
        }

        if (nbt.contains("Biomes", NbtElement.COMPOUND_TYPE))
        {
            NbtCompound biomesNBT = nbt.getCompound("Biomes");

            NbtCompound oldNBT = biomesNBT.getCompound("Old");
            NbtCompound buffer1NBT = biomesNBT.getCompound("Buffer1");
            NbtCompound buffer2NBT = biomesNBT.getCompound("Buffer2");

            Registry<net.minecraft.world.biome.Biome> biomeRegistry = WorldRegistries.biomes(world.toServerWorld());
            this.oldBiomes = new PalettedArray<>(oldNBT, serialized ->
            {
                Identifier identifier = new Identifier(serialized.asString());
                return biomeRegistry.getEntry(RegistryKey.of(RegistryKeys.BIOME, identifier)).get();
            });
            this.biomeBuffer1 = new PalettedArray<>(buffer1NBT, serialized ->
            {
                Identifier identifier = new Identifier(serialized.asString());
                return biomeRegistry.getEntry(RegistryKey.of(RegistryKeys.BIOME, identifier)).get();
            });
            this.biomeBuffer2 = new PalettedArray<>(buffer2NBT, serialized ->
            {
                Identifier identifier = new Identifier(serialized.asString());
                return biomeRegistry.getEntry(RegistryKey.of(RegistryKeys.BIOME, identifier)).get();
            });

            this.swappedBiomes = biomesNBT.getBoolean("Swapped");
            this.biomesChanged = biomesNBT.getBoolean("BiomesChanged");
        }
        else
        {
            this.oldBiomes = new PalettedArray<>(64, 1, WorldRegistries.biomes(world.toServerWorld()).entryOf(BiomeKeys.THE_VOID));
            this.biomeBuffer1 = new PalettedArray<>(64, 1, WorldRegistries.biomes(world.toServerWorld()).entryOf(BiomeKeys.THE_VOID));
            this.biomeBuffer2 = new PalettedArray<>(64, 1, WorldRegistries.biomes(world.toServerWorld()).entryOf(BiomeKeys.THE_VOID));
            this.swappedBiomes = false;
            this.biomesChanged = false;
        }

        if (nbt.contains("Entities", NbtElement.COMPOUND_TYPE))
        {
            NbtCompound entitiesNBT = nbt.getCompound("Entities");

            this.oldEntities = new ConcurrentHashMap<>(NBTSerializer.deserializeEntities(entitiesNBT.getCompound("Old")));
            this.entityBuffer1 = new ConcurrentHashMap<>(NBTSerializer.deserializeEntities(entitiesNBT.getCompound("Buffer1")));
            this.entityBuffer2 = new ConcurrentHashMap<>(NBTSerializer.deserializeEntities(entitiesNBT.getCompound("Buffer2")));
            this.allEntities = new ConcurrentHashMap<>(NBTSerializer.deserializeEntities(entitiesNBT.getCompound("All")));

            this.swappedEntities = entitiesNBT.getBoolean("Swapped");
        }
        else
        {
            this.oldEntities = new ConcurrentHashMap<>();
            this.entityBuffer1 = new ConcurrentHashMap<>();
            this.entityBuffer2 = new ConcurrentHashMap<>();
            this.allEntities = new ConcurrentHashMap<>();

            this.swappedEntities = false;
        }
    }
    public NbtCompound serialize()
    {
        NbtCompound nbt = new NbtCompound();
        
        nbt.putIntArray("ChunkPos", new int[] { chunkX, chunkY, chunkZ });
        nbt.putString("World", world.toServerWorld().getRegistryKey().getValue().toString());
        
        NbtCompound blocksNBT = new NbtCompound();
        blocksNBT.put("Old", this.oldBlockStates.serialize(NbtHelper::fromBlockState));
        blocksNBT.put("Buffer1", this.blockStateBuffer1.serialize(NbtHelper::fromBlockState));
        blocksNBT.put("Buffer2", this.blockStateBuffer2.serialize(NbtHelper::fromBlockState));
        blocksNBT.putBoolean("Swapped", this.swappedBlocks);
        nbt.put("Blocks", blocksNBT);
        
        NbtCompound tileEntitiesNBT = new NbtCompound();
        tileEntitiesNBT.put("Old", NBTSerializer.serializeTileEntities(oldTileEntities));
        tileEntitiesNBT.put("Buffer1", NBTSerializer.serializeTileEntities(tileEntityBuffer1));
        tileEntitiesNBT.put("Buffer2", NBTSerializer.serializeTileEntities(tileEntityBuffer2));
        nbt.put("TileEntities", tileEntitiesNBT);
        
        NbtCompound biomesNBT = new NbtCompound();
        biomesNBT.put("Old", this.oldBiomes.serialize(biome -> NbtString.of(biome.getKey().get().getValue().toString())));
        biomesNBT.put("Buffer1", this.biomeBuffer1.serialize(biome -> NbtString.of(biome.getKey().get().getValue().toString())));
        biomesNBT.put("Buffer2", this.biomeBuffer2.serialize(biome -> NbtString.of(biome.getKey().get().getValue().toString())));
        biomesNBT.putBoolean("Swapped", this.swappedBiomes);
        biomesNBT.putBoolean("BiomesChanged", this.biomesChanged);
        nbt.put("Biomes", biomesNBT);
        
        NbtCompound entitiesNBT = new NbtCompound();
        entitiesNBT.put("Old", NBTSerializer.serializeEntities(oldEntities));
        entitiesNBT.put("Buffer1", NBTSerializer.serializeEntities(entityBuffer1));
        entitiesNBT.put("Buffer2", NBTSerializer.serializeEntities(entityBuffer2));
        entitiesNBT.put("All", NBTSerializer.serializeEntities(allEntities));
        entitiesNBT.putBoolean("Swapped", this.swappedEntities);
        nbt.put("Entities", entitiesNBT);
        
        return nbt;
    }
    //endregion

    public RegistryKey<World> getRegistryKey() { return world.toServerWorld().getRegistryKey(); }
    public boolean isBiomesChanged() { return biomesChanged; }

    //region Content Getters
    public BlockType getBlockType(int x, int y, int z, RetrievalMode retrievalMode)
    {
        x -= chunkX * 16;
        y -= chunkY * 16;
        z -= chunkZ * 16;

        switch (retrievalMode)
        {
            case ORIGINAL: return BlockTypeRegistry.fromMinecraftBlock(this.oldBlockStates.get(z + y * 16 + x * 256));
            case LAST_SWAPPED: return BlockTypeRegistry.fromMinecraftBlock(swappedBlocks ? blockStateBuffer1.get(z + y * 16 + x * 256) : blockStateBuffer2.get(z + y * 16 + x * 256));
            case CURRENT: return BlockTypeRegistry.fromMinecraftBlock(swappedBlocks ? blockStateBuffer2.get(z + y * 16 + x * 256) : blockStateBuffer1.get(z + y * 16 + x * 256));
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
                blockType = BlockTypeRegistry.fromMinecraftBlock(oldBlockStates.get(z + y * 16 + x * 256));
                tileEntity = oldTileEntities.getOrDefault(pos, null);
                break;
            case LAST_SWAPPED:
                if (swappedBlocks)
                {
                    blockType = BlockTypeRegistry.fromMinecraftBlock(blockStateBuffer1.get(z + y * 16 + x * 256));
                    tileEntity = tileEntityBuffer1.getOrDefault(pos, null);
                }
                else
                {
                    blockType = BlockTypeRegistry.fromMinecraftBlock(blockStateBuffer2.get(z + y * 16 + x * 256));
                    tileEntity = tileEntityBuffer2.getOrDefault(pos, null);
                }
                break;
            case CURRENT:
                if (swappedBlocks)
                {
                    blockType = BlockTypeRegistry.fromMinecraftBlock(blockStateBuffer2.get(z + y * 16 + x * 256));
                    tileEntity = tileEntityBuffer2.getOrDefault(pos, null);
                }
                else
                {
                    blockType = BlockTypeRegistry.fromMinecraftBlock(blockStateBuffer1.get(z + y * 16 + x * 256));
                    tileEntity = tileEntityBuffer1.getOrDefault(pos, null);
                }
                break;
        }

        return new Block(blockType, tileEntity != null ? tileEntity.clone() : null);
    }
    public Biome getBiome(int x, int y, int z, RetrievalMode retrievalMode)
    {
        x -= chunkX * 16;
        y -= chunkY * 16;
        z -= chunkZ * 16;

        int biomeX = BiomeCoords.fromBlock(x);
        int biomeY = BiomeCoords.fromBlock(y);
        int biomeZ = BiomeCoords.fromBlock(z);
        int index = biomeZ + biomeY * 4 + biomeX * 16;

        switch(retrievalMode)
        {
            case ORIGINAL: return new Biome(oldBiomes.get(index));
            case LAST_SWAPPED: return new Biome(swappedBiomes ? biomeBuffer1.get(index) : biomeBuffer2.get(index));
            case CURRENT: return new Biome(swappedBiomes ? biomeBuffer2.get(index) : biomeBuffer1.get(index));
        }
        return null;
    }
    public RegistryEntry<net.minecraft.world.biome.Biome> getBiomeRaw(int biomeX, int biomeY, int biomeZ, RetrievalMode retrievalMode)
    {
        int index = biomeZ + biomeY * 4 + biomeX * 16;
        return switch (retrievalMode)
                {
                    case ORIGINAL -> oldBiomes.get(index);
                    case LAST_SWAPPED -> swappedBiomes ? biomeBuffer1.get(index) : biomeBuffer2.get(index);
                    case CURRENT -> swappedBiomes ? biomeBuffer2.get(index) : biomeBuffer1.get(index);
                };
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
    //endregion
    //region Content Setters
    public void setBlock(int x, int y, int z, BlockType blockType)
    {
        markDirty();
        if (swappedBlocks)
        {
            tileEntityBuffer2.remove(new BlockPos(x, y, z));

            x -= chunkX * 16;
            y -= chunkY * 16;
            z -= chunkZ * 16;

            blockStateBuffer2.set(z + y * 16 + x * 256, blockType.getMinecraftBlock());
        }
        else
        {
            tileEntityBuffer1.remove(new BlockPos(x, y, z));

            x -= chunkX * 16;
            y -= chunkY * 16;
            z -= chunkZ * 16;

            blockStateBuffer1.set(z + y * 16 + x * 256, blockType.getMinecraftBlock());
        }
    }
    public void setBlock(int x, int y, int z, Block block)
    {
        markDirty();
        BlockPos pos = new BlockPos(x, y, z);
        if (swappedBlocks)
        {
            if (block.tileEntity() == null) tileEntityBuffer2.remove(pos);
            else tileEntityBuffer2.put(pos, block.tileEntity());

            x -= chunkX * 16;
            y -= chunkY * 16;
            z -= chunkZ * 16;

            blockStateBuffer2.set(z + y * 16 + x * 256, block.blockType().getMinecraftBlock());
        }
        else
        {
            if (block.tileEntity() == null) tileEntityBuffer1.remove(pos);
            else tileEntityBuffer1.put(pos, block.tileEntity());

            x -= chunkX * 16;
            y -= chunkY * 16;
            z -= chunkZ * 16;

            blockStateBuffer1.set(z + y * 16 + x * 256, block.blockType().getMinecraftBlock());
        }
    }
    public void setBiome(int x, int y, int z, Biome biome)
    {
        markDirty();
        x -= chunkX * 16;
        y -= chunkY * 16;
        z -= chunkZ * 16;

        int biomeX = BiomeCoords.fromBlock(x);
        int biomeY = BiomeCoords.fromBlock(y);
        int biomeZ = BiomeCoords.fromBlock(z);
        int index = biomeZ + biomeY * 4 + biomeX * 16;

        if (swappedBiomes) biomeBuffer2.set(index, biome.getMinecraftBiome());
        else biomeBuffer1.set(index, biome.getMinecraftBiome());
        biomesChanged = true;
    }
    public void commitEntityChanges(Entity entity)
    {
        markDirty();
        if (swappedEntities) entityBuffer2.put(entity.keystoneUUID(), entity.duplicate());
        else entityBuffer1.put(entity.keystoneUUID(), entity.duplicate());
        allEntities.put(entity.keystoneUUID(), entity.duplicate());
    }
    //endregion
    //region Swapping
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
    public void swapBiomeBuffers(boolean copy)
    {
        swappedBiomes = !swappedBiomes;
        if (copy)
        {
            if (swappedBiomes) this.biomeBuffer2 = this.biomeBuffer1.copy();
            else this.biomeBuffer1 = this.biomeBuffer2.copy();
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
            for (Map.Entry<UUID, Entity> entry : entitySource.entrySet()) entityDestination.put(entry.getKey(), entry.getValue().duplicate());
        }
    }
    //endregion
    //region Change Applications
    public void markDirty()
    {
        historyEntry.dirtyChunk(this);
    }
    public void revertBlocks()
    {
        apply(oldBlockStates, oldTileEntities, oldBiomes, oldEntities);
    }
    public void placeBlocks()
    {
        PalettedArray<BlockState> blockStates = swappedBlocks ? blockStateBuffer2 : blockStateBuffer1;
        Map<BlockPos, NBTCompound> tileEntities = swappedEntities ? tileEntityBuffer2 : tileEntityBuffer1;
        PalettedArray<RegistryEntry<net.minecraft.world.biome.Biome>> biomes = swappedBiomes ? biomeBuffer2 : biomeBuffer1;
        Map<UUID, Entity> entities = swappedEntities ? entityBuffer2 : entityBuffer1;
        apply(blockStates, tileEntities, biomes, entities);
    }
    public void processUpdates(boolean undoing)
    {
        PalettedArray<BlockState> newBlocks = undoing ? oldBlockStates : (swappedBlocks ? blockStateBuffer2 : blockStateBuffer1);
        
        BlockPos start = new BlockPos(chunkX << 4, chunkY << 4, chunkZ << 4);
        int index = 0;
        for (int x = 0; x < 16; x++)
        {
            for (int y = 0; y < 16; y++)
            {
                for (int z = 0; z < 16; z++)
                {
                    BlockState newState = newBlocks.get(index);
                    BlockPos pos = start.add(x, y, z);
                    world.updateNeighbors(pos, newState.getBlock());
                    newState.updateNeighbors(world, pos, UPDATE_FLAGS);
                    index++;
                }
            }
        }
    }
    //endregion
    //region Private Helpers
    private void apply(PalettedArray<BlockState> blockStates, Map<BlockPos, NBTCompound> tileEntities, PalettedArray<RegistryEntry<net.minecraft.world.biome.Biome>> biomes, Map<UUID, Entity> entities)
    {
        // Apply Biomes
        var biomeContainer = createBiomeContainer(biomes);
        ((ChunkSectionAccessor)chunkSection).setBiomeStorage(biomeContainer);
        if (MinecraftClient.getInstance().world.getDimension().equals(world.getDimension()))
        {
            ClientWorld world = MinecraftClient.getInstance().world;
            Chunk chunk = world.getChunk(chunkX, chunkZ);
            ChunkSection chunkSection = world.getChunk(chunkX, chunkZ).getSection(chunk.getSectionIndex(chunkY << 4));
            ((ChunkSectionAccessor)chunkSection).setBiomeStorage(biomeContainer.copy());
            KeystoneGlobalState.DirtyChunks.computeIfAbsent(this.world.toServerWorld(), key -> Lists.newArrayList()).add(chunk);
        }
        
        // Apply Blocks
        KeystoneGlobalState.BlockTickScheduling = true;
        BlockPos start = new BlockPos(chunkX << 4, chunkY << 4, chunkZ << 4);
        for (int x = 0; x < 16; x++)
        {
            for (int y = 0; y < 16; y++)
            {
                for (int z = 0; z < 16; z++)
                {
                    BlockPos pos = start.add(x, y, z);
                    BlockState state = blockStates.get(z + y * 16 + x * 256);
    
                    world.toServerWorld().setBlockState(pos, state, PLACE_FLAGS);
                    
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
        KeystoneGlobalState.BlockTickScheduling = false;
    
        // Apply Entities
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
            else entities.get(entityID).updateMinecraftEntity(world);
        }
    
        chunk.setNeedsSaving(true);
    }
    private <T> PalettedArray<T> copyContainer(PalettedContainer<T> container, int sizeX, int sizeY, int sizeZ)
    {
        PalettedArray<T> ret = new PalettedArray<>(sizeX * sizeY * sizeZ, 1, null);
        for (int x = 0; x < sizeX; x++)
        {
            for (int y = 0; y < sizeY; y++)
            {
                for (int z = 0; z < sizeZ; z++)
                {
                    int index = z + y * sizeZ + x * sizeZ * sizeY;
                    ret.set(index, container.get(x, y, z));
                }
            }
        }
        return ret;
    }
    private PalettedContainer<RegistryEntry<net.minecraft.world.biome.Biome>> createBiomeContainer(PalettedArray<RegistryEntry<net.minecraft.world.biome.Biome>> array)
    {
        Registry<net.minecraft.world.biome.Biome> biomeRegistry = WorldRegistries.biomes(world.toServerWorld());
        PalettedContainer<RegistryEntry<net.minecraft.world.biome.Biome>> container = new PalettedContainer<>(biomeRegistry.getIndexedEntries(), biomeRegistry.entryOf(BiomeKeys.THE_VOID), PalettedContainer.PaletteProvider.BIOME);
        
        for (int x = 0; x < 4; x++)
        {
            for (int y = 0; y < 4; y++)
            {
                for (int z = 0; z < 4; z++)
                {
                    int index = z + y * 4 + x * 16;
                    container.swap(x, y, z, array.get(index));
                }
            }
        }
        
        return container;
    }
    //endregion
}