package keystone.core.utils;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;

public class Tests
{
    public static void main(String[] args)
    {
        testPalettedArray();
    }

    private static void testPalettedArray()
    {
        PalettedArray<String> test = new PalettedArray<>(40, 1, "NULL");
        for (int i = 0; i < test.size(); i += 8)
        {
            test.set(i + 0, "NULL");
            test.set(i + 1, "Test1");
            test.set(i + 2, "Test2");
            test.set(i + 3, "Test3");
            test.set(i + 4, "Test4");
            test.set(i + 5, "Test5");
            test.set(i + 6, "Test6");
            test.set(i + 7, "Test7");
        }

        NbtCompound nbt = test.serialize(NbtString::of);
        test = new PalettedArray<>(nbt, NbtElement::asString);

        for (int i = 0; i < test.size(); i++) System.out.println(i + ": " + test.get(i));
    }
}
