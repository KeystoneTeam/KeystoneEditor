package keystone.core.modules.history.chunk;

import keystone.core.utils.NBTSerializer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TileEntityHistoryBuffer extends HistoryBuffer<ConcurrentHashMap<BlockPos, NbtCompound>, NbtList>
{
    private TileEntityHistoryBuffer()
    {
        super(new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
    }
    
    public static TileEntityHistoryBuffer createFromSection(World world, Chunk chunk, int sectionY)
    {
        TileEntityHistoryBuffer ret = new TileEntityHistoryBuffer();
        
        Set<BlockPos> tileEntityPositions = chunk.getBlockEntityPositions();
        for (BlockPos pos : tileEntityPositions)
        {
            if (ChunkSectionPos.getSectionCoord(pos.getY()) != sectionY) continue;
            BlockEntity tileEntity = chunk.getBlockEntity(pos);
            NbtCompound nbt = tileEntity.createNbtWithIdentifyingData(world.getRegistryManager());
            ret.old.put(pos, nbt.copy());
            ret.buffer1.put(pos, nbt.copy());
            ret.buffer2.put(pos, nbt.copy());
        }
        
        return ret;
    }
    public static TileEntityHistoryBuffer createEmpty() { return new TileEntityHistoryBuffer(); }
    
    @Override
    protected NbtList writeBuffer(ConcurrentHashMap<BlockPos, NbtCompound> buffer)
    {
        return NBTSerializer.serializeTileEntities(buffer);
    }
    
    @Override
    protected ConcurrentHashMap<BlockPos, NbtCompound> readBuffer(NbtList nbt)
    {
        return new ConcurrentHashMap<>(NBTSerializer.deserializeTileEntities(nbt));
    }
    
    @Override
    protected ConcurrentHashMap<BlockPos, NbtCompound> copyBuffer(ConcurrentHashMap<BlockPos, NbtCompound> buffer)
    {
        ConcurrentHashMap<BlockPos, NbtCompound> copy = new ConcurrentHashMap<>(buffer.size());
        for (Map.Entry<BlockPos, NbtCompound> entry : buffer.entrySet()) copy.put(entry.getKey(), entry.getValue().copy());
        return copy;
    }
}
