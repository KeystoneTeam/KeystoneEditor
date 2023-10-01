package keystone.core.modules.filter.execution;

import keystone.api.Keystone;
import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.wrappers.coordinates.BlockPos;
import keystone.api.wrappers.entities.Entity;
import keystone.core.modules.filter.FilterModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.utils.ProgressBar;

import java.util.*;
import java.util.stream.Stream;

public class MainFilterThread extends AbstractFilterThread
{
    private final HistoryModule historyModule = Keystone.getModule(HistoryModule.class);
    private final FilterModule filterModule = Keystone.getModule(FilterModule.class);

    private int iterations;
    private boolean ignoreRepeatEntities;

    private boolean processRegions;
    private boolean processBlocks;
    private boolean processEntities;

    private final Set<UUID> allEntities = new HashSet<>();
    private final Map<WorldRegion, Set<Entity>> entitiesByRegion = new HashMap<>();

    public MainFilterThread(FilterExecutor executor)
    {
        super(executor);

        Class<? extends KeystoneFilter> filterClass = filter.getClass();
        try
        {
            processRegions = filterClass.getMethod("processRegion", WorldRegion.class).getDeclaringClass().equals(filterClass);
            processBlocks = filterClass.getMethod("processBlock", int.class, int.class, int.class, WorldRegion.class).getDeclaringClass().equals(filterClass);
            processEntities = filterClass.getMethod("processEntity", Entity.class, WorldRegion.class).getDeclaringClass().equals(filterClass);
        }
        catch (NoSuchMethodException e)
        {
            e.printStackTrace();
            processRegions = false;
            processBlocks = false;
            processEntities = false;
        }
    }

    @Override
    public void threadCode()
    {
        // Initialize filter and get execution properties
        long startTime = System.currentTimeMillis();
        filter.initialize(); if (executor.isCancelled()) return;
        iterations = filter.iterations(); if (executor.isCancelled()) return;
        ignoreRepeatEntities = settings.ignoreRepeatEntities;

        // Start progress bar
        ProgressBar.start(filter.getName(), iterations, () -> executor.cancel("Filter cancelled"));

        // Run filter iterations
        for (int iteration = 0; iteration < iterations; iteration++) performIteration(iteration);

        // Filter cleanup
        filter.finished(); if (executor.isCancelled()) return;
        filter.print("Filter completed in " + (System.currentTimeMillis() - startTime) + "ms"); if (executor.isCancelled()) return;
        ProgressBar.finish();
    }
    @Override
    public void onExecutionEnded()
    {
        filterModule.markFilterFinished();
    }

    private void calculateEntities()
    {
        // Calculate Entities
        if (processEntities)
        {
            if (ignoreRepeatEntities)
            {
                for (WorldRegion region : executor.getRegions())
                {
                    Set<Entity> regionEntitySet = new HashSet<>();
                    region.forEachEntity(entity ->
                    {
                        if (!allEntities.contains(entity.keystoneUUID()))
                        {
                            allEntities.add(entity.keystoneUUID());
                            regionEntitySet.add(entity);
                        }
                    });
                    entitiesByRegion.put(region, regionEntitySet);
                }
            }
            else for (WorldRegion region : executor.getRegions())
            {
                Set<Entity> regionEntitySet = new HashSet<>();
                region.forEachEntity(entity ->
                {
                    allEntities.add(entity.keystoneUUID());
                    regionEntitySet.add(entity);
                });
                entitiesByRegion.put(region, regionEntitySet);
            }
        }
    }

    private void performIteration(int iteration)
    {
        // Initialize Pass
        filter.setPass(iteration); if (executor.isCancelled()) return;
        filter.preparePass(); if (executor.isCancelled()) return;
        calculateEntities();

        // Prepare Regions
        for (WorldRegion region : executor.getRegions())
        {
            filter.prepareRegion(region);
            if (executor.isCancelled()) return;
        }

        // Configure Progress Bar
        int progressBarSteps = allEntities.size();
        for (WorldRegion region : executor.getRegions())
        {
            progressBarSteps += region.size.x * region.size.y * region.size.z;
            progressBarSteps += filter.getRegionSteps(region);
            if (executor.isCancelled()) return;
        }
        ProgressBar.beginIteration(progressBarSteps);

        // Process Regions
        if (processRegions)
        {
            Stream<WorldRegion> regionStream = Arrays.stream(executor.getRegions());
            regionStream.forEach(filter::processRegion);
        }

        // Process Blocks
        if (processBlocks)
        {
            Stream<WorldRegion> regionStream = Arrays.stream(executor.getRegions());
            regionStream.forEach(region ->
            {
                Stream<BlockPos> blockStream = region.streamBlocks();
                blockStream.forEach(pos ->
                {
                    filter.processBlock(pos.x, pos.y, pos.z, region);
                    ProgressBar.nextStep();
                });
            });
        }

        // Process Entities
        if (processEntities)
        {
            Stream<WorldRegion> regionStream = entitiesByRegion.keySet().stream();
            regionStream.forEach(region ->
            {
                Stream<Entity> entityStream = entitiesByRegion.get(region).stream();
                entityStream.forEach(entity -> filter.processEntity(entity, region));
            });
        }

        // Complete Iteration
        filter.finishPass(); if (executor.isCancelled()) return;
        if (iteration < iterations - 1)
        {
            historyModule.swapBlockBuffers(true);
            historyModule.swapBiomeBuffers(true);
            historyModule.swapEntityBuffers(true);
        }
        ProgressBar.nextIteration();
    }
}
