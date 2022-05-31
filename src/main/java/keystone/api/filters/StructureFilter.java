package keystone.api.filters;

import keystone.api.DiscSampler;
import keystone.api.Keystone;
import keystone.api.WorldRegion;
import keystone.api.variables.Variable;
import keystone.api.wrappers.coordinates.Vector2f;
import keystone.core.utils.ProgressBar;

import java.util.List;
import java.util.Map;
import net.minecraft.util.math.random.Random;

public abstract class StructureFilter extends KeystoneFilter
{
    @Variable public int seed = 0;
    public Random random;

    private Map<WorldRegion, List<Vector2f>> coordinates;

    public abstract int getStructureSeparation();
    public int getStructureSteps() { return 1; }
    public void processStructure(Vector2f coordinate, WorldRegion region) {}
    public void postProcessStructures(WorldRegion region) {}

    @Override public boolean allowBlocksOutsideRegion() { return true; }
    @Override public void initialize()
    {
        this.random = seed == 0 ? Keystone.RANDOM : Random.create(seed);
    }

    @Override
    public void prepareRegion(WorldRegion region)
    {
        List<Vector2f> samples = DiscSampler.sample2D(random, getStructureSeparation(), region.min.x, region.min.z, region.size.x, region.size.z);
        coordinates.put(region, samples);
    }
    @Override
    public int getRegionSteps(WorldRegion region)
    {
        List<Vector2f> regionCoordinates = coordinates.get(region);
        return regionCoordinates.size() * getStructureSteps();
    }
    @Override
    public void processRegion(WorldRegion region)
    {
        List<Vector2f> regionCoordinates = coordinates.get(region);
        for (Vector2f coordinate : regionCoordinates)
        {
            if (isCanceled()) break;

            processStructure(coordinate, region);
            ProgressBar.nextStep();
        }
        postProcessStructures(region);
    }
}
