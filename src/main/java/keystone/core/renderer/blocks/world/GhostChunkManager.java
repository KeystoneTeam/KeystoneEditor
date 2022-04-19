package keystone.core.renderer.blocks.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.map.MapState;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.EmptyTickSchedulers;
import net.minecraft.world.tick.QueryableTickScheduler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class GhostChunkManager extends ChunkManager
{
    private final World fallbackWorld;

    public GhostChunkManager(World world)
    {
        fallbackWorld = world;
    }



    //@Nullable
    //@Override
    //public BlockGetter getChunkForLighting(int x, int z)
    //{
    //    return getChunk(x, z);
    //}

    @Override
    public World getWorld()
    {
        return fallbackWorld;
    }
    
    @Nullable
    @Override
    public WorldChunk getChunk(int x, int z, ChunkStatus status, boolean p_212849_4_)
    {
        return getChunk(x, z);
    }

    public WorldChunk getChunk(int x, int z)
    {
        return new EmptierChunk(fallbackWorld.getRegistryManager());
    }

    @Override
    public String getDebugString()
    {
        return "WrappedChunkProvider";
    }

    @Override
    public LightingProvider getLightingProvider()
    {
        return fallbackWorld.getLightingProvider();
    }

    @Override
    public void tick(BooleanSupplier p_202162_, boolean p_202163_) {}


    @Override
    public int getLoadedChunkCount()
    {
        return 0;
    }

    public static class EmptierChunk extends WorldChunk
    {
        private static final class DummyWorld extends World
        {
            private DynamicRegistryManager access;

            private DummyWorld(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> registryEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed)
            {
                super(properties, registryRef, registryEntry, profiler, isClient, debugWorld, seed);
            }

            public World withAccess(DynamicRegistryManager access)
            {
                this.access = access;
                return this;
            }

            @Override
            public ChunkManager getChunkManager()
            {
                return null;
            }

            @Override
            public void syncWorldEvent(PlayerEntity pPlayer, int pType, BlockPos pPos, int pData) {}

            @Override
            public void emitGameEvent(Entity pEntity, GameEvent pEvent, BlockPos pPos) {}

            @Override
            public DynamicRegistryManager getRegistryManager()
            {
                return access;
            }

            @Override
            public List<? extends PlayerEntity> getPlayers()
            {
                return null;
            }

            @Override
            public RegistryEntry<Biome> getGeneratorStoredBiome(int pX, int pY, int pZ)
            {
                return null;
            }

            @Override
            public float getBrightness(Direction pDirection, boolean pShade)
            {
                return 0;
            }

            @Override
            public QueryableTickScheduler<Block> getBlockTickScheduler() { return EmptyTickSchedulers.getClientTickScheduler(); }

            @Override
            public QueryableTickScheduler<Fluid> getFluidTickScheduler() { return EmptyTickSchedulers.getClientTickScheduler(); }

            @Override public void updateListeners(BlockPos pos, BlockState oldState, BlockState newState, int flags) { }
            @Override public void playSound(@org.jetbrains.annotations.Nullable PlayerEntity except, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) { }
            @Override public void playSoundFromEntity(@org.jetbrains.annotations.Nullable PlayerEntity except, Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch) { }
            @Override public String asString() { return null; }
            @org.jetbrains.annotations.Nullable @Override public Entity getEntityById(int id) { return null; }
            @org.jetbrains.annotations.Nullable @Override public MapState getMapState(String id) { return null; }
            @Override public void putMapState(String id, MapState state) { }
            @Override public int getNextMapId() { return 0; }
            @Override public void setBlockBreakingInfo(int entityId, BlockPos pos, int progress) { }
            @Override public Scoreboard getScoreboard() { return null; }
            @Override public RecipeManager getRecipeManager() { return null; }
            @Override protected EntityLookup<Entity> getEntityLookup() { return null; }
        }

        private static final DummyWorld DUMMY_LEVEL = new DummyWorld(null, null, DynamicRegistryManager.BUILTIN.get()
                .get(Registry.DIMENSION_TYPE_KEY)
                .getEntry(DimensionType.OVERWORLD_REGISTRY_KEY).get(), null, false, false, 0);

        public EmptierChunk(DynamicRegistryManager registryAccess)
        {
            super(DUMMY_LEVEL.withAccess(registryAccess), null);
        }

        public BlockState getBlockState(BlockPos p_180495_1_)
        {
            return Blocks.VOID_AIR.getDefaultState();
        }

        @Nullable
        public BlockState setBlockState(BlockPos p_177436_1_, BlockState p_177436_2_, boolean p_177436_3_)
        {
            return null;
        }

        public FluidState getFluidState(BlockPos p_204610_1_)
        {
            return Fluids.EMPTY.getDefaultState();
        }

        public int getLightEmission(BlockPos p_217298_1_)
        {
            return 0;
        }

        //@Nullable
        //public BlockEntity getBlockEntity(BlockPos p_177424_1_, EntityCreationType p_177424_2_)
        //{
        //    return null;
        //}

        public void addAndRegisterBlockEntity(BlockEntity p_150813_1_) {}

        public void setBlockEntity(BlockEntity p_177426_2_) {}

        public void removeBlockEntity(BlockPos p_177425_1_) {}

        public void markUnsaved() {}

        public boolean isEmpty()
        {
            return true;
        }

        public boolean isYSpaceEmpty(int p_76606_1_, int p_76606_2_)
        {
            return true;
        }

        //public ChunkHolder.FullChunkStatus getFullStatus()
        //{
        //    return ChunkHolder.FullChunkStatus.BORDER;
        //}
    }
}