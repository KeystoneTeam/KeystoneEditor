package keystone.core.modules.history.chunk;

import keystone.core.utils.PalettedArray;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;

public class BlockStateHistoryBuffer extends HistoryBuffer<PalettedArray<BlockState>, NbtCompound>
{
    private final RegistryWrapper<Block> registry;
    
    private BlockStateHistoryBuffer(RegistryWrapper<Block> registry, PalettedArray<BlockState> old, PalettedArray<BlockState> buffer1, PalettedArray<BlockState> buffer2)
    {
        super(old, buffer1, buffer2);
        this.registry = registry;
    }
    
    public static BlockStateHistoryBuffer createEmpty(World world)
    {
        return new BlockStateHistoryBuffer(world.createCommandRegistryWrapper(RegistryKeys.BLOCK), null, null, null);
    }
    public static BlockStateHistoryBuffer createFromChunkSection(World world, ChunkSection section)
    {
        if (section.isEmpty()) return createFilled(world, Blocks.AIR.getDefaultState());
        
        PalettedArray<BlockState> old = PalettedArray.fromContainer(section.getBlockStateContainer(), 16, 16, 16);
        PalettedArray<BlockState> buffer1 = old.copy();
        PalettedArray<BlockState> buffer2 = old.copy();
        return new BlockStateHistoryBuffer(world.createCommandRegistryWrapper(RegistryKeys.BLOCK), old, buffer1, buffer2);
    }
    public static BlockStateHistoryBuffer createFilled(World world, BlockState fill)
    {
        PalettedArray<BlockState> old = new PalettedArray<>(4096, 1, fill);
        PalettedArray<BlockState> buffer1 = old.copy();
        PalettedArray<BlockState> buffer2 = old.copy();
        return new BlockStateHistoryBuffer(world.createCommandRegistryWrapper(RegistryKeys.BLOCK), old, buffer1, buffer2);
    }
    
    @Override
    protected NbtCompound writeBuffer(PalettedArray<BlockState> buffer)
    {
        return buffer.serialize(NbtHelper::fromBlockState);
    }
    @Override
    protected PalettedArray<BlockState> readBuffer(NbtCompound nbt)
    {
        return new PalettedArray<>(nbt, serialized -> NbtHelper.toBlockState(registry, (NbtCompound)serialized));
    }
    @Override
    protected PalettedArray<BlockState> copyBuffer(PalettedArray<BlockState> buffer)
    {
        return buffer.copy();
    }
}
