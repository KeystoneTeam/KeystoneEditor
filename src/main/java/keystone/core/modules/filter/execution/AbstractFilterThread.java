package keystone.core.modules.filter.execution;

import keystone.api.filters.KeystoneFilter;

public abstract class AbstractFilterThread extends Thread
{
    protected final KeystoneFilter filter;
    protected final FilterExecutor executor;

    public AbstractFilterThread(FilterExecutor executor)
    {
        this.filter = executor.getFilter();
        this.executor = executor;
    }

    public abstract void threadCode();
    public void onExecutionEnded() {}

    public KeystoneFilter getFilter() { return filter; }
    public FilterExecutor getExecutor() { return executor; }

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
