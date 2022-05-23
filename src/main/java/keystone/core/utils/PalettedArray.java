package keystone.core.utils;

import java.util.ArrayList;
import java.util.List;

public class PalettedArray<T>
{
    private static final int binaryTypeBitCount = 8;

    private byte[] binary;
    private List<T> palette;
    private int bits;
    private int size;

    public PalettedArray(int size, int startingBits, T startingContent)
    {
        palette = new ArrayList<>();
        palette.add(startingContent);

        this.bits = startingBits;
        this.size = size;

        this.binary = new byte[(int)Math.ceil(size / (double) binaryTypeBitCount * startingBits)];
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
        int newBits = bits + 1;
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
