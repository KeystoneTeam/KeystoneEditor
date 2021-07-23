package keystone.core.modules.filter;

import keystone.api.Keystone;
import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.selection.SelectionModule;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class FilterModule implements IKeystoneModule
{
    private HistoryModule historyModule;
    private SelectionModule selectionModule;
    private ITextComponent[] abortFilter;

    @Override
    public boolean isEnabled() { return true; }

    @Override
    public void postInit()
    {
        historyModule = Keystone.getModule(HistoryModule.class);
        selectionModule = Keystone.getModule(SelectionModule.class);
    }

    /**
     * Compile and run a {@link keystone.api.filters.KeystoneFilter} on the current selection boxes
     * @param filterPath The path to the filter file
     */
    public void runFilter(String filterPath)
    {
        abortFilter = null;

        KeystoneFilter filter = FilterCompiler.compileFilter(filterPath);
        if (abortFilter != null)
        {
            for (ITextComponent reasonPart : abortFilter) Minecraft.getInstance().player.sendMessage(reasonPart, Util.NIL_UUID);
            return;
        }
        else runFilter(filter);
    }
    /**
     * Run a {@link keystone.api.filters.KeystoneFilter} on the current selection boxes
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
        Keystone.runOnMainThread(ticksDelay, () ->
        {
            abortFilter = null;

            if (filter.isCompiledSuccessfully())
            {
                historyModule.beginHistoryEntry();
                WorldRegion[] regions = selectionModule.buildRegions(filter.allowBlocksOutsideRegion());
                filter.setBlockRegions(regions);

                try
                {
                    int iterations = filter.iterations();
                    for (int iteration = 0; iteration < iterations; iteration++)
                    {
                        filter.setIteration(iteration);
                        filter.prepare();
                        if (abortFilter != null)
                        {
                            for (ITextComponent reasonPart : abortFilter) Minecraft.getInstance().player.sendMessage(reasonPart, Util.NIL_UUID);
                            historyModule.abortHistoryEntry();
                            return;
                        }

                        Set<BlockPos> processedBlocks = new HashSet<>();
                        for (WorldRegion box : regions)
                        {
                            filter.processRegion(box);
                            if (abortFilter != null)
                            {
                                for (ITextComponent reasonPart : abortFilter) Minecraft.getInstance().player.sendMessage(reasonPart, Util.NIL_UUID);
                                historyModule.abortHistoryEntry();
                                return;
                            }

                            box.forEachBlock((x, y, z, block) ->
                            {
                                BlockPos pos = new BlockPos(x, y, z);
                                if (!filter.ignoreRepeatBlocks() || !processedBlocks.contains(pos))
                                {
                                    filter.processBlock(x, y, z, box);
                                    processedBlocks.add(pos);
                                }

                                if (abortFilter != null)
                                {
                                    for (ITextComponent reasonPart : abortFilter) Minecraft.getInstance().player.sendMessage(reasonPart, Util.NIL_UUID);
                                    historyModule.abortHistoryEntry();
                                    return;
                                }
                            });
                        }

                        if (iteration < iterations - 1) historyModule.swapBlockBuffers(true);
                    }
                }
                catch (Exception e)
                {
                    filterException(filter, e);
                }

                if (abortFilter != null)
                {
                    for (ITextComponent reasonPart : abortFilter) Minecraft.getInstance().player.sendMessage(reasonPart, Util.NIL_UUID);
                    historyModule.abortHistoryEntry();
                    return;
                }

                filter.finished();
                if (abortFilter != null)
                {
                    for (ITextComponent reasonPart : abortFilter) Minecraft.getInstance().player.sendMessage(reasonPart, Util.NIL_UUID);
                    historyModule.abortHistoryEntry();
                    return;
                }

                for (WorldRegion region : regions) region.updateEntities();
                historyModule.endHistoryEntry();
            }
        });
    }

    /**
     * Abort filter execution
     * @param reason The reason for aborting the filter
     */
    public void abortFilter(String... reason)
    {
        abortFilter = new ITextComponent[reason.length];
        for (int i = 0; i < reason.length; i++) abortFilter[i] = new StringTextComponent(reason[i]).withStyle(TextFormatting.RED);
    }
    public void filterException(KeystoneFilter filter, Exception e)
    {
        e.printStackTrace();

        StringBuilder reasonBuilder = new StringBuilder();
        reasonBuilder.append("An exception was thrown executing filter '");
        reasonBuilder.append(filter.getName());
        reasonBuilder.append("':");
        reasonBuilder.append(System.lineSeparator());

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        String utf8 = StandardCharsets.UTF_8.name();
        try (PrintStream printStream = new PrintStream(byteStream, true, utf8))
        {
            e.printStackTrace(printStream);
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

        abortFilter(reasonBuilder.toString().trim().split(System.lineSeparator()));
    }
}
