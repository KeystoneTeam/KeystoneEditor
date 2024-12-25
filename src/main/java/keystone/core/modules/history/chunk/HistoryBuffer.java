package keystone.core.modules.history.chunk;

import keystone.api.enums.RetrievalMode;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.world.World;

public abstract class HistoryBuffer<BufferType, NbtType extends NbtElement>
{
    public interface HistoryBufferConstructor<BufferType, NbtType extends NbtElement, Subclass extends HistoryBuffer<BufferType, NbtType>>
    {
        Subclass construct();
    }
    
    BufferType old;
    BufferType buffer1;
    BufferType buffer2;
    boolean swapped;
    
    public HistoryBuffer(BufferType old, BufferType buffer1, BufferType buffer2)
    {
        this.old = old;
        this.buffer1 = buffer1;
        this.buffer2 = buffer2;
        this.swapped = false;
    }
    
    public static <BufferType, NbtType extends NbtElement, Subclass extends HistoryBuffer<BufferType, NbtType>> Subclass deserialize(World world, NbtCompound nbt, HistoryBufferConstructor<BufferType, NbtType, Subclass> constructor)
    {
        Subclass ret = constructor.construct();
        ret.read(world, nbt);
        return ret;
    }
    
    protected abstract NbtType writeBuffer(World world, BufferType buffer);
    protected abstract BufferType readBuffer(World world, NbtType nbt);
    protected abstract BufferType copyBuffer(BufferType buffer);
    
    public NbtCompound write(World world)
    {
        NbtCompound nbt = new NbtCompound();
        nbt.put("Old", writeBuffer(world, old));
        nbt.put("Buffer1", writeBuffer(world, buffer1));
        nbt.put("Buffer2", writeBuffer(world, buffer2));
        nbt.putBoolean("Swapped", swapped);
        return nbt;
    }
    public void read(World world, NbtCompound nbt)
    {
        old = readBuffer(world, (NbtType)nbt.get("Old"));
        buffer1 = readBuffer(world, (NbtType)nbt.get("Buffer1"));
        buffer2 = readBuffer(world, (NbtType)nbt.get("Buffer2"));
        swapped = nbt.getBoolean("Swapped");
    }
    
    public void swap(boolean copyContents)
    {
        swapped = !swapped;
        if (copyContents)
        {
            if (swapped) this.buffer2 = copyBuffer(buffer1);
            else this.buffer1 = copyBuffer(buffer2);
        }
    }
    
    public BufferType getBuffer(RetrievalMode retrievalMode)
    {
        return switch (retrievalMode)
        {
            case ORIGINAL -> old;
            case LAST_SWAPPED -> swapped ? buffer1 : buffer2;
            case CURRENT -> swapped ? buffer2 : buffer1;
        };
    }
    public boolean isSwapped() { return swapped; }
}
