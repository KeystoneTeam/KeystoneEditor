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
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class FilterModule implements IKeystoneModule
{
    private HistoryModule historyModule;
    private SelectionModule selectionModule;
    private FilterDirectoryManager filterDirectoryManager;

    @Override
    public boolean isEnabled() { return true; }

    @Override
    public void postInit()
    {
        historyModule = Keystone.getModule(HistoryModule.class);
        selectionModule = Keystone.getModule(SelectionModule.class);
        filterDirectoryManager = FilterDirectoryManager.create(KeystoneDirectories.getStockFilterCache(), KeystoneDirectories.getFilterDirectory());
    }

    public FilterDirectoryManager getFilterDirectoryManager() { return this.filterDirectoryManager; }

    /**
     * Run a {@link KeystoneFilter} on the current selection boxes
     * @param filter The filter to run
     */
    public void runFilter(KeystoneFilter filter) { runFilter(filter, 0); }
    /**
     * Run a {@link keystone.api.filters.KeystoneFilter} on the current selection boxes after a delay
     * @param filter The filter to run
     * @param ticksDelay The delay, in ticks
     */
    public void runFilter(KeystoneFilter filter, int ticksDelay)
    {
        Keystone.runDelayed(ticksDelay, () ->
        {
            FilterExecutor executor = FilterExecutor.create(filter);
            executor.start();
        });
    }
}
