package keystone.core.renderer.blocks.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.map.MapState;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.annotation.MethodsReturnNonnullByDefault;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.QueryableTickScheduler;

import java.util.Collections;
import java.util.List;

@MethodsReturnNonnullByDefault
public class WrappedWorld extends World
{
    protected World world;
    protected ChunkManager chunkManager;
    protected DummyEntityLookup<Entity> entityLookup = new DummyEntityLookup<>();

    public WrappedWorld(World world)
    {
        super((MutableWorldProperties) world.getLevelProperties(), world.getRegistryKey(), world.getDimensionEntry(), world::getProfiler, world.isClient, world.isDebugWorld(), 0, -1);
        this.world = world;
    }

    public void setChunkManager(ChunkManager chunkManager)
    {
        this.chunkManager = chunkManager;
    }
    public World getWorld()
    {
        return this.world;
    }

    @Override protected EntityLookup<Entity> getEntityLookup() { return this.entityLookup; }
    @Override public ChunkManager getChunkManager() { return chunkManager != null ? chunkManager : world.getChunkManager(); }
    @Override
    public boolean spawnEntity(Entity entity)
    {
        entity.world = world;
        return world.spawnEntity(entity);
    }

    @Override public DynamicRegistryManager getRegistryManager() { return world.getRegistryManager(); }
    @Override public RegistryEntry<Biome> getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) { return world.getGeneratorStoredBiome(biomeX, biomeY, biomeZ); }
    @Override public void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags) { world.updateListeners(pos, oldState, newState, flags); }
    @Override public float getBrightness(Direction direction, boolean shaded) { return world.getBrightness(direction, shaded); }
    @Override public QueryableTickScheduler<Block> getBlockTickScheduler() { return world.getBlockTickScheduler(); }
    @Override public QueryableTickScheduler<Fluid> getFluidTickScheduler() { return world.getFluidTickScheduler(); }
    @Override public Scoreboard getScoreboard() { return world.getScoreboard(); }
    @Override public int getNextMapId() { return world.getNextMapId(); }
    @Override public LightingProvider getLightingProvider() { return world.getLightingProvider(); }
    @Override public BlockState getBlockState(BlockPos pos)
    {
        return world.getBlockState(pos);
    }

    @Override public RecipeManager getRecipeManager() { return world.getRecipeManager(); }
    @Override public String asString() { return world.asString(); }

    @Override public List<? extends PlayerEntity> getPlayers() { return Collections.emptyList(); }
    @Override public void syncWorldEvent(@org.jetbrains.annotations.Nullable PlayerEntity player, int eventId, BlockPos pos, int data) { }

    @Override public void playSound(@org.jetbrains.annotations.Nullable PlayerEntity except, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) { }
    @Override public void playSound(@org.jetbrains.annotations.Nullable PlayerEntity except, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, long seed) { }
    @Override public void playSoundFromEntity(@org.jetbrains.annotations.Nullable PlayerEntity except, Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch) { }
    @Override public void playSoundFromEntity(@org.jetbrains.annotations.Nullable PlayerEntity except, Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch, long seed) { }
    @Override public void emitGameEvent(@org.jetbrains.annotations.Nullable Entity entity, GameEvent event, BlockPos pos) { }
    @Override public void emitGameEvent(GameEvent event, Vec3d emitterPos, GameEvent.Emitter emitter) { }
    @Override @org.jetbrains.annotations.Nullable public MapState getMapState(String id) { return null; }
    @Override public void putMapState(String id, MapState state) { }
    @Override @org.jetbrains.annotations.Nullable public Entity getEntityById(int id) { return null; }
    @Override public void setBlockBreakingInfo(int entityId, BlockPos pos, int progress) { }
    @Override public int getMaxLightLevel() { return 15; }

    @Override
    public int getTopY()
    {
        return this.getBottomY() + this.getHeight();
    }

    @Override
    public int countVerticalSections()
    {
        return this.getTopSectionCoord() - this.getBottomSectionCoord();
    }

    @Override
    public int getBottomSectionCoord()
    {
        return ChunkSectionPos.getSectionCoord(this.getBottomY());
    }

    @Override
    public int getTopSectionCoord()
    {
        return ChunkSectionPos.getSectionCoord(this.getTopY() - 1) + 1;
    }

    @Override
    public boolean isOutOfHeightLimit(BlockPos pos)
    {
        return this.isOutOfHeightLimit(pos.getY());
    }

    @Override
    public boolean isOutOfHeightLimit(int y)
    {
        return y < this.getBottomY() || y >= this.getTopY();
    }

    @Override
    public int getSectionIndex(int y)
    {
        return this.sectionCoordToIndex(ChunkSectionPos.getSectionCoord(y));
    }

    @Override
    public int sectionCoordToIndex(int coord)
    {
        return coord - this.getBottomSectionCoord();
    }

    @Override
    public int sectionIndexToCoord(int index)
    {
        return index + this.getBottomSectionCoord();
    }
}