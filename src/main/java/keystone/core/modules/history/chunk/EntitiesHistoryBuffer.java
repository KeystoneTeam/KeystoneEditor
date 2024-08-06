package keystone.core.modules.history.chunk;

import keystone.api.wrappers.entities.Entity;
import keystone.core.utils.NBTSerializer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EntitiesHistoryBuffer extends HistoryBuffer<ConcurrentHashMap<UUID, Entity>, NbtCompound>
{
    protected ConcurrentHashMap<UUID, Entity> allEntities;
    
    private EntitiesHistoryBuffer()
    {
        super(new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
        this.allEntities = new ConcurrentHashMap<>();
    }
    
    public static EntitiesHistoryBuffer createFromSection(World world, Vec3i pos)
    {
        EntitiesHistoryBuffer ret = new EntitiesHistoryBuffer();
        
        int startX = pos.getX() << 4;
        int startY = pos.getY() << 4;
        int startZ = pos.getZ() << 4;
        Box bb = new Box(startX, startY, startZ, startX + 16, startY + 16, startZ + 16);
        List<net.minecraft.entity.Entity> mcEntities = world.getNonSpectatingEntities(net.minecraft.entity.Entity.class, bb);
        for (net.minecraft.entity.Entity mcEntity : mcEntities)
        {
            Entity entity = new Entity(mcEntity);
            ret.old.put(entity.keystoneUUID(), entity);
            ret.buffer1.put(entity.keystoneUUID(), entity.duplicate());
            ret.buffer2.put(entity.keystoneUUID(), entity.duplicate());
            ret.allEntities.put(entity.keystoneUUID(), entity.duplicate());
        }
        
        return ret;
    }
    public static EntitiesHistoryBuffer createEmpty() { return new EntitiesHistoryBuffer(); }
    
    @Override
    public NbtCompound write()
    {
        NbtCompound nbt = super.write();
        nbt.put("All", writeBuffer(allEntities));
        return nbt;
    }
    
    @Override
    public void read(NbtCompound nbt)
    {
        super.read(nbt);
        if (nbt.contains("All", NbtElement.COMPOUND_TYPE)) this.allEntities = readBuffer(nbt.getCompound("All"));
        else this.allEntities.clear();
    }
    
    @Override
    protected NbtCompound writeBuffer(ConcurrentHashMap<UUID, Entity> buffer)
    {
        return NBTSerializer.serializeEntities(buffer);
    }
    
    @Override
    protected ConcurrentHashMap<UUID, Entity> readBuffer(NbtCompound nbt)
    {
        return new ConcurrentHashMap<>(NBTSerializer.deserializeEntities(nbt));
    }
    
    @Override
    protected ConcurrentHashMap<UUID, Entity> copyBuffer(ConcurrentHashMap<UUID, Entity> buffer)
    {
        ConcurrentHashMap<UUID, Entity> copy = new ConcurrentHashMap<>(buffer.size());
        for (Map.Entry<UUID, Entity> entry : buffer.entrySet()) copy.put(entry.getKey(), entry.getValue().clone());
        return copy;
    }
}
