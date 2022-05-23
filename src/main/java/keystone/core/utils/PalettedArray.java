package keystone.core.utils;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class PalettedArray<T>
{
    private static final int binaryTypeBitCount = 8;

    private byte[] binary;
    private List<T> palette;
    private byte bits;
    private int size;

    private PalettedArray() {}
    public PalettedArray(int size, int startingBits, T startingContent)
    {
        palette = new ArrayList<>();
        if (startingContent != null) palette.add(startingContent);

        this.bits = (byte)startingBits;
        this.size = size;

        this.binary = new byte[(int)Math.ceil(size / (double) binaryTypeBitCount * startingBits)];
    }
    public PalettedArray(NbtCompound nbt, Function<NbtElement, T> paletteElementDeserializer)
    {
        palette = new ArrayList<>();
        for (NbtElement nbtElement : (NbtList)nbt.get("Palette")) palette.add(paletteElementDeserializer.apply(nbtElement));

        binary = nbt.getByteArray("Binary");
        bits = nbt.getByte("Bits");
        size = nbt.getInt("Size");
    }

    public PalettedArray<T> copy()
    {
        PalettedArray<T> copy = new PalettedArray<>();

        copy.binary = Arrays.copyOf(binary, binary.length);
        copy.palette = new ArrayList<>(palette.size());
        copy.palette.addAll(palette);
        copy.bits = bits;
        copy.size = size;

        return copy;
    }

    public NbtCompound serialize(Function<T, NbtElement> paletteElementSerializer)
    {
        NbtList paletteList = new NbtList();
        for (T paletteElement : palette) paletteList.add(paletteElementSerializer.apply(paletteElement));

        NbtCompound ret = new NbtCompound();
        ret.put("Palette", paletteList);
        ret.putByteArray("Binary", binary);
        ret.putByte("Bits", bits);
        ret.putInt("Size", size);

        return ret;
    }

    public T get(int index)
    {
        if (index < 0 || index >= size) throw new ArrayIndexOutOfBoundsException("Index " + index + " out of bounds for length " + size);
        return palette.get(readBinary(binary, index * bits, bits));
    }
    public void set(int index, T value)
    {
        if (index < 0 || index >= size) throw new ArrayIndexOutOfBoundsException("Index " + index + " out of bounds for length " + size);
        int paletteIndex = getPaletteIndex(value);
        writeBinary(binary, index * bits, bits, paletteIndex);
    }
    public int size() { return size; }

    private int getPaletteIndex(T value)
    {
        int index = palette.indexOf(value);
        if (index < 0)
        {
            index = palette.size();
            palette.add(value);
            if (index >= 1 << bits) increaseBits();
        }
        return index;
    }
    private void increaseBits()
    {
        byte newBits = (byte)(bits + 1);
        byte[] newBinary = new byte[(int)Math.ceil(size / (double) binaryTypeBitCount * newBits)];

        for (int i = 0; i < size; i++)
        {
            int value = readBinary(binary, i * bits, bits);
            writeBinary(newBinary, i * newBits, newBits, value);
        }

        bits = newBits;
        binary = newBinary;
    }

    private static byte setBits(byte value, byte bits) { return (byte)(value | bits); }
    private static byte unsetBits(byte value, byte bits) { return (byte)(value & ~bits); }

    private static int readBinary(byte[] binary, int startingBit, int length)
    {
        int ret = 0;
        for (int i = 0, bitIndex = startingBit; i < length; i++, bitIndex++)
        {
            byte chunk = binary[bitIndex / binaryTypeBitCount];
            byte bitValue = (byte)(chunk & (1 << (bitIndex % binaryTypeBitCount)));
            if (bitValue != 0) ret += (1 << i);
        }
        return ret;
    }
    private static void writeBinary(byte[] binary, int startingBit, int length, int value)
    {
        for (int i = 0, bitIndex = startingBit; i < length; i++, bitIndex++)
        {
            int binaryIndex = bitIndex / binaryTypeBitCount;
            byte bitValue = (byte)(1 << (bitIndex % binaryTypeBitCount));

            boolean set = (value & (1 << i)) > 0;
            if (set) binary[binaryIndex] = setBits(binary[binaryIndex], bitValue);
            else binary[binaryIndex] = unsetBits(binary[binaryIndex], bitValue);
        }
    }
}
