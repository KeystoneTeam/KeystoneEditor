package keystone.api;

import keystone.api.filters.FilterBox;
import keystone.api.filters.KeystoneFilter;
import keystone.api.tools.interfaces.IBlockTool;
import keystone.api.tools.interfaces.IKeystoneTool;
import keystone.api.tools.interfaces.ISelectionBoxTool;
import keystone.core.KeystoneConfig;
import keystone.core.filters.FilterCompiler;
import keystone.core.renderer.client.Player;
import keystone.core.renderer.common.models.DimensionId;
import keystone.modules.IKeystoneModule;
import keystone.modules.history.HistoryModule;
import keystone.modules.history.IHistoryEntry;
import keystone.modules.history.entries.FillHistoryEntry;
import keystone.modules.history.entries.FilterHistoryEntry;
import keystone.modules.selection.SelectionModule;
import keystone.modules.world_cache.WorldCacheModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Consumer;

public class Keystone
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Random RANDOM = new Random();

    //region Active Toggle
    public static boolean CloseSelection = false;
    public static boolean RenderHighlightBox = true;
    public static boolean AllowPlayerLook = false;

    private static boolean enabled = KeystoneConfig.startActive;
    private static GameType previousGamemode;
    private static boolean revertGamemode;
    private static ITextComponent abortFilter;

    public static void toggleKeystone()
    {
        if (enabled)
        {
            enabled = false;
            Minecraft.getInstance().mouseHelper.grabMouse();
            revertGamemode = true;
        }
        else
        {
            enabled = true;
            AllowPlayerLook = false;
            Minecraft.getInstance().mouseHelper.ungrabMouse();
        }
    }
    public static boolean isActive()
    {
        return enabled && Minecraft.getInstance().world != null;
    }
    //endregion
    //region Module Registry
    private static Map<Class, IKeystoneModule> modules = new HashMap<>();

    public static void registerModule(IKeystoneModule module)
    {
        if (modules.containsKey(module.getClass())) LOGGER.error("Trying to register keystone module '" + module.getClass().getSimpleName() + "', when it was already registered!");
        else modules.put(module.getClass(), module);
    }
    public static <T extends IKeystoneModule> T getModule(Class<T> clazz)
    {
        if (modules.containsKey(clazz)) return (T)modules.get(clazz);
        else LOGGER.error("Trying to get unregistered keystone module '" + clazz.getSimpleName() + "'!");
        return null;
    }
    public static void forEachModule(Consumer<IKeystoneModule> consumer)
    {
        modules.values().forEach(consumer);
    }
    //endregion
    //region Threading
    private static class DelayedRunnable
    {
        private int delay;
        private Runnable runnable;
        private boolean executed;

        public DelayedRunnable(int delay, Runnable runnable)
        {
            this.delay = delay;
            this.runnable = runnable;
            this.executed = false;
        }
        public void tick()
        {
            if (delay <= 0)
            {
                runnable.run();
                executed = true;
            }
            else delay--;
        }
        public boolean executed()
        {
            return executed;
        }
    }

    private static List<DelayedRunnable> runOnMainThread = new ArrayList<>();
    private static List<DelayedRunnable> addList = new ArrayList<>();

    public static void runOnMainThread(Runnable runnable) { runOnMainThread(0, runnable); }
    public static void runOnMainThread(int delay, Runnable runnable)
    {
        addList.add(new DelayedRunnable(delay, runnable));
    }
    //endregion
    //region Tools
    public static void runTool(IKeystoneTool tool)
    {
        runOnMainThread(() ->
        {
            DimensionId dimensionId = Player.getDimensionId();
            World world = getModule(WorldCacheModule.class).getDimensionWorld(dimensionId);
            if (world == null)
            {
                LOGGER.error("Trying to run keystone tool when there is no loaded world for dimension '" + dimensionId.getDimensionType().getRegistryName() + "'!");
                return;
            }

            SelectionBox[] boxes = getModule(SelectionModule.class).buildSelectionBoxes(world);

            Set<BlockPos> processedBlocks = new HashSet<>();
            for (SelectionBox box : boxes)
            {
                if (tool instanceof ISelectionBoxTool) ((ISelectionBoxTool)tool).process(box);
                if (tool instanceof IBlockTool)
                {
                    IBlockTool blockTool = (IBlockTool)tool;
                    box.forEachBlock((pos ->
                    {
                        if (!blockTool.ignoreRepeatBlocks() || !processedBlocks.contains(pos))
                        {
                            blockTool.process(pos, box);
                            processedBlocks.add(pos);
                        }
                    }));
                }
            }

            IHistoryEntry toolHistoryEntry = new FillHistoryEntry(world, boxes);
            getModule(HistoryModule.class).pushToHistory(toolHistoryEntry);
            toolHistoryEntry.redo();
        });
    }
    public static void runFilter(String filterPath)
    {
        abortFilter = null;

        KeystoneFilter filter = FilterCompiler.compileFilter(filterPath);
        if (abortFilter != null)
        {
            Minecraft.getInstance().player.sendMessage(abortFilter, Util.DUMMY_UUID);
            return;
        }
        else runFilter(filter);
    }
    public static void runFilter(KeystoneFilter filter) { runFilter(filter, 0); }
    public static void runFilter(KeystoneFilter filter, int ticksDelay)
    {
        runOnMainThread(ticksDelay, () ->
        {
            abortFilter = null;

            if (filter.isCompiledSuccessfully())
            {
                DimensionId dimensionId = Player.getDimensionId();
                World world = getModule(WorldCacheModule.class).getDimensionWorld(dimensionId);
                if (world == null)
                {
                    LOGGER.error("Trying to run keystone tool when there is no loaded world for dimension '" + dimensionId.getDimensionType().getRegistryName() + "'!");
                    return;
                }

                SelectionBox[] selectionBoxes = getModule(SelectionModule.class).buildSelectionBoxes(world);
                FilterBox[] boxes = new FilterBox[selectionBoxes.length];
                for (int i = 0; i < boxes.length; i++) boxes[i] = new FilterBox(world, selectionBoxes[i], filter);

                filter.prepare();
                if (abortFilter != null)
                {
                    Minecraft.getInstance().player.sendMessage(abortFilter, Util.DUMMY_UUID);
                    return;
                }

                Set<BlockPos> processedBlocks = new HashSet<>();
                for (FilterBox box : boxes)
                {
                    filter.processBox(box);
                    if (abortFilter != null)
                    {
                        Minecraft.getInstance().player.sendMessage(abortFilter, Util.DUMMY_UUID);
                        return;
                    }

                    box.forEachBlock((x, y, z) ->
                    {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (!filter.ignoreRepeatBlocks() || !processedBlocks.contains(pos))
                        {
                            filter.processBlock(x, y, z, box);
                            processedBlocks.add(pos);
                        }

                        if (abortFilter != null)
                        {
                            Minecraft.getInstance().player.sendMessage(abortFilter, Util.DUMMY_UUID);
                            return;
                        }
                    });
                }

                if (abortFilter != null)
                {
                    Minecraft.getInstance().player.sendMessage(abortFilter, Util.DUMMY_UUID);
                    return;
                }

                filter.finished();
                if (abortFilter != null)
                {
                    Minecraft.getInstance().player.sendMessage(abortFilter, Util.DUMMY_UUID);
                    return;
                }

                IHistoryEntry toolHistoryEntry = new FilterHistoryEntry(world, boxes);
                getModule(HistoryModule.class).pushToHistory(toolHistoryEntry);
                toolHistoryEntry.redo();
            }
        });
    }
    public static void abortFilter(String reason)
    {
        abortFilter = new StringTextComponent(reason).mergeStyle(TextFormatting.RED);
    }
    //endregion
    //region Event Handling
    public static final void init()
    {
        MinecraftForge.EVENT_BUS.addListener(Keystone::onWorldTick);
        MinecraftForge.EVENT_BUS.addListener(Keystone::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(Keystone::onRightClickBlock);
    }

    private static final void onWorldTick(final TickEvent.WorldTickEvent event)
    {
        if (Keystone.isActive() && event.phase == TickEvent.Phase.START)
        {
            runOnMainThread.addAll(addList);
            addList.clear();

            for (DelayedRunnable runnable : runOnMainThread) runnable.tick();
            runOnMainThread.removeIf(runnable -> runnable.executed());
        }
    }
    private static final void onPlayerTick(final TickEvent.PlayerTickEvent event)
    {
        ClientPlayerEntity clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer == null) return;

        if (Keystone.isActive())
        {
            if (event.player instanceof ServerPlayerEntity)
            {
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity)event.player;
                if (serverPlayer.getUniqueID().equals(clientPlayer.getUniqueID()))
                {
                    if (serverPlayer.interactionManager.getGameType() != GameType.SPECTATOR)
                    {
                        previousGamemode = serverPlayer.interactionManager.getGameType();
                        serverPlayer.setGameType(GameType.SPECTATOR);
                    }
                }
            }
        }
        else if (revertGamemode)
        {
            if (event.player instanceof ServerPlayerEntity)
            {
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity)event.player;
                if (serverPlayer.getUniqueID().equals(clientPlayer.getUniqueID()))
                {
                    if (previousGamemode != null) serverPlayer.setGameType(previousGamemode);
                    revertGamemode = false;
                }
            }
        }
    }
    private static final void onRightClickBlock(final PlayerInteractEvent.RightClickBlock event)
    {
        if (Keystone.isActive()) event.setCanceled(true);
    }
    //endregion
}
