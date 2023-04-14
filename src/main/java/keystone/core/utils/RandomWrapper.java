package keystone.core.utils;

import keystone.api.Keystone;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSplitter;

public class RandomWrapper implements Random
{
    public static final RandomWrapper INSTANCE = new RandomWrapper();
    
    @Override public Random split() { return new CheckedRandom(this.nextLong()); }
    @Override public RandomSplitter nextSplitter() { return new CheckedRandom.Splitter(this.nextLong()); }
    
    @Override public void setSeed(long seed) { Keystone.RANDOM.setSeed(seed); }
    @Override public int nextInt() { return Keystone.RANDOM.nextInt(); }
    @Override public int nextInt(int bound) { return Keystone.RANDOM.nextInt(bound); }
    @Override public long nextLong() { return Keystone.RANDOM.nextLong(); }
    @Override public boolean nextBoolean() { return Keystone.RANDOM.nextBoolean(); }
    @Override public float nextFloat() { return Keystone.RANDOM.nextFloat(); }
    @Override public double nextDouble() { return Keystone.RANDOM.nextDouble(); }
    @Override public double nextGaussian() { return Keystone.RANDOM.nextGaussian(); }
}
