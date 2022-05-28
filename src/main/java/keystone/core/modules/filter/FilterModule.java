package keystone.core.modules.filter;

import keystone.api.Keystone;
import keystone.api.KeystoneDirectories;
import keystone.api.WorldRegion;
import keystone.api.filters.KeystoneFilter;
import keystone.api.wrappers.entities.Entity;
import keystone.core.modules.IKeystoneModule;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.utils.ProgressBar;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

public class FilterModule implements IKeystoneModule
{
    private HistoryModule historyModule;
    private SelectionModule selectionModule;
    private FilterDirectoryManager filterDirectoryManager;
    private Text[] abortFilter;

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
     * Compile and run a {@link keystone.api.filters.KeystoneFilter} on the current selection boxes
     * @param filterPath The path to the filter file
     */
    public void runFilter(String filterPath)
    {
        abortFilter = null;
        File filterFile = Paths.get(filterPath).toFile();
        if (!filterFile.exists())
        {
            abortFilter("Invalid Filter Path: '" + filterPath + "'!");
            testAborted();
            return;
        }

        KeystoneFilter filter = FilterCompiler.compileFilter(Paths.get(filterPath).toFile());
        if (!testAborted()) runFilter(filter);
    }
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
        Keystone.runOnMainThread(ticksDelay, () ->
        {
            abortFilter = null;

            if (filter.isCompiledSuccessfully())
            {
                historyModule.beginHistoryEntry();
                WorldRegion[] regions = selectionModule.buildRegions(filter.allowBlocksOutsideRegion());
                filter.setBlockRegions(regions);

                long startTime = System.currentTimeMillis();
                try
                {
                    filter.initialize();
                    if (testAborted()) return;

                    int iterations = filter.iterations();
                    ProgressBar.start(filter.getName(), iterations, () -> abortFilter("Filter cancelled"));
                    for (int iteration = 0; iteration < iterations; iteration++)
                    {
                        filter.setIteration(iteration);
                        filter.preparePass();
                        if (testAborted()) return;

                        Class<? extends KeystoneFilter> filterClass = filter.getClass();
                        boolean processRegions = filterClass.getMethod("processRegion", WorldRegion.class).getDeclaringClass().equals(filterClass);
                        boolean processBlocks = filterClass.getMethod("processBlock", int.class, int.class, int.class, WorldRegion.class).getDeclaringClass().equals(filterClass);
                        boolean processEntities = filterClass.getMethod("processEntity", Entity.class, WorldRegion.class).getDeclaringClass().equals(filterClass);

                        Set<BlockPos> blocks = new HashSet<>();
                        Map<WorldRegion, Set<BlockPos>> regionBlocks = new HashMap<>();
                        Set<UUID> entities = new HashSet<>();
                        Map<WorldRegion, Set<Entity>> regionEntities = new HashMap<>();

                        // Calculate Block Positions
                        if (processBlocks)
                        {
                            if (filter.ignoreRepeatBlocks())
                            {
                                for (WorldRegion region : regions)
                                {
                                    Set<BlockPos> regionBlockSet = new HashSet<>();
                                    region.forEachBlock((x, y, z, blockType) ->
                                    {
                                        BlockPos pos = new BlockPos(x, y, z);
                                        if (!blocks.contains(pos))
                                        {
                                            blocks.add(pos);
                                            regionBlockSet.add(pos);
                                        }
                                    });
                                    regionBlocks.put(region, regionBlockSet);
                                }
                            }
                            else for (WorldRegion region : regions)
                            {
                                Set<BlockPos> regionBlockSet = new HashSet<>();
                                region.forEachBlock((x, y, z, blockType) ->
                                {
                                    BlockPos pos = new BlockPos(x, y, z);
                                    blocks.add(pos);
                                    regionBlockSet.add(pos);
                                });
                                regionBlocks.put(region, regionBlockSet);
                            }
                        }

                        // Calculate Entities
                        if (processEntities)
                        {
                            if (filter.ignoreRepeatEntities())
                            {
                                for (WorldRegion region : regions)
                                {
                                    Set<Entity> regionEntitySet = new HashSet<>();
                                    region.forEachEntity(entity ->
                                    {
                                        if (!entities.contains(entity.keystoneUUID()))
                                        {
                                            entities.add(entity.keystoneUUID());
                                            regionEntitySet.add(entity);
                                        }
                                    });
                                    regionEntities.put(region, regionEntitySet);
                                }
                            }
                            else for (WorldRegion region : regions)
                            {
                                Set<Entity> regionEntitySet = new HashSet<>();
                                region.forEachEntity(entity ->
                                {
                                    entities.add(entity.keystoneUUID());
                                    regionEntitySet.add(entity);
                                });
                                regionEntities.put(region, regionEntitySet);
                            }
                        }

                        // Prepare Regions
                        for (WorldRegion region : regions)
                        {
                            filter.prepareRegion(region);
                            if (testAborted()) return;
                        }

                        // Configure Progress Bar
                        int progressBarSteps = blocks.size() + entities.size();
                        for (WorldRegion region : regions) progressBarSteps += filter.getRegionSteps(region);
                        ProgressBar.beginIteration(progressBarSteps);

                        // Process Regions
                        if (processRegions)
                        {
                            for (WorldRegion region : regions)
                            {
                                filter.processRegion(region);
                                if (testAborted()) return;
                            }
                        }

                        // Process Blocks
                        if (processBlocks)
                        {
                            for (Map.Entry<WorldRegion, Set<BlockPos>> entry : regionBlocks.entrySet())
                            {
                                for (BlockPos pos : entry.getValue())
                                {
                                    filter.processBlock(pos.getX(), pos.getY(), pos.getZ(), entry.getKey());
                                    if (testAborted()) return;
                                    else ProgressBar.nextStep();
                                }
                            }
                        }

                        // Process Entities
                        if (processEntities)
                        {
                            for (Map.Entry<WorldRegion, Set<Entity>> entry : regionEntities.entrySet())
                            {
                                for (Entity entity : entry.getValue())
                                {
                                    filter.processEntity(entity, entry.getKey());
                                    if (testAborted()) return;
                                    else ProgressBar.nextStep();
                                }
                            }
                        }

                        // Complete Iteration
                        filter.finishPass();
                        if (testAborted()) return;

                        if (iteration < iterations - 1)
                        {
                            historyModule.swapBlockBuffers(true);
                            historyModule.swapEntityBuffers(true);
                        }
                        ProgressBar.nextIteration();
                    }
                }
                catch (Exception e)
                {
                    filterException(filter, e);
                }

                if (testAborted()) return;

                filter.finished();
                if (testAborted()) return;

                filter.print("Filter completed in " + (System.currentTimeMillis() - startTime) + "ms");
                historyModule.endHistoryEntry();
                ProgressBar.finish();
            }
        });
    }
    private boolean testAborted()
    {
        if (abortFilter != null)
        {
            for (Text reasonPart : abortFilter) MinecraftClient.getInstance().player.sendMessage(reasonPart, false);
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
    public void abortFilter(String... reason)
    {
        abortFilter = new Text[reason.length];
        for (int i = 0; i < reason.length; i++) abortFilter[i] = Text.literal(reason[i]).styled(style -> style.withColor(Formatting.RED));
    }
    public void filterException(KeystoneFilter filter, Throwable throwable)
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

        abortFilter(reasonBuilder.toString().trim().split(System.lineSeparator()));
    }
}
