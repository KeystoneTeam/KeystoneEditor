package keystone.api.utils;

import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.util.math.random.Random;

public class PerlinNoise
{
    private final PerlinNoiseSampler sampler;
    
    public PerlinNoise()
    {
        this.sampler = new PerlinNoiseSampler(Random.create());
    }
    public PerlinNoise(long seed)
    {
        this.sampler = new PerlinNoiseSampler(Random.create(seed));
    }
    
    public double sample(double x, double y) { return sample(x, y, 0); }
    public double sample(double x, double y, double z) { return 0.5 * (sampler.sample(x, y, 0) + 1); }
    
    public double sample(double x, double y, double wavelength, double persistence, double lacunarity, int octaves) { return sample(x, y, 0, wavelength, persistence, lacunarity, octaves); }
    public double sample(double x, double y, double z, double wavelength, double persistence, double lacunarity, int octaves)
    {
        double amplitude = 1.0;
        double frequency = 1.0 / wavelength;
        double noise = 0;
        
        while (octaves > 0)
        {
            noise += amplitude * sample(x * frequency, y * frequency, z * frequency);
            frequency *= lacunarity;
            amplitude *= persistence;
            octaves--;
        }
        
        return noise / Math.pow(1 + persistence, octaves - 1);
    }
}
