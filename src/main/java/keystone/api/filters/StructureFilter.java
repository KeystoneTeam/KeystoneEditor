package keystone.api.filters;

import it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap;
import keystone.api.Keystone;
import keystone.api.WorldRegion;
import keystone.api.utils.PositionSampler;
import keystone.api.variables.Variable;
import keystone.api.wrappers.coordinates.Vector2f;
import keystone.core.utils.ProgressBar;

import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class StructureFilter extends KeystoneFilter
{
    @Variable public int seed = 0;
    public Random random;

    private Map<WorldRegion, List<Vector2f>> coordinates;

    public abstract int getStructureSeparation();
    public int getStructureSteps() { return 1; }
    public void processStructure(Vector2f coordinate, WorldRegion region) {}
    public void postProcessStructures(WorldRegion region) {}

    @Override public void initialize()
    {
        this.random = seed == 0 ? Keystone.RANDOM : new Random(seed);
        this.coordinates = new Object2ReferenceArrayMap<>();
    }

    @Override
    public void prepareRegion(WorldRegion region)
    {
        List<Vector2f> samples = PositionSampler.sample2D(random, getStructureSeparation(), region.min.x, region.min.z, region.size.x, region.size.z);
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
            if (isCancelled()) break;

            processStructure(coordinate, region);
            ProgressBar.nextStep();
        }
        postProcessStructures(region);
    }
}
