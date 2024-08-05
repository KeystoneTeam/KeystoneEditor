package keystone.core.modules.history.chunk;

import keystone.api.wrappers.nbt.NBTCompound;
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

public class TileEntityHistoryBuffer extends HistoryBuffer<ConcurrentHashMap<BlockPos, NBTCompound>, NbtList>
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
            ret.old.put(pos, new NBTCompound(nbt.copy()));
            ret.buffer1.put(pos, new NBTCompound(nbt.copy()));
            ret.buffer2.put(pos, new NBTCompound(nbt.copy()));
        }
        
        return ret;
    }
    public static TileEntityHistoryBuffer createEmpty() { return new TileEntityHistoryBuffer(); }
    
    @Override
    protected NbtList writeBuffer(ConcurrentHashMap<BlockPos, NBTCompound> buffer)
    {
        return NBTSerializer.serializeTileEntities(buffer);
    }
    
    @Override
    protected ConcurrentHashMap<BlockPos, NBTCompound> readBuffer(NbtList nbt)
    {
        return new ConcurrentHashMap<>(NBTSerializer.deserializeTileEntities(nbt));
    }
    
    @Override
    protected ConcurrentHashMap<BlockPos, NBTCompound> copyBuffer(ConcurrentHashMap<BlockPos, NBTCompound> buffer)
    {
        ConcurrentHashMap<BlockPos, NBTCompound> copy = new ConcurrentHashMap<>(buffer.size());
        for (Map.Entry<BlockPos, NBTCompound> entry : buffer.entrySet()) copy.put(entry.getKey(), entry.getValue().clone());
        return copy;
    }
}
