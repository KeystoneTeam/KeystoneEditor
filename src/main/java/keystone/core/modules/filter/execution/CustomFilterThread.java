package keystone.core.modules.filter.execution;

public class CustomFilterThread extends AbstractFilterThread
{
    private final Runnable threadCode;
    private final Runnable onExecutionEnded;

    public CustomFilterThread(FilterExecutor executor, Runnable threadCode, Runnable onExecutionEnded, boolean start)
    {
        super(executor);
        this.threadCode = threadCode;
        this.onExecutionEnded = onExecutionEnded;
        if (start) start();
    }

    @Override
    public void threadCode()
    {
        if (threadCode != null) threadCode.run();
    }

    @Override
    public void onExecutionEnded()
    {
        if (onExecutionEnded != null) onExecutionEnded.run();
    }
}
