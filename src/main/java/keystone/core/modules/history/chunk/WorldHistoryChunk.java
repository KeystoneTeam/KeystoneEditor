package keystone.core.modules.history.chunk;

import keystone.api.Keystone;
import keystone.api.enums.RetrievalMode;
import keystone.api.wrappers.entities.Entity;
import keystone.core.KeystoneGlobalState;
import keystone.core.mixins.common.ChunkSectionAccessor;
import keystone.core.modules.history.HistoryStackFrame;
import keystone.core.modules.world.change_queue.DirtyChunkList;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.utils.PalettedArray;
import keystone.core.utils.PalettedContainerUtils;
import keystone.core.utils.RegistryLookups;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.FillBiomeCommand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.WorldChunk;
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
    private final ServerWorld world;
    private final WorldChunk chunk;
    private final ChunkSection chunkSection;

    private final BlockStateHistoryBuffer blocks;
    private final TileEntityHistoryBuffer tileEntities;
    private final BiomeHistoryBuffer biomes;
    private final EntitiesHistoryBuffer entities;
    
    private boolean biomesChanged;

    public WorldHistoryChunk(HistoryStackFrame historyEntry, Vec3i chunkPosition, @NotNull ServerWorld world)
    {
        // Initialize Chunk Coordinates
        this.chunkX = chunkPosition.getX();
        this.chunkY = chunkPosition.getY();
        this.chunkZ = chunkPosition.getZ();

        // Initialize Metadata
        this.historyEntry = historyEntry;
        this.world = world;
        this.chunk = world.getChunk(chunkX, chunkZ);
        
        // Get Section Info
        ChunkSection[] sections = this.chunk.getSectionArray();
        int sectionIndex = chunk.getSectionIndex(chunkY << 4);
        
        // If Section is outside world
        if (sectionIndex < 0 || sectionIndex >= sections.length)
        {
            this.chunkSection = null;
            this.blocks = BlockStateHistoryBuffer.createFilled(Blocks.VOID_AIR.getDefaultState());
            this.tileEntities = TileEntityHistoryBuffer.createEmpty();
            this.biomes = BiomeHistoryBuffer.createFilled(world.toServerWorld(), BiomeKeys.THE_VOID);
            this.entities = EntitiesHistoryBuffer.createFromSection(world.toServerWorld(), chunkPosition);
        }
        
        // If Section is inside world
        else
        {
            this.chunkSection = sections[sectionIndex];
            this.blocks = BlockStateHistoryBuffer.createFromChunkSection(this.chunkSection);
            this.tileEntities = TileEntityHistoryBuffer.createFromSection(world.toServerWorld(), this.chunk, this.chunkY);
            this.biomes = BiomeHistoryBuffer.createFromChunkSection(world.toServerWorld(), this.chunkSection);
            this.entities = EntitiesHistoryBuffer.createEmpty();
        }
    }
    
    //region NBT Serialization
    public WorldHistoryChunk(HistoryStackFrame historyEntry, NbtCompound nbt)
    {
        // Chunk Position
        WorldCacheModule worldCache = Keystone.getModule(WorldCacheModule.class);
        int[] chunkPos = nbt.getIntArray("ChunkPos");
        chunkX = chunkPos[0];
        chunkY = chunkPos[1];
        chunkZ = chunkPos[2];
    
        // Metadata
        this.historyEntry = historyEntry;
        world = worldCache.getDimensionWorld(WorldCacheModule.getDimensionKey(Identifier.of(nbt.getString("World"))));
        chunk = world.getChunk(chunkX, chunkZ);
        
        // Section Info
        ChunkSection[] sections = chunk.getSectionArray();
        int sectionIndex = chunk.getSectionIndex(chunkY << 4);
        
        // Chunk Section
        if (sectionIndex < 0 || sectionIndex >= sections.length) this.chunkSection = null;
        else this.chunkSection = sections[sectionIndex];
        
        // Blocks
        if (nbt.contains("Blocks", NbtElement.COMPOUND_TYPE)) this.blocks = HistoryBuffer.deserialize(world.toServerWorld(), nbt.getCompound("Blocks"), BlockStateHistoryBuffer::createEmpty);
        else this.blocks = BlockStateHistoryBuffer.createFilled(Blocks.AIR.getDefaultState());
        
        // Tile Entities
        if (nbt.contains("TileEntities", NbtElement.COMPOUND_TYPE)) this.tileEntities = HistoryBuffer.deserialize(world.toServerWorld(), nbt.getCompound("TileEntities"), TileEntityHistoryBuffer::createEmpty);
        else this.tileEntities = TileEntityHistoryBuffer.createEmpty();
        
        // Biomes
        if (nbt.contains("Biomes", NbtElement.COMPOUND_TYPE)) this.biomes = HistoryBuffer.deserialize(world.toServerWorld(), nbt.getCompound("Biomes"), () -> BiomeHistoryBuffer.createEmpty(world.toServerWorld()));
        else this.biomes = BiomeHistoryBuffer.createFilled(world.toServerWorld(), BiomeKeys.THE_VOID);
        this.biomesChanged = nbt.getBoolean("BiomesChanged");

        // Entities
        if (nbt.contains("Entities", NbtElement.COMPOUND_TYPE)) this.entities = HistoryBuffer.deserialize(world.toServerWorld(), nbt.getCompound("Entities"), EntitiesHistoryBuffer::createEmpty);
        else this.entities = EntitiesHistoryBuffer.createEmpty();
    }
    public NbtCompound serialize()
    {
        NbtCompound nbt = new NbtCompound();
        
        nbt.putIntArray("ChunkPos", new int[] { chunkX, chunkY, chunkZ });
        nbt.putString("World", world.toServerWorld().getRegistryKey().getValue().toString());
        
        nbt.put("Blocks", blocks.write(world.toServerWorld()));
        nbt.put("TileEntities", tileEntities.write(world.toServerWorld()));
        nbt.put("Biomes", biomes.write(world.toServerWorld()));
        nbt.put("Entities", entities.write(world.toServerWorld()));
        nbt.putBoolean("BiomesChanged", biomesChanged);
        
        return nbt;
    }
    //endregion

    public RegistryKey<World> getRegistryKey() { return world.toServerWorld().getRegistryKey(); }
    public boolean isBiomesChanged() { return biomesChanged; }

    //region Content Getters
    public BlockState getBlockState(int x, int y, int z, RetrievalMode retrievalMode)
    {
        x -= chunkX * 16;
        y -= chunkY * 16;
        z -= chunkZ * 16;
        return this.blocks.getBuffer(retrievalMode).get(x, y, z);
    }
    public NbtCompound getBlockData(int x, int y, int z, RetrievalMode retrievalMode)
    {
        BlockPos pos = new BlockPos(x, y, z);
        NbtCompound tileEntity = this.tileEntities.getBuffer(retrievalMode).getOrDefault(pos, null);
        if (tileEntity != null) return tileEntity.copy();
        else return null;
    }
    public RegistryEntry<Biome> getBiome(int x, int y, int z, RetrievalMode retrievalMode)
    {
        x -= chunkX * 16;
        y -= chunkY * 16;
        z -= chunkZ * 16;

        int biomeX = BiomeCoords.fromBlock(x);
        int biomeY = BiomeCoords.fromBlock(y);
        int biomeZ = BiomeCoords.fromBlock(z);

        return this.biomes.getBuffer(retrievalMode).get(biomeX, biomeY, biomeZ);
    }
    public RegistryEntry<net.minecraft.world.biome.Biome> getBiomeRaw(int biomeX, int biomeY, int biomeZ, RetrievalMode retrievalMode)
    {
        return this.biomes.getBuffer(retrievalMode).get(biomeX, biomeY, biomeZ);
    }
    public Entity getEntity(UUID keystoneUUID, RetrievalMode retrievalMode)
    {
        Entity entity = this.entities.getBuffer(retrievalMode).get(keystoneUUID);
        if (entity == null) return this.entities.old.get(keystoneUUID);
        else return entity;
    }
    public int getEntities(List<Entity> buffer, Box boundingBox, RetrievalMode retrievalMode)
    {
        Map<UUID, Entity> retrievalBuffer = this.entities.getBuffer(retrievalMode);

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
    public void setBlockState(int x, int y, int z, BlockState blockState)
    {
        markDirty();
        
        x -= chunkX * 16;
        y -= chunkY * 16;
        z -= chunkZ * 16;
        
        this.blocks.getBuffer(RetrievalMode.CURRENT).set(x, y, z, blockState);
        this.tileEntities.getBuffer(RetrievalMode.CURRENT).remove(new BlockPos(x, y, z));
    }
    public void setBlockData(int x, int y, int z, NbtCompound blockData)
    {
        markDirty();
        BlockPos pos = new BlockPos(x, y, z);
        
        if (blockData != null) this.tileEntities.getBuffer(RetrievalMode.CURRENT).put(pos, blockData);
        else this.tileEntities.getBuffer(RetrievalMode.CURRENT).remove(pos);
    }
    public void setBiome(int x, int y, int z, RegistryEntry<Biome> biome)
    {
        markDirty();
        x -= chunkX * 16;
        y -= chunkY * 16;
        z -= chunkZ * 16;

        int biomeX = BiomeCoords.fromBlock(x);
        int biomeY = BiomeCoords.fromBlock(y);
        int biomeZ = BiomeCoords.fromBlock(z);

        this.biomes.getBuffer(RetrievalMode.CURRENT).set(biomeX, biomeY, biomeZ, biome);
        biomesChanged = true;
    }
    public void commitEntityChanges(Entity entity)
    {
        markDirty();
        this.entities.getBuffer(RetrievalMode.CURRENT).put(entity.keystoneUUID(), entity.duplicate());
        this.entities.allEntities.put(entity.keystoneUUID(), entity.duplicate());
    }
    //endregion
    //region Swapping
    public void swapBlockBuffers(boolean copy)
    {
        this.blocks.swap(copy);
        this.tileEntities.swap(copy);
    }
    public void swapBiomeBuffers(boolean copy)
    {
        this.biomes.swap(copy);
    }
    public void swapEntityBuffers(boolean copy)
    {
        this.entities.swap(copy);
    }
    //endregion
    //region Change Applications
    public void markDirty()
    {
        if (chunkSection == null) return;
        historyEntry.dirtyChunk(this);
    }
    public void revertBlocks(Map<ServerWorld, DirtyChunkList> dirtyChunks)
    {
        if (chunkSection == null) return;
        apply(RetrievalMode.ORIGINAL, dirtyChunks);
    }
    public void place(Map<ServerWorld, DirtyChunkList> dirtyChunks)
    {
        if (chunkSection == null) return;
        apply(RetrievalMode.CURRENT, dirtyChunks);
    }
    public void processUpdates(boolean undoing)
    {
        if (chunkSection == null) return;
        PalettedContainer<BlockState> newBlocks = this.blocks.getBuffer(undoing ? RetrievalMode.ORIGINAL : RetrievalMode.CURRENT);
        
        BlockPos start = new BlockPos(chunkX << 4, chunkY << 4, chunkZ << 4);
        for (int x = 0; x < 16; x++)
        {
            for (int y = 0; y < 16; y++)
            {
                for (int z = 0; z < 16; z++)
                {
                    BlockState newState = newBlocks.get(x, y, z);
                    BlockPos pos = start.add(x, y, z);
                    world.updateNeighbors(pos, newState.getBlock());
                    newState.updateNeighbors(world, pos, UPDATE_FLAGS);
                }
            }
        }
    }
    //endregion
    //region Private Helpers
    private void apply(RetrievalMode retrievalMode, Map<ServerWorld, DirtyChunkList> dirtyChunks)
    {
        if (chunkSection == null) return;
        ClientWorld clientWorld = MinecraftClient.getInstance().world;
        
        BlockPos chunkOrigin = new BlockPos(chunkX << 4, chunkY << 4, chunkZ << 4);
        PalettedContainer<BlockState> blockStates = this.blocks.getBuffer(retrievalMode);
        ConcurrentHashMap<BlockPos, NbtCompound> tileEntities = this.tileEntities.getBuffer(retrievalMode);
        PalettedContainer<RegistryEntry<net.minecraft.world.biome.Biome>> biomes = this.biomes.getBuffer(retrievalMode);
        ConcurrentHashMap<UUID, Entity> entities = this.entities.getBuffer(retrievalMode);
        
        // Apply Biomes
        chunkSection.populateBiomes((x, y, z, noise) -> biomes.get(x, y, z), world.getChunkManager().getNoiseConfig().getMultiNoiseSampler(), 0, 0, 0);
        
        // Apply Blocks
        ((ChunkSectionAccessor) chunkSection).setBlockStateContainer(blockStates);
        chunkSection.calculateCounts();
        
        /*
        KeystoneGlobalState.BlockTickScheduling = true;
        for (int x = 0; x < 16; x++)
        {
            for (int y = 0; y < 16; y++)
            {
                for (int z = 0; z < 16; z++)
                {
                    BlockPos pos = chunkOrigin.add(x, y, z);
                    chunk.setBlockState(pos, blockStates.get(x, y, z), false);
                    world.updateListeners(pos, blocks.getBuffer(RetrievalMode.ORIGINAL).get(x, y, z), blockStates.get(x, y, z), PLACE_FLAGS);
                    NbtCompound tileEntityData = tileEntities.getOrDefault(pos, null);
                    if (tileEntityData != null)
                    {
                        BlockEntity blockEntity = world.getBlockEntity(pos);
                        if (blockEntity != null) blockEntity.read(tileEntityData, RegistryLookups.registryLookup());
                    }
                }
            }
        }
        KeystoneGlobalState.BlockTickScheduling = false;
         */
        
        /*
        for (int x = 0; x < 16; x++)
        {
            for (int y = 0; y < 16; y++)
            {
                for (int z = 0; z < 16; z++)
                {
                    BlockPos pos = chunkOrigin.add(x, y, z);
                    BlockState state = blockStates.get(x, y, z);
                    BlockState existing = world.getBlockState(pos);
                    
                    if (!state.equals(existing))
                    {
                        KeystoneGlobalState.BlockTickScheduling = true;
                        world.toServerWorld().setBlockState(pos, state, PLACE_FLAGS);
                        KeystoneGlobalState.BlockTickScheduling = false;
                    }
                    
                    NbtCompound blockData = tileEntities.getOrDefault(pos, null);
                    if (blockData != null)
                    {
                        NbtCompound tileEntityData = blockData.copy();
                        tileEntityData.putInt("x", pos.getX());
                        tileEntityData.putInt("y", pos.getY());
                        tileEntityData.putInt("z", pos.getZ());
                        BlockEntity tileEntity = world.getBlockEntity(pos);
                        if (tileEntity != null) tileEntity.read(tileEntityData, RegistryLookups.registryLookup());
                    }
                }
            }
        }
         */
        
        // Apply Tile Entities
        for (BlockPos pos : chunk.getBlockEntityPositions())
        {
            int sectionY = ChunkSectionPos.getSectionCoord(pos.getY());
            if (sectionY == chunkY) chunk.removeBlockEntity(pos);
        }
        for (Map.Entry<BlockPos, NbtCompound> entry : tileEntities.entrySet())
        {
            BlockPos pos = entry.getKey();
            NbtCompound blockData = entry.getValue();
            BlockEntity blockEntity = BlockEntity.createFromNbt(pos, blockStates.get(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15), blockData, world.getRegistryManager());
            if (blockEntity != null) chunk.addBlockEntity(blockEntity);
            else Keystone.LOGGER.error("Failed to create block entity at {}!", pos);
        }
    
        // Apply Entities
        for (Map.Entry<UUID, Entity> entry : this.entities.allEntities.entrySet())
        {
            UUID keystoneID = entry.getKey();
            if (!entities.containsKey(keystoneID))
            {
                Entity entity = entry.getValue();
                net.minecraft.entity.Entity mcEntity = world.toServerWorld().getEntity(entity.minecraftUUID());
                if (mcEntity != null)
                {
                    mcEntity.discard();
                    entity.breakMinecraftEntityConnection();
                }
            }
            else entities.get(keystoneID).updateMinecraftEntity(world);
        }
    
        // Mark Chunk Dirty
        chunk.setNeedsSaving(true);
        dirtyChunks.computeIfAbsent(world, k -> new DirtyChunkList()).dirtyChunkSection(chunk, chunkSection, chunkY);
    }
    //endregion
}