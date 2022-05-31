package keystone.core.modules.filter.execution;

import keystone.api.Keystone;
import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.utils.ProgressBar;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class FilterExecutor
{
    private final HistoryModule historyModule = Keystone.getModule(HistoryModule.class);
    private final SelectionModule selectionModule = Keystone.getModule(SelectionModule.class);

    private final KeystoneFilter filter;
    private final WorldRegion[] regions;

    private final MainFilterThread mainThread;
    private final Set<AbstractFilterThread> threads;

    private Text[] filterError;

    private FilterExecutor(KeystoneFilter filter)
    {
        this.filter = filter;
        this.regions = selectionModule.buildRegions(filter.allowBlocksOutsideRegion());
        this.mainThread = new MainFilterThread(this);
        this.threads = new HashSet<>();
        this.threads.add(this.mainThread);
    }
    public static FilterExecutor create(KeystoneFilter filter)
    {
        if (filter == null) throw new IllegalArgumentException("Error creating FilterExecutor: Filter cannot be null!");
        else if (!filter.isCompiledSuccessfully()) throw new IllegalStateException("Error creating FilterExecutor: Filter " + filter.getName() + " was not compiled successfully!");
        return new FilterExecutor(filter);
    }

    public void start()
    {
        mainThread.start();
    }

    public CustomFilterThread newThread(Runnable threadCode, Runnable onExecutionEnded, boolean start)
    {
        CustomFilterThread thread = new CustomFilterThread(this, threadCode, onExecutionEnded, start);
        this.threads.add(thread);
        return thread;
    }


    //region Getters
    public KeystoneFilter getFilter() { return this.filter; }
    public WorldRegion[] getRegions() { return this.regions; }
    //endregion
    //region Error API
    /**
     * @return True if the filter was canceled, false otherwise
     */
    public boolean isCanceled() { return filterError != null; }
    /**
     * Checks if the filter should cancel, and prints the error message if it was
     * @return True if the filter was aborted, false otherwise
     */
    public boolean shouldCancel()
    {
        if (filterError != null)
        {
            for (Text reasonPart : filterError) MinecraftClient.getInstance().player.sendMessage(reasonPart, false);
            historyModule.abortHistoryEntry();
            ProgressBar.finish();
            return true;
        }
        else return false;
    }

    /**
     * Abort filter execution
     * @param reason The reason for aborting the filter
     */
    public void cancel(String... reason)
    {
        filterError = new Text[reason.length];
        for (int i = 0; i < reason.length; i++) filterError[i] = Text.literal(reason[i]).styled(style -> style.withColor(Formatting.RED));
    }

    /**
     * Report an exception during filter execution
     * @param throwable The throwable that was raised
     */
    public void throwException(Throwable throwable)
    {
        throwable.printStackTrace();

        StringBuilder reasonBuilder = new StringBuilder();
        reasonBuilder.append("An exception was thrown executing filter '");
        reasonBuilder.append(filter.getName());
        reasonBuilder.append("':");
        reasonBuilder.append(System.lineSeparator());

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        String utf8 = StandardCharsets.UTF_8.name();
        try (PrintStream printStream = new PrintStream(byteStream, true, utf8))
        {
            throwable.printStackTrace(printStream);
            reasonBuilder.append(byteStream.toString(utf8));
        }
        catch (UnsupportedEncodingException uee)
        {
            uee.printStackTrace();
        }

        String rawReason = reasonBuilder.toString();
        reasonBuilder = new StringBuilder();
        try (Scanner scanner = new Scanner(rawReason))
        {
            while (scanner.hasNext())
            {
                String line = scanner.nextLine();
                boolean tabbed = line.startsWith("\t");
                line = line.trim();
                if (line.contains("keystone.api")) break;
                else
                {
                    if (tabbed) reasonBuilder.append("    ");
                    reasonBuilder.append(line.replace(filter.getClass().getSimpleName(), filter.getName()).replace("(Unknown Source)", ""));
                    reasonBuilder.append(System.lineSeparator());
                }
            }
        }

        cancel(reasonBuilder.toString().trim().split(System.lineSeparator()));
    }
    //endregion
}
