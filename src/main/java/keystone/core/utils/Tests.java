package keystone.core.utils;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.math.Vec3i;

import java.util.stream.Stream;

public class Tests
{
    public static void main(String[] args) throws Exception
    {
        //testPalettedArray();
        //testPalettedArrayThreading();
    }

    private static void testPalettedArray()
    {
        int iterations = 5;

        String[] values = new String[8 * iterations];
        for (int i = 0; i < values.length; i += 8)
        {
            values[i + 0] =  "NULL";
            values[i + 1] =  "Test1";
            values[i + 2] =  "Test2";
            values[i + 3] =  "Test3";
            values[i + 4] =  "Test4";
            values[i + 5] =  "Test5";
            values[i + 6] =  "Test6";
            values[i + 7] =  "Test7";
        }

        PalettedArray<String> test = new PalettedArray<>(values.length, 1, values[0]);
        for (int i = 0; i < test.size(); i++)
        {
            test.set(i, values[i]);
        }

        // Set test
        boolean successful = true;
        for (int i = 0; i < values.length; i++)
        {
            if (!values[i].equals(test.get(i)))
            {
                System.err.println("Index " + i + " does not match! Expected " + values[i] + ", found " + test.get(i));
                successful = false;
            }
        }
        if (!successful) System.err.println("PalettedArray.set failed validation!");
        // Serialize Test
        else
        {
            System.out.println("PalettedArray.set met validation");

            NbtCompound nbt = test.serialize(NbtString::of);
            PalettedArray<String> deserialized = new PalettedArray<>(nbt, NbtElement::asString);

            for (int i = 0; i < values.length; i++)
            {
                if (!values[i].equals(deserialized.get(i)))
                {
                    System.err.println("Index " + i + " does not match! Expected " + test.get(i) + ", found " + deserialized.get(i));
                    successful = false;
                }
            }
            if (!successful) System.err.println("PalettedArray.serialize failed validation!");
            else System.out.println("PalettedArray.serialize met validation");
        }
    }
    private static void testPalettedArrayThreading()
    {
        // Create Value Array
        int iterations = 10000;
        int[] values = new int[4096 * iterations];
        for (int i = 0; i < values.length; i += 8)
        {
            values[i + 0] = 0;
            values[i + 1] = 1;
            values[i + 2] = 2;
            values[i + 3] = 3;
            values[i + 4] = 4;
            values[i + 5] = 5;
            values[i + 6] = 6;
            values[i + 7] = 7;
        }

        // Create Stream
        Stream.Builder<Vec3i> streamBuilder = Stream.builder();
        for (int i = 0; i < values.length; i++) streamBuilder.add(new Vec3i(i, values[i], 0));
        Stream<Vec3i> stream = streamBuilder.build();

        // Create PalettedArray
        PalettedArray<Integer> array = new PalettedArray<>(values.length, 1, values[0]);
        stream.parallel().forEach(vec -> array.set(vec.getX(), vec.getY()));

        // Perform Test
        boolean successful = true;
        for (int i = 0; i < values.length; i++)
        {
            if (values[i] != array.get(i))
            {
                System.err.println("Index " + i + " does not match! Expected " + values[i] + ", found " + array.get(i));
                successful = false;
            }
        }
        if (!successful) System.err.println("PalettedArray.threadSafe failed validation!");
        else System.out.println("PalettedArray.threadSafe met validation");
    }
}
