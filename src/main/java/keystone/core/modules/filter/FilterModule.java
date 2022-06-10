package keystone.core.modules.filter;

import keystone.api.Keystone;
import keystone.api.KeystoneDirectories;
import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.wrappers.entities.Entity;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.filter.execution.FilterExecutor;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.utils.ProgressBar;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FilterModule implements IKeystoneModule
{
    private HistoryModule historyModule;
    private SelectionModule selectionModule;
    private FilterDirectoryManager filterDirectoryManager;
    private Queue<FilterExecutor[]> filterQueue;
    private FilterExecutor[] currentExecutorGroup;
    private int currentExecutorIndex;
    private boolean executingFilter;

    @Override
    public boolean isEnabled() { return true; }

    @Override
    public void postInit()
    {
        historyModule = Keystone.getModule(HistoryModule.class);
        selectionModule = Keystone.getModule(SelectionModule.class);
        filterDirectoryManager = FilterDirectoryManager.create(KeystoneDirectories.getStockFilterCache(), KeystoneDirectories.getFilterDirectory());
        filterQueue = new ConcurrentLinkedQueue<>();

        ServerTickEvents.START_SERVER_TICK.register(this::tick);
    }

    private void tick(MinecraftServer server)
    {
        if (!executingFilter)
        {
            if (currentExecutorGroup == null && filterQueue.size() > 0)
            {
                historyModule.beginHistoryEntry();
                currentExecutorGroup = filterQueue.poll();
                currentExecutorIndex = 0;
            }
            if (currentExecutorGroup != null && currentExecutorIndex < currentExecutorGroup.length)
            {
                currentExecutorGroup[currentExecutorIndex].start();
                executingFilter = true;
            }
        }
    }

    public FilterDirectoryManager getFilterDirectoryManager() { return this.filterDirectoryManager; }
    public boolean isExecutingFilter() { return this.executingFilter; }

    public void markFilterFinished()
    {
        currentExecutorIndex++;
        if (currentExecutorIndex >= currentExecutorGroup.length)
        {
            currentExecutorGroup = null;
            historyModule.endHistoryEntry();
        }
        this.executingFilter = false;
    }

    /**
     * Run one or more {@link keystone.api.filters.KeystoneFilter KeystoneFilters} on the current selection boxes.
     * All filters will be performed on the same history entry
     * @param filters The filter to run
     */
    public void runFilters(KeystoneFilter... filters) { runFilters(0, filters); }
    /**
     * Run one or more {@link keystone.api.filters.KeystoneFilter KeystoneFilters} on the current selection boxes after a delay.
     * All filters will be performed on the same history entry
     * @param ticksDelay The delay, in ticks
     * @param filters The filters to run
     */
    public void runFilters(int ticksDelay, KeystoneFilter... filters)
    {
        Keystone.runDelayed(ticksDelay, () ->
        {
            FilterExecutor[] executors = new FilterExecutor[filters.length];
            for (int i = 0; i < executors.length; i++) executors[i] = FilterExecutor.create(filters[i]);
            filterQueue.offer(executors);
        });
    }
}
