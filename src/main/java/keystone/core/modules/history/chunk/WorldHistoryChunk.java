package keystone.core.modules.history.chunk;

import keystone.api.Keystone;
import keystone.api.enums.RetrievalMode;
import keystone.api.wrappers.Biome;
import keystone.api.wrappers.blocks.Block;
import keystone.api.wrappers.blocks.BlockType;
import keystone.api.wrappers.coordinates.BoundingBox;
import keystone.api.wrappers.entities.Entity;
import keystone.api.wrappers.nbt.NBTCompound;
import keystone.core.KeystoneGlobalState;
import keystone.core.mixins.common.ChunkSectionAccessor;
import keystone.core.modules.history.HistoryStackFrame;
import keystone.core.modules.world_cache.WorldCacheModule;
import keystone.core.registries.BlockTypeRegistry;
import keystone.core.utils.NBTSerializer;
import keystone.core.utils.PalettedArray;
import keystone.core.utils.RegistryLookups;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
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

    private final BlockStateHistoryBuffer blocks;
    private final TileEntityHistoryBuffer tileEntities;
    private final BiomeHistoryBuffer biomes;
    private final EntitiesHistoryBuffer entities;
    
    private boolean biomesChanged;

    public WorldHistoryChunk(HistoryStackFrame historyEntry, Vec3i chunkPosition, @NotNull ServerWorldAccess world)
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
            this.blocks = BlockStateHistoryBuffer.createFilled(world.toServerWorld(), Blocks.VOID_AIR.getDefaultState());
            this.tileEntities = TileEntityHistoryBuffer.createEmpty();
            this.biomes = BiomeHistoryBuffer.createFilled(world.toServerWorld(), BiomeKeys.THE_VOID);
            this.entities = EntitiesHistoryBuffer.createFromSection(world.toServerWorld(), chunkPosition);
        }
        
        // If Section is inside world
        else
        {
            this.chunkSection = sections[sectionIndex];
            this.blocks = BlockStateHistoryBuffer.createFromChunkSection(world.toServerWorld(), this.chunkSection);
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
        if (nbt.contains("Blocks", NbtElement.COMPOUND_TYPE)) this.blocks = HistoryBuffer.deserialize(nbt.getCompound("Blocks"), () -> BlockStateHistoryBuffer.createEmpty(world.toServerWorld()));
        else this.blocks = BlockStateHistoryBuffer.createFilled(world.toServerWorld(), Blocks.AIR.getDefaultState());
        
        // Tile Entities
        if (nbt.contains("TileEntities", NbtElement.COMPOUND_TYPE)) this.tileEntities = HistoryBuffer.deserialize(nbt.getCompound("TileEntities"), TileEntityHistoryBuffer::createEmpty);
        else this.tileEntities = TileEntityHistoryBuffer.createEmpty();
        
        // Biomes
        if (nbt.contains("Biomes", NbtElement.COMPOUND_TYPE)) this.biomes = HistoryBuffer.deserialize(nbt.getCompound("Biomes"), () -> BiomeHistoryBuffer.createEmpty(world.toServerWorld()));
        else this.biomes = BiomeHistoryBuffer.createFilled(world.toServerWorld(), BiomeKeys.THE_VOID);
        this.biomesChanged = nbt.getBoolean("BiomesChanged");

        // Entities
        if (nbt.contains("Entities", NbtElement.COMPOUND_TYPE)) this.entities = HistoryBuffer.deserialize(nbt.getCompound("Entities"), EntitiesHistoryBuffer::createEmpty);
        else this.entities = EntitiesHistoryBuffer.createEmpty();
    }
    public NbtCompound serialize()
    {
        NbtCompound nbt = new NbtCompound();
        
        nbt.putIntArray("ChunkPos", new int[] { chunkX, chunkY, chunkZ });
        nbt.putString("World", world.toServerWorld().getRegistryKey().getValue().toString());
        
        nbt.put("Blocks", blocks.write());
        nbt.put("TileEntities", tileEntities.write());
        nbt.put("Biomes", biomes.write());
        nbt.put("Entities", entities.write());
        nbt.putBoolean("BiomesChanged", biomesChanged);
        
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
        return BlockTypeRegistry.fromMinecraftBlock(this.blocks.getBuffer(retrievalMode).get(z + y * 16 + x * 256));
    }
    public NBTCompound getBlockEntity(int x, int y, int z, RetrievalMode retrievalMode)
    {
        BlockPos pos = new BlockPos(x, y, z);
        NBTCompound tileEntity = this.tileEntities.getBuffer(retrievalMode).getOrDefault(pos, null);
        if (tileEntity != null) return tileEntity.clone();
        else return null;
    }
    public Block getBlock(int x, int y, int z, RetrievalMode retrievalMode)
    {
        BlockPos pos = new BlockPos(x, y, z);
        
        x -= chunkX * 16;
        y -= chunkY * 16;
        z -= chunkZ * 16;
        
        BlockType blockType = BlockTypeRegistry.fromMinecraftBlock(this.blocks.getBuffer(retrievalMode).get(z + y * 16 + x * 256));
        NBTCompound tileEntity = this.tileEntities.getBuffer(retrievalMode).getOrDefault(pos, null);

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

        return new Biome(this.biomes.getBuffer(retrievalMode).get(biomeZ + biomeY * 4 + biomeX * 16));
    }
    public RegistryEntry<net.minecraft.world.biome.Biome> getBiomeRaw(int biomeX, int biomeY, int biomeZ, RetrievalMode retrievalMode)
    {
        return this.biomes.getBuffer(retrievalMode).get(biomeZ + biomeY * 4 + biomeX * 16);
    }
    public Entity getEntity(UUID keystoneUUID, RetrievalMode retrievalMode)
    {
        Entity entity = this.entities.getBuffer(retrievalMode).get(keystoneUUID);
        if (entity == null) return this.entities.old.get(keystoneUUID);
        else return entity;
    }
    public int getEntities(List<Entity> buffer, BoundingBox boundingBox, RetrievalMode retrievalMode)
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
    public void setBlock(int x, int y, int z, BlockType blockType)
    {
        markDirty();
        BlockPos pos = new BlockPos(x, y, z);
        
        x -= chunkX * 16;
        y -= chunkY * 16;
        z -= chunkZ * 16;
        
        this.blocks.getBuffer(RetrievalMode.CURRENT).set(z + y * 16 + x * 256, blockType.getMinecraftBlock());
        this.tileEntities.getBuffer(RetrievalMode.CURRENT).remove(new BlockPos(x, y, z));
    }
    public void setBlock(int x, int y, int z, Block block)
    {
        markDirty();
        BlockPos pos = new BlockPos(x, y, z);
        
        x -= chunkX * 16;
        y -= chunkY * 16;
        z -= chunkZ * 16;
        
        this.blocks.getBuffer(RetrievalMode.CURRENT).set(z + y * 16 + x * 256, block.blockType().getMinecraftBlock());
        if (block.tileEntity() != null) this.tileEntities.getBuffer(RetrievalMode.CURRENT).put(pos, block.tileEntity());
        else this.tileEntities.getBuffer(RetrievalMode.CURRENT).remove(pos);
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

        this.biomes.getBuffer(RetrievalMode.CURRENT).set(biomeZ + biomeY * 4 + biomeX * 16, biome.getMinecraftBiome());
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
    public void revertBlocks()
    {
        if (chunkSection == null) return;
        apply(RetrievalMode.ORIGINAL);
    }
    public void place()
    {
        if (chunkSection == null) return;
        apply(RetrievalMode.CURRENT);
    }
    public void processUpdates(boolean undoing)
    {
        if (chunkSection == null) return;
        PalettedArray<BlockState> newBlocks = this.blocks.getBuffer(undoing ? RetrievalMode.ORIGINAL : RetrievalMode.CURRENT);
        
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
    private void apply(RetrievalMode retrievalMode)
    {
        if (chunkSection == null) return;
        
        PalettedArray<BlockState> blockStates = this.blocks.getBuffer(retrievalMode);
        ConcurrentHashMap<BlockPos, NBTCompound> tileEntities = this.tileEntities.getBuffer(retrievalMode);
        PalettedArray<RegistryEntry<net.minecraft.world.biome.Biome>> biomes = this.biomes.getBuffer(retrievalMode);
        ConcurrentHashMap<UUID, Entity> entities = this.entities.getBuffer(retrievalMode);
        
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
        BlockPos start = new BlockPos(chunkX << 4, chunkY << 4, chunkZ << 4);
        for (int x = 0; x < 16; x++)
        {
            for (int y = 0; y < 16; y++)
            {
                for (int z = 0; z < 16; z++)
                {
                    BlockPos pos = start.add(x, y, z);
                    BlockState state = blockStates.get(z + y * 16 + x * 256);
                    BlockState existing = world.getBlockState(pos);
                    
                    if (!state.equals(existing))
                    {
                        KeystoneGlobalState.BlockTickScheduling = true;
                        world.toServerWorld().setBlockState(pos, state, PLACE_FLAGS);
                        KeystoneGlobalState.BlockTickScheduling = false;
                    }
                    
                    NBTCompound blockData = tileEntities.getOrDefault(pos, null);
                    if (blockData != null)
                    {
                        NbtCompound tileEntityData = blockData.getMinecraftNBT().copy();
                        tileEntityData.putInt("x", x);
                        tileEntityData.putInt("y", y);
                        tileEntityData.putInt("z", z);
                        BlockEntity tileEntity = world.getBlockEntity(pos);
                        if (tileEntity != null) tileEntity.read(tileEntityData, RegistryLookups.registryLookup());
                    }
                }
            }
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
    
        chunk.setNeedsSaving(true);
    }
    private PalettedContainer<RegistryEntry<net.minecraft.world.biome.Biome>> createBiomeContainer(PalettedArray<RegistryEntry<net.minecraft.world.biome.Biome>> array)
    {
        Registry<net.minecraft.world.biome.Biome> biomeRegistry = RegistryLookups.registry(RegistryKeys.BIOME);
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