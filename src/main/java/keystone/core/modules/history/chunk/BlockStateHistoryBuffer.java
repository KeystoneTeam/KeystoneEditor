package keystone.core.modules.history.chunk;

import com.mojang.serialization.Codec;
import keystone.api.Keystone;
import keystone.core.utils.PalettedArray;
import keystone.core.utils.PalettedContainerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;

public class BlockStateHistoryBuffer extends HistoryBuffer<PalettedContainer<BlockState>, NbtElement>
{
    private static final Codec<PalettedContainer<BlockState>> CODEC = PalettedContainer.createPalettedContainerCodec(Block.STATE_IDS, BlockState.CODEC, PalettedContainer.PaletteProvider.BLOCK_STATE, Blocks.AIR.getDefaultState());
    
    private BlockStateHistoryBuffer(PalettedContainer<BlockState> old, PalettedContainer<BlockState> buffer1, PalettedContainer<BlockState> buffer2)
    {
        super(old, buffer1, buffer2);
    }
    
    public static BlockStateHistoryBuffer createEmpty()
    {
        return new BlockStateHistoryBuffer(null, null, null);
    }
    public static BlockStateHistoryBuffer createFromChunkSection(ChunkSection section)
    {
        if (section.isEmpty()) return createFilled(Blocks.AIR.getDefaultState());
        
        PalettedContainer<BlockState> old = PalettedContainerUtils.copyContainer(section.getBlockStateContainer());
        PalettedContainer<BlockState> buffer1 = PalettedContainerUtils.copyContainer(section.getBlockStateContainer());
        PalettedContainer<BlockState> buffer2 = PalettedContainerUtils.copyContainer(section.getBlockStateContainer());
        return new BlockStateHistoryBuffer(old, buffer1, buffer2);
    }
    public static BlockStateHistoryBuffer createFilled(BlockState fill)
    {
        PalettedContainer<BlockState> old = new PalettedContainer<>(Block.STATE_IDS, fill, PalettedContainer.PaletteProvider.BLOCK_STATE);
        PalettedContainer<BlockState> buffer1 = new PalettedContainer<>(Block.STATE_IDS, fill, PalettedContainer.PaletteProvider.BLOCK_STATE);
        PalettedContainer<BlockState> buffer2 = new PalettedContainer<>(Block.STATE_IDS, fill, PalettedContainer.PaletteProvider.BLOCK_STATE);
        return new BlockStateHistoryBuffer(old, buffer1, buffer2);
    }
    
    @Override
    protected NbtElement writeBuffer(PalettedContainer<BlockState> buffer)
    {
        return CODEC.encodeStart(NbtOps.INSTANCE, buffer).getOrThrow();
    }
    @Override
    protected PalettedContainer<BlockState> readBuffer(NbtElement nbt)
    {
        return CODEC.parse(NbtOps.INSTANCE, nbt).promotePartial(error -> Keystone.LOGGER.warn("Recoverable error while reading block state history buffer: {}", error)).getOrThrow();
    }
    @Override
    protected PalettedContainer<BlockState> copyBuffer(PalettedContainer<BlockState> buffer)
    {
        return PalettedContainerUtils.copyContainer(buffer);
    }
}
