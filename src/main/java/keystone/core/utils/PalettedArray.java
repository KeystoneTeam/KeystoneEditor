package keystone.core.utils;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class PalettedArray<T>
{
    private static class Binary
    {
        private final BitSet bitSet = new BitSet();

        public Binary() { }
        public Binary(int[] serialized) { deserialize(serialized); }

        public synchronized int[] serialize(int bitLength)
        {
            int[] ret = new int[(int)Math.ceil(bitLength / 32.0)];
            for (int i = 0; i < ret.length; i++)
            {
                int value = 0;
                for (int bit = 0; bit < 32; bit++)
                {
                    if (bitSet.get(i * 32 + bit)) value += 1 << bit;
                }
                ret[i] = value;
            }
            return ret;
        }
        public synchronized void deserialize(int[] bits)
        {
            bitSet.clear();
            for (int i = 0; i < bits.length; i++)
            {
                int value = bits[i];
                for (int bit = 0; bit < 32; bit++)
                {
                    if ((value & (1 << bit)) != 0) bitSet.set(i * 32 + bit);
                }
            }
        }

        public synchronized int read(int index, int length, int stride)
        {
            int ret = 0;
            for (int i = 0; i < length; i++)
            {
                int bitIndex = index + i * stride;
                if (bitSet.get(bitIndex)) ret += 1 << i;
            }
            return ret;
        }
        public synchronized void write(int value, int index, int length, int stride)
        {
            for (int i = 0; i < length; i++)
            {
                int bitIndex = index + i * stride;
                if ((value & (1 << i)) != 0) bitSet.set(bitIndex);
                else bitSet.clear(bitIndex);
            }
        }
    }

    private final Binary binary;
    private final List<T> palette;
    private final int size;
    private int bits;

    private PalettedArray(PalettedArray<T> copyFrom)
    {
        binary = new Binary(copyFrom.binary.serialize(copyFrom.size * copyFrom.bits));
        palette = Collections.synchronizedList(new ArrayList<>(copyFrom.palette.size()));
        palette.addAll(copyFrom.palette);
        size = copyFrom.size;
        bits = copyFrom.bits;
    }
    public PalettedArray(int size, int startingBits, T startingContent)
    {
        palette = Collections.synchronizedList(new ArrayList<>());
        if (startingContent != null) palette.add(startingContent);

        this.size = size;
        this.bits = startingBits;
        this.binary = new Binary();
    }
    public PalettedArray(NbtCompound nbt, Function<NbtElement, T> paletteElementDeserializer)
    {
        palette = Collections.synchronizedList(new ArrayList<>());
        for (NbtElement nbtElement : (NbtList)nbt.get("Palette")) palette.add(paletteElementDeserializer.apply(nbtElement));

        binary = new Binary(nbt.getIntArray("Binary"));
        size = nbt.getInt("Size");
        bits = nbt.getInt("Bits");
    }

    public PalettedArray<T> copy()
    {
        return new PalettedArray<>(this);
    }
    public NbtCompound serialize(Function<T, NbtElement> paletteElementSerializer)
    {
        NbtList paletteList = new NbtList();
        for (T paletteElement : palette) paletteList.add(paletteElementSerializer.apply(paletteElement));

        NbtCompound ret = new NbtCompound();
        ret.put("Palette", paletteList);
        ret.putIntArray("Binary", binary.serialize(size * bits));
        ret.putInt("Bits", bits);
        ret.putInt("Size", size);

        return ret;
    }

    public T get(int index)
    {
        if (index < 0 || index >= size) throw new ArrayIndexOutOfBoundsException("Index " + index + " out of bounds for length " + size);
        return palette.get(binary.read(index, bits, size));
    }
    public void set(int index, T value)
    {
        if (index < 0 || index >= size) throw new ArrayIndexOutOfBoundsException("Index " + index + " out of bounds for length " + size);
        int paletteIndex = getPaletteIndex(value);
        binary.write(paletteIndex, index, bits, size);
    }
    public int size() { return size; }

    private int getPaletteIndex(T value)
    {
        int index = palette.indexOf(value);
        if (index < 0)
        {
            index = palette.size();
            palette.add(value);
            if (index >= 1 << bits)
            {
                bits++;
                //binary.expand(size * (bits.get() - 1), size * bits.get() - 1);
            }
        }
        return index;
    }
}
