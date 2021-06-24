package keystone.core.renderer.blocks.world;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.ITickList;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;

public class WrappedChunk implements IChunk
{
    final PlacementSimulationWorld world;
    boolean needsLight;
    final int x;
    final int z;
    final ChunkPos pos;

    private final ChunkSection[] sections;

    public WrappedChunk(PlacementSimulationWorld world, int x, int z)
    {
        this.world = world;
        // TODO: Check if this needs to be false
        this.needsLight = true;
        this.x = x;
        this.z = z;
        this.pos = new ChunkPos(x, z);

        this.sections = new ChunkSection[16];

        for (int i = 0; i < 16; i++)
        {
            sections[i] = new WrappedChunkSection(this, i << 4);
        }
    }

    @Override
    public Stream<BlockPos> getLights()
    {
        return world.blocksAdded
                .entrySet()
                .stream()
                .filter(it ->
                {
                    BlockPos blockPos = it.getKey();
                    boolean chunkContains = blockPos.getX() >> 4 == x && blockPos.getZ() >> 4 == z;
                    return chunkContains && it.getValue().getLightValue(world, blockPos) != 0;
                })
                .map(Map.Entry::getKey);
    }

    @Override
    public ITickList<Block> getBlockTicks()
    {
        return null;
    }

    @Override
    public ITickList<Fluid> getLiquidTicks()
    {
        return null;
    }

    @Override
    public ChunkSection[] getSections()
    {
        return sections;
    }

    @Override
    public Collection<Map.Entry<Heightmap.Type, Heightmap>> getHeightmaps()
    {
        return null;
    }

    @Override
    public ChunkStatus getStatus()
    {
        return ChunkStatus.LIGHT;
    }

    @Override
    public void removeBlockEntity(BlockPos p_177425_1_)
    {

    }

    @Nullable
    @Override
    public BlockState setBlockState(BlockPos p_177436_1_, BlockState p_177436_2_, boolean p_177436_3_)
    {
        return null;
    }

    @Override
    public void setBlockEntity(BlockPos p_177426_1_, TileEntity p_177426_2_)
    {

    }

    @Override
    public void addEntity(Entity p_76612_1_)
    {

    }

    @Override
    public Set<BlockPos> getBlockEntitiesPos()
    {
        return null;
    }

    @Override
    public void setHeightmap(Heightmap.Type p_201607_1_, long[] p_201607_2_)
    {

    }

    @Override
    public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Type p_217303_1_)
    {
        return null;
    }

    @Override
    public int getHeight(Heightmap.Type p_201576_1_, int p_201576_2_, int p_201576_3_)
    {
        return 0;
    }

    @Override
    public ChunkPos getPos()
    {
        return pos;
    }

    @Override
    public void setLastSaveTime(long p_177432_1_)
    {

    }

    @Override
    public Map<Structure<?>, StructureStart<?>> getAllStarts()
    {
        return null;
    }

    @Override
    public void setAllStarts(Map<Structure<?>, StructureStart<?>> p_201612_1_)
    {

    }

    @Nullable
    @Override
    public BiomeContainer getBiomes()
    {
        return null;
    }

    @Override
    public void setUnsaved(boolean p_177427_1_)
    {

    }

    @Override
    public boolean isUnsaved()
    {
        return false;
    }

    @Override
    public ShortList[] getPostProcessing()
    {
        return new ShortList[0];
    }

    @Nullable
    @Override
    public CompoundNBT getBlockEntityNbt(BlockPos p_201579_1_)
    {
        return null;
    }

    @Nullable
    @Override
    public CompoundNBT getBlockEntityNbtForSaving(BlockPos p_223134_1_)
    {
        return null;
    }

    @Override
    public UpgradeData getUpgradeData()
    {
        return null;
    }

    @Override
    public void setInhabitedTime(long p_177415_1_)
    {

    }

    @Override
    public long getInhabitedTime()
    {
        return 0;
    }

    @Override
    public boolean isLightCorrect()
    {
        return needsLight;
    }

    @Override
    public void setLightCorrect(boolean needsLight)
    {
        this.needsLight = needsLight;
    }

    @Nullable
    @Override
    public TileEntity getBlockEntity(BlockPos pos)
    {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos)
    {
        return world.getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos p_204610_1_)
    {
        return null;
    }

    @Nullable
    @Override
    public StructureStart<?> getStartForFeature(Structure<?> p_230342_1_)
    {
        return null;
    }

    @Override
    public void setStartForFeature(Structure<?> p_230344_1_, StructureStart<?> p_230344_2_)
    {

    }

    @Override
    public LongSet getReferencesForFeature(Structure<?> p_230346_1_)
    {
        return null;
    }

    @Override
    public void addReferenceForFeature(Structure<?> p_230343_1_, long p_230343_2_)
    {

    }

    @Override
    public Map<Structure<?>, LongSet> getAllReferences()
    {
        return null;
    }

    @Override
    public void setAllReferences(Map<Structure<?>, LongSet> p_201606_1_)
    {

    }
}