package keystone.core.renderer.blocks.world;

import keystone.api.Keystone;
import keystone.core.renderer.blocks.GhostWorldRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.LightType;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.tick.EmptyTickSchedulers;
import net.minecraft.world.tick.QueryableTickScheduler;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class GhostBlocksWorld extends WrappedWorld implements ServerWorldAccess
{
    protected Map<BlockPos, BlockState> blocks;
    protected Map<BlockPos, BlockEntity> tileEntities;
    protected List<BlockEntity> renderedTileEntities;
    protected List<Entity> entities;
    protected BlockBox bounds;
    protected GhostWorldRenderer renderer;
    protected BlockRotation rotation;
    protected BlockMirror mirror;

    public GhostBlocksWorld(World original, BlockRotation rotation, BlockMirror mirror)
    {
        super(original);
        setChunkManager(new GhostChunkManager(this));

        this.blocks = new HashMap<>();
        this.tileEntities = new HashMap<>();
        this.bounds = new BlockBox(BlockPos.ORIGIN);
        this.entities = new ArrayList<>();
        this.renderedTileEntities = new ArrayList<>();

        this.renderer = new GhostWorldRenderer();
        this.renderer.display(this);

        this.rotation = rotation;
        this.mirror = mirror;
    }

    public GhostWorldRenderer getRenderer () { return renderer; }
    public BlockRotation getRotation() { return rotation; }
    public BlockMirror getMirror() { return mirror; }
    public Set<BlockPos> getAllPositions()
    {
        return blocks.keySet();
    }

    public void setOrientation(BlockRotation rotation, BlockMirror mirror)
    {
        this.rotation = rotation;
        this.mirror = mirror;
    }

    public void clearAllContents()
    {
        this.blocks.clear();
        this.tileEntities.clear();
        this.bounds = new BlockBox(BlockPos.ORIGIN);
        this.entities.clear();
        this.renderedTileEntities.clear();
        this.rotation = BlockRotation.NONE;
        this.mirror = BlockMirror.NONE;
        this.renderer.markDirty();
    }

    @Override
    public boolean spawnEntity(Entity entityIn)
    {
        // CRITICAL TODO: See if this needs to be reimplemented
        //if (entityIn instanceof ItemFrameEntity) ((ItemFrameEntity) entityIn).getHeldItemStack().setNbt(null);
        //if (entityIn instanceof ArmorStandEntity armorStandEntity)
        //{
        //    armorStandEntity.getItemsEquipped()
        //            .forEach(stack -> stack.setNbt(null));
        //}

        this.renderer.markDirty();
        return entities.add(entityIn);
    }

    public Stream<Entity> getEntities()
    {
        return entities.stream();
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pos)
    {
        if (isOutOfHeightLimit(pos)) return null;
        if (tileEntities.containsKey(pos)) return tileEntities.get(pos);
        if (!blocks.containsKey(pos)) return null;

        BlockState blockState = getBlockState(pos);
        if (blockState.hasBlockEntity())
        {
            try
            {
                BlockEntity tileEntity = ((BlockEntityProvider)blockState.getBlock()).createBlockEntity(pos, blockState);
                if (tileEntity != null)
                {
                    onTEadded(tileEntity, pos);
                    tileEntities.put(pos, tileEntity);
                    renderedTileEntities.add(tileEntity);
                }
                return tileEntity;
            }
            catch (Exception e)
            {
                Keystone.LOGGER.debug("Could not create BlockEntity of block " + blockState + ": " + e);
            }
        }
        return null;
    }

    protected void onTEadded(BlockEntity tileEntity, BlockPos pos)
    {
        tileEntity.setWorld(this);
    }

    @Override
    public BlockState getBlockState(BlockPos globalPos)
    {
        if (getBounds().contains(globalPos) && blocks.containsKey(globalPos)) return blocks.get(globalPos);
        return Blocks.AIR.getDefaultState();
    }

    public Map<BlockPos, BlockState> getBlockMap()
    {
        return blocks;
    }

    @Override
    public FluidState getFluidState(BlockPos pos)
    {
        return getBlockState(pos).getFluidState();
    }
    
    @Override
    public void playSound(@Nullable PlayerEntity except, double x, double y, double z, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed)
    {
    
    }
    
    @Override
    public void playSoundFromEntity(@Nullable PlayerEntity except, Entity entity, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed)
    {
    
    }
    
    @Override
    public RegistryEntry<Biome> getBiome(BlockPos pos)
    {
        return world.getRegistryManager().get(RegistryKeys.BIOME).getEntry(BiomeKeys.THE_VOID).get();
    }
    
    @Override
    public FeatureSet getEnabledFeatures()
    {
        return world.getEnabledFeatures();
    }
    
    @Override
    public int getLightLevel(LightType p_226658_1_, BlockPos p_226658_2_)
    {
        return 10;
    }

    @Override
    public List<Entity> getOtherEntities(Entity arg0, Box arg1, Predicate<? super Entity> arg2)
    {
        return Collections.emptyList();
    }

    public <T extends Entity> List<T> getEntities(Class<? extends T> arg0, Box arg1,
                                                  Predicate<? super T> arg2)
    {
        return Collections.emptyList();
    }

    @Override
    public List<? extends PlayerEntity> getPlayers()
    {
        return Collections.emptyList();
    }

    @Override
    public int getAmbientDarkness()
    {
        return 0;
    }

    @Override
    public boolean testBlockState(BlockPos pos, Predicate<BlockState> predicate)
    {
        return predicate.test(getBlockState(pos));
    }

    @Override
    public boolean removeBlock(BlockPos arg0, boolean arg1)
    {
        return setBlockState(arg0, Blocks.AIR.getDefaultState(), 3);
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState state, int flags)
    {
        pos = pos.toImmutable();
        bounds.encompass(BlockBox.create(pos, pos));
        blocks.put(pos, state);
        if (tileEntities.containsKey(pos))
        {
            BlockEntity tileEntity = tileEntities.get(pos);
            if (!tileEntity.getType().supports(state))
            {
                tileEntities.remove(pos);
                renderedTileEntities.remove(tileEntity);
            }
        }

        BlockEntity tileEntity = getBlockEntity(pos);
        if (tileEntity != null)
            tileEntities.put(pos, tileEntity);

        this.renderer.markDirty();
        return true;
    }

    @Override
    public QueryableTickScheduler<Block> getBlockTickScheduler()
    {
        return EmptyTickSchedulers.getClientTickScheduler();
    }

    @Override
    public QueryableTickScheduler<Fluid> getFluidTickScheduler()
    {
        return EmptyTickSchedulers.getClientTickScheduler();
    }

    public BlockBox getBounds()
    {
        return bounds;
    }

    public Iterable<BlockEntity> getRenderedTileEntities()
    {
        return renderedTileEntities;
    }

    @Override
    public ServerWorld toServerWorld()
    {
        if (this.world instanceof ServerWorld) return (ServerWorld) this.world;
        throw new IllegalStateException("Cannot use ServerLevelAccess#toServerWorld in a client environment");
    }
}