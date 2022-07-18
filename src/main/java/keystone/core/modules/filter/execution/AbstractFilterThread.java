package keystone.core.modules.filter.execution;

import keystone.api.filters.FilterExecutionSettings;
import keystone.api.filters.KeystoneFilter;

public abstract class AbstractFilterThread extends Thread implements IFilterThread
{
    protected final KeystoneFilter filter;
    protected final FilterExecutionSettings settings;
    protected final FilterExecutor executor;

    public AbstractFilterThread(FilterExecutor executor)
    {
        this.filter = executor.getFilter();
        this.settings = executor.getSettings();
        this.executor = executor;
    }

    public abstract void threadCode();
    public void onExecutionEnded() {}

    @Override public KeystoneFilter getFilter() { return filter; }
    @Override public FilterExecutor getExecutor() { return executor; }
    @Override public FilterExecutionSettings getSettings() { return settings; }

    @Override
    public final void run()
    {
        try
        {
            threadCode();
        }
        catch (Throwable t)
        {
            executor.throwException(t);
        }
        onExecutionEnded();
    }
}
