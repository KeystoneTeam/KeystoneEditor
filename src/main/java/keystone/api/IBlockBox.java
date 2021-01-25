package keystone.api;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.function.Consumer;

public interface IBlockBox
{
    void forEachBlock(Consumer<BlockPos> consumer);
    BlockState getBlock(BlockPos pos, boolean getOriginalState);
    boolean setBlock(BlockPos pos, BlockState block);
}
