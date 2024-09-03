package keystone.core.renderer.blocks.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.map.MapState;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.annotation.MethodsReturnNonnullByDefault;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.QueryableTickScheduler;
import net.minecraft.world.tick.TickManager;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@MethodsReturnNonnullByDefault
public class WrappedClientWorld extends World
{
    protected ChunkManager chunkManager;
    protected DummyEntityLookup<Entity> entityLookup = new DummyEntityLookup<>();

    public WrappedClientWorld()
    {
        super(
                MinecraftClient.getInstance().world.getLevelProperties(),
                MinecraftClient.getInstance().world.getRegistryKey(),
                MinecraftClient.getInstance().world.getRegistryManager(),
                MinecraftClient.getInstance().world.getDimensionEntry(),
                MinecraftClient.getInstance().world.getProfilerSupplier(),
                MinecraftClient.getInstance().world.isClient,
                MinecraftClient.getInstance().world.isDebugWorld(), 0, -1
        );
    }

    public void setChunkManager(ChunkManager chunkManager)
    {
        this.chunkManager = chunkManager;
    }

    @Override protected EntityLookup<Entity> getEntityLookup() { return this.entityLookup; }
    @Override public ChunkManager getChunkManager() { return chunkManager != null ? chunkManager : MinecraftClient.getInstance().world.getChunkManager(); }
    @Override
    public boolean spawnEntity(Entity entity)
    {
        return MinecraftClient.getInstance().world.spawnEntity(entity);
    }

    @Override public DynamicRegistryManager getRegistryManager() { return MinecraftClient.getInstance().world.getRegistryManager(); }
    @Override public BrewingRecipeRegistry getBrewingRecipeRegistry() { return MinecraftClient.getInstance().world.getBrewingRecipeRegistry(); }
    @Override public FeatureSet getEnabledFeatures() { return MinecraftClient.getInstance().world.getEnabledFeatures(); }
    
    @Override public RegistryEntry<Biome> getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) { return MinecraftClient.getInstance().world.getGeneratorStoredBiome(biomeX, biomeY, biomeZ); }
    @Override public void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags) { MinecraftClient.getInstance().world.updateListeners(pos, oldState, newState, flags); }
    @Override public float getBrightness(Direction direction, boolean shaded) { return MinecraftClient.getInstance().world.getBrightness(direction, shaded); }
    @Override public QueryableTickScheduler<Block> getBlockTickScheduler() { return MinecraftClient.getInstance().world.getBlockTickScheduler(); }
    @Override public QueryableTickScheduler<Fluid> getFluidTickScheduler() { return MinecraftClient.getInstance().world.getFluidTickScheduler(); }
    @Override public Scoreboard getScoreboard() { return MinecraftClient.getInstance().world.getScoreboard(); }
    @Override public LightingProvider getLightingProvider() { return MinecraftClient.getInstance().world.getLightingProvider(); }
    @Override public BlockState getBlockState(BlockPos pos) { return MinecraftClient.getInstance().world.getBlockState(pos); }
    
    @Override public RecipeManager getRecipeManager() { return MinecraftClient.getInstance().world.getRecipeManager(); }
    @Override public String asString() { return MinecraftClient.getInstance().world.asString(); }

    @Override public List<? extends PlayerEntity> getPlayers() { return Collections.emptyList(); }
    @Override public void syncWorldEvent(@org.jetbrains.annotations.Nullable PlayerEntity player, int eventId, BlockPos pos, int data) { }
    @Override public void emitGameEvent(RegistryEntry<GameEvent> event, Vec3d emitterPos, GameEvent.Emitter emitter) { }
    
    @Override public void playSound(@org.jetbrains.annotations.Nullable PlayerEntity except, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) { }
    @Override public void playSound(@org.jetbrains.annotations.Nullable PlayerEntity except, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, long seed) { }
    @Override public void playSound(@Nullable PlayerEntity except, double x, double y, double z, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed) { }
    @Override public void playSoundFromEntity(@Nullable PlayerEntity except, Entity entity, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed) { }
    
    @Override public void playSoundFromEntity(@org.jetbrains.annotations.Nullable PlayerEntity except, Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch) { }
    @Override @org.jetbrains.annotations.Nullable public Entity getEntityById(int id) { return null; }
    @Override public TickManager getTickManager() { return MinecraftClient.getInstance().world.getTickManager(); }
    
    @Override public MapIdComponent increaseAndGetMapId() { return MinecraftClient.getInstance().world.increaseAndGetMapId(); }
    @Nullable @Override public MapState getMapState(MapIdComponent id) { return null; }
    @Override public void putMapState(MapIdComponent id, MapState state) { }
    
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